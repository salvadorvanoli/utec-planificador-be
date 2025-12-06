package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.aiagent.AIReportRequest.CourseStatisticsDto;
import edu.utec.planificador.dto.request.CourseRequest;
import edu.utec.planificador.dto.response.CourseBasicResponse;
import edu.utec.planificador.dto.response.CourseBriefResponse;
import edu.utec.planificador.dto.response.CourseDetailedInfoResponse;
import edu.utec.planificador.dto.response.CoursePdfDataResponse;
import edu.utec.planificador.dto.response.CourseResponse;
import edu.utec.planificador.dto.response.PeriodResponse;
import edu.utec.planificador.dto.response.TeacherCourseResponse;
import edu.utec.planificador.entity.Activity;
import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.CurricularUnit;
import edu.utec.planificador.entity.OfficeHours;
import edu.utec.planificador.entity.Position;
import edu.utec.planificador.entity.Program;
import edu.utec.planificador.entity.ProgrammaticContent;
import edu.utec.planificador.entity.Teacher;
import edu.utec.planificador.entity.Term;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.entity.WeeklyPlanning;
import edu.utec.planificador.enumeration.DeliveryFormat;
import edu.utec.planificador.enumeration.PartialGradingSystem;
import edu.utec.planificador.enumeration.Role;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;
import edu.utec.planificador.exception.ForbiddenException;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.mapper.CourseMapper;
import edu.utec.planificador.mapper.CourseStatisticsMapper;
import edu.utec.planificador.repository.CampusRepository;
import edu.utec.planificador.repository.CourseRepository;
import edu.utec.planificador.repository.CurricularUnitRepository;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.service.AccessControlService;
import edu.utec.planificador.service.CourseService;
import edu.utec.planificador.service.MessageService;
import edu.utec.planificador.specification.CourseSpecification;
import edu.utec.planificador.util.WeeklyPlanningGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CurricularUnitRepository curricularUnitRepository;
    private final UserRepository userRepository;
    private final CampusRepository campusRepository;
    private final CourseMapper courseMapper;
    private final CourseStatisticsMapper courseStatisticsMapper;
    private final AccessControlService accessControlService;
    private final MessageService messageService;

    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        log.debug("Creating course for curricular unit: {}", request.getCurricularUnitId());
        
        // Validate startDate <= endDate
        if (request.getStartDate() != null && request.getEndDate() != null && 
            request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException(
                messageService.getMessage("error.course.start-after-end")
            );
        }
        
        accessControlService.validateCurricularUnitAccess(request.getCurricularUnitId());

        CurricularUnit curricularUnit = curricularUnitRepository.findById(request.getCurricularUnitId())
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.curricular-unit.not-found")
            ));
        
        Long programId = curricularUnit.getTerm().getProgram().getId();
        List<Campus> programCampuses = campusRepository.findByProgram(programId);
        
        if (programCampuses.isEmpty()) {
            throw new IllegalStateException(messageService.getMessage("error.course.program-no-campuses"));
        }
        
        List<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .toList();
        
        log.debug("Curricular unit's program is offered at {} campus(es): {}", programCampuses.size(), programCampusIds);
        
        List<Teacher> teachers = new ArrayList<>();
        for (Long userId : request.getUserIds()) {
            User user = userRepository.findByIdWithPositions(userId)
                .orElseThrow(() -> new ResourceNotFoundException(messageService.getMessage("error.user.not-found")));
            
            Teacher teacher = user.getPositions().stream()
                .filter(pos -> pos instanceof Teacher)
                .map(pos -> (Teacher) pos)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(messageService.getMessage("error.course.user-not-teacher")));
            
            boolean teacherBelongsToValidCampus = teacher.getCampuses().stream()
                .anyMatch(campus -> programCampusIds.contains(campus.getId()));
            
            if (!teacherBelongsToValidCampus) {
                throw new IllegalArgumentException(
                    messageService.getMessage("error.course.teacher-wrong-campus")
                );
            }
            
            teachers.add(teacher);
        }
        
        log.debug("Validated {} teacher(s) for course", teachers.size());
        
        // If multiple teachers, validate they share at least one common campus within the program's campuses
        if (teachers.size() > 1) {
            Set<Long> commonCampusIds = teachers.get(0).getCampuses().stream()
                .map(Campus::getId)
                .filter(programCampusIds::contains)
                .collect(Collectors.toSet());
            
            for (int i = 1; i < teachers.size(); i++) {
                Set<Long> teacherCampusIds = teachers.get(i).getCampuses().stream()
                    .map(Campus::getId)
                    .filter(programCampusIds::contains)
                    .collect(Collectors.toSet());
                
                commonCampusIds.retainAll(teacherCampusIds);
            }
            
            if (commonCampusIds.isEmpty()) {
                throw new IllegalArgumentException(
                    messageService.getMessage("error.course.teachers-no-common-campus")
                );
            }
            
            log.debug("Teachers share {} common campus(es): {}", commonCampusIds.size(), commonCampusIds);
        }
        
        Course course = new Course(
            request.getShift(),
            request.getStartDate(),
            request.getEndDate(),
            curricularUnit
        );
        
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            course.setDescription(request.getDescription());
        } else {
            String defaultDescription = "Curso de " + curricularUnit.getName();
            course.setDescription(defaultDescription);
            log.debug("Using default description: {}", defaultDescription);
        }
        
        if (request.getPartialGradingSystem() != null) {
            course.setPartialGradingSystem(request.getPartialGradingSystem());
        } else {
            course.setPartialGradingSystem(PartialGradingSystem.PGS_1);
            log.debug("Using default partial grading system: PGS_1");
        }
        
        course.getTeachers().addAll(teachers);
        
        curricularUnit.getCourses().add(course);
        
        if (request.getIsRelatedToInvestigation() != null) {
            course.setIsRelatedToInvestigation(request.getIsRelatedToInvestigation());
        }
        
        if (request.getInvolvesActivitiesWithProductiveSector() != null) {
            course.setInvolvesActivitiesWithProductiveSector(request.getInvolvesActivitiesWithProductiveSector());
        }
        
        if (request.getHoursPerDeliveryFormat() != null && !request.getHoursPerDeliveryFormat().isEmpty()) {
            // Validar que todas las horas sean no negativas
            for (Map.Entry<DeliveryFormat, Integer> entry : request.getHoursPerDeliveryFormat().entrySet()) {
                if (entry.getValue() < 0) {
                    throw new IllegalArgumentException(
                        messageService.getMessage("error.course.invalid-hours-format")
                    );
                }
            }
            course.getHoursPerDeliveryFormat().putAll(request.getHoursPerDeliveryFormat());
        } else {
            for (DeliveryFormat format : DeliveryFormat.values()) {
                course.getHoursPerDeliveryFormat().put(format, 0);
            }
            log.debug("Using default hours per delivery format: all formats set to 0");
        }
        
        if (request.getSustainableDevelopmentGoals() != null && !request.getSustainableDevelopmentGoals().isEmpty()) {
            course.getSustainableDevelopmentGoals().addAll(request.getSustainableDevelopmentGoals());
        }
        
        if (request.getUniversalDesignLearningPrinciples() != null && !request.getUniversalDesignLearningPrinciples().isEmpty()) {
            course.getUniversalDesignLearningPrinciples().addAll(request.getUniversalDesignLearningPrinciples());
        }
        
        // Generate weekly plannings automatically
        // Each WeeklyPlanning represents one full week (Monday to Sunday)
        // The course dates are aligned to full weeks:
        // - startDate is adjusted to the Monday of that week
        // - endDate is adjusted to the Sunday of that week
        log.debug("Generating weekly plannings for course from {} to {}", request.getStartDate(), request.getEndDate());
        List<WeeklyPlanning> weeklyPlannings = WeeklyPlanningGenerator.generateWeeklyPlannings(
            request.getStartDate(), 
            request.getEndDate()
        );
        course.getWeeklyPlannings().addAll(weeklyPlannings);
        log.info("Generated {} empty weekly planning(s) for course", weeklyPlannings.size());
        
        // Guardar el curso con WeeklyPlannings vacíos
        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with id: {} and {} teacher(s) - planning is empty and must be loaded manually by teacher", 
            savedCourse.getId(), teachers.size());
        
        return courseMapper.toResponse(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        log.debug("Getting course by id: {}", id);
        
        // Validate access to course
        accessControlService.validateCourseAccess(id);

        // Cargar el curso con weeklyPlannings (primera query)
        Course course = courseRepository.findByIdWithWeeklyPlannings(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.course.not-found")
            ));
        
        // Cargar teachers en query separada (evita MultipleBagFetchException con weeklyPlannings)
        // Hibernate detecta que es el mismo objeto en la sesión y actualiza la colección teachers
        courseRepository.findByIdWithTeachers(id);
        
        // Mapear dentro de la transacción para acceder a todas las colecciones
        return courseMapper.toResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getLatestCourseByCurricularUnitAndUser(Long curricularUnitId, Long userId) {
        log.debug("Getting latest course for curricular unit: {} and user: {}", curricularUnitId, userId);
        
        // Validate access to curricular unit
        accessControlService.validateCurricularUnitAccess(curricularUnitId);
        
        Course course = courseRepository.findLatestByCurricularUnitAndUsers(
            curricularUnitId, 
            List.of(userId), 
            null
        ).orElse(null);
        
        if (course == null) {
            log.debug("No previous course found for curricular unit: {} and user: {}", curricularUnitId, userId);
            return null;
        }
        
        log.debug("Found previous course with id: {}", course.getId());
        return courseMapper.toResponse(course);
    }

    /**
     * Ajusta los WeeklyPlannings de un curso cuando se modifican sus fechas de inicio o fin.
     * 
     * Comportamiento:
     * - Si se agregan semanas: Copia planificación de curso anterior si existe, o crea semanas vacías
     * - Si se eliminan semanas: Elimina las últimas WeeklyPlannings con sus contenidos
     * - Ajusta las fechas de las WeeklyPlannings existentes según las nuevas fechas del curso
     * 
     * @param course Curso a ajustar
     * @param newStartDate Nueva fecha de inicio
     * @param newEndDate Nueva fecha de fin
     * @param teacherIds IDs de los docentes del curso (para buscar cursos anteriores)
     */
    private void adjustWeeklyPlanningsIfNeeded(Course course, LocalDate newStartDate, LocalDate newEndDate, List<Long> teacherIds) {
        LocalDate oldStartDate = course.getStartDate();
        LocalDate oldEndDate = course.getEndDate();
        
        // Si no cambiaron las fechas, no hacer nada
        if (oldStartDate.equals(newStartDate) && oldEndDate.equals(newEndDate)) {
            log.debug("Course dates unchanged, no weekly planning adjustment needed");
            return;
        }
        
        log.debug("Adjusting weekly plannings due to date change: [{} to {}] -> [{} to {}]", 
            oldStartDate, oldEndDate, newStartDate, newEndDate);
        
        // Calcular el número de semanas actual y nuevo
        List<WeeklyPlanning> currentPlannings = new ArrayList<>(course.getWeeklyPlannings());
        int currentWeekCount = currentPlannings.size();
        
        List<WeeklyPlanning> newPlannings = WeeklyPlanningGenerator.generateWeeklyPlannings(newStartDate, newEndDate);
        int newWeekCount = newPlannings.size();
        
        log.debug("Current week count: {}, New week count: {}", currentWeekCount, newWeekCount);
        
        if (newWeekCount > currentWeekCount) {
            // Caso 1: Se agregan semanas
            log.info("Adding {} week(s) to course", newWeekCount - currentWeekCount);
            
            // Actualizar fechas de las semanas existentes
            for (int i = 0; i < currentWeekCount; i++) {
                WeeklyPlanning existing = currentPlannings.get(i);
                WeeklyPlanning template = newPlannings.get(i);
                existing.setStartDate(template.getStartDate());
                existing.setEndDate(template.getEndDate());
            }
            
            // Intentar copiar planificación de curso anterior para las nuevas semanas
            Optional<Course> sourceCourseOpt = courseRepository.findLatestByCurricularUnitAndUsers(
                course.getCurricularUnit().getId(),
                teacherIds,
                course.getId()
            );
            
            Course sourceCourse = null;
            java.util.Map<Integer, WeeklyPlanning> sourceWeeklyPlanningMap = null;
            
            if (sourceCourseOpt.isPresent()) {
                sourceCourse = sourceCourseOpt.get();
                log.debug("Found previous course {} for copying additional weeks", sourceCourse.getId());
                
                // Cargar los detalles del curso fuente
                courseRepository.findByIdWithWeeklyPlannings(sourceCourse.getId());
                courseRepository.loadProgrammaticContents(sourceCourse.getId());
                courseRepository.loadProgrammaticContentActivities(sourceCourse.getId());
                
                // Refrescar el curso fuente para obtener las colecciones cargadas
                sourceCourse = courseRepository.findByIdWithWeeklyPlannings(sourceCourse.getId()).orElse(null);
                
                if (sourceCourse != null) {
                    sourceWeeklyPlanningMap = sourceCourse.getWeeklyPlannings().stream()
                        .collect(java.util.stream.Collectors.toMap(
                            WeeklyPlanning::getWeekNumber,
                            wp -> wp
                        ));
                }
            }
            
            // Agregar las nuevas semanas
            for (int i = currentWeekCount; i < newWeekCount; i++) {
                WeeklyPlanning template = newPlannings.get(i);
                WeeklyPlanning newWeekly = new WeeklyPlanning(
                    template.getWeekNumber(),
                    template.getStartDate(),
                    template.getEndDate()
                );
                
                // Intentar copiar contenido del curso anterior si existe
                if (sourceWeeklyPlanningMap != null) {
                    WeeklyPlanning sourceWeekly = sourceWeeklyPlanningMap.get(template.getWeekNumber());
                    if (sourceWeekly != null) {
                        log.debug("Copying content from previous course for week {}", template.getWeekNumber());
                        
                        // Copiar referencias bibliográficas
                        newWeekly.getBibliographicReferences().addAll(sourceWeekly.getBibliographicReferences());
                        
                        // Copiar ProgrammaticContents y Activities
                        for (ProgrammaticContent sourceContent : sourceWeekly.getProgrammaticContents()) {
                            ProgrammaticContent newContent = new ProgrammaticContent(
                                sourceContent.getTitle(),
                                sourceContent.getContent(),
                                newWeekly
                            );
                            newContent.setColor(sourceContent.getColor());
                            
                            // Copiar Activities
                            for (Activity sourceActivity : sourceContent.getActivities()) {
                                Activity newActivity = new Activity(
                                    sourceActivity.getDescription(),
                                    sourceActivity.getDurationInMinutes(),
                                    sourceActivity.getLearningModality(),
                                    newContent
                                );
                                newActivity.setTitle(sourceActivity.getTitle());
                                newActivity.setColor(sourceActivity.getColor());
                                newActivity.getCognitiveProcesses().addAll(sourceActivity.getCognitiveProcesses());
                                newActivity.getTransversalCompetencies().addAll(sourceActivity.getTransversalCompetencies());
                                newActivity.getTeachingStrategies().addAll(sourceActivity.getTeachingStrategies());
                                newActivity.getLearningResources().addAll(sourceActivity.getLearningResources());
                                
                                newContent.getActivities().add(newActivity);
                            }
                            
                            newWeekly.getProgrammaticContents().add(newContent);
                        }
                    }
                }
                
                course.getWeeklyPlannings().add(newWeekly);
            }
            
            log.info("Added {} new week(s) to course", newWeekCount - currentWeekCount);
            
        } else if (newWeekCount < currentWeekCount) {
            // Caso 2: Se eliminan semanas
            int weeksToRemove = currentWeekCount - newWeekCount;
            log.info("Removing last {} week(s) from course", weeksToRemove);
            
            // Actualizar fechas de las semanas que se mantienen
            for (int i = 0; i < newWeekCount; i++) {
                WeeklyPlanning existing = currentPlannings.get(i);
                WeeklyPlanning template = newPlannings.get(i);
                existing.setStartDate(template.getStartDate());
                existing.setEndDate(template.getEndDate());
            }
            
            // Eliminar las últimas semanas (orphanRemoval se encargará de eliminar contenidos)
            List<WeeklyPlanning> toRemove = new ArrayList<>();
            for (int i = newWeekCount; i < currentWeekCount; i++) {
                toRemove.add(currentPlannings.get(i));
            }
            
            course.getWeeklyPlannings().removeAll(toRemove);
            log.info("Removed {} week(s) with their contents", weeksToRemove);
            
        } else {
            // Caso 3: Mismo número de semanas, solo actualizar fechas
            log.debug("Same week count, updating dates only");
            for (int i = 0; i < currentWeekCount; i++) {
                WeeklyPlanning existing = currentPlannings.get(i);
                WeeklyPlanning template = newPlannings.get(i);
                existing.setStartDate(template.getStartDate());
                existing.setEndDate(template.getEndDate());
            }
        }
        
        // Ajustar OfficeHours que queden fuera del nuevo rango de fechas
        adjustOfficeHours(course, newStartDate, newEndDate);
    }

    /**
     * Ajusta los OfficeHours de un curso cuando cambian las fechas.
     * Elimina los OfficeHours que queden fuera del nuevo rango de fechas del curso.
     * 
     * @param course Curso a ajustar
     * @param newStartDate Nueva fecha de inicio
     * @param newEndDate Nueva fecha de fin
     */
    private void adjustOfficeHours(Course course, LocalDate newStartDate, LocalDate newEndDate) {
        if (course.getOfficeHours() == null || course.getOfficeHours().isEmpty()) {
            return;
        }
        
        List<OfficeHours> officeHoursToRemove = course.getOfficeHours().stream()
            .filter(oh -> oh.getDate().isBefore(newStartDate) || oh.getDate().isAfter(newEndDate))
            .toList();
        
        if (!officeHoursToRemove.isEmpty()) {
            log.info("Removing {} office hour(s) that fall outside new course date range [{} to {}]", 
                officeHoursToRemove.size(), newStartDate, newEndDate);
            
            for (OfficeHours oh : officeHoursToRemove) {
                log.debug("Removing office hour on {} (outside new range)", oh.getDate());
            }
            
            course.getOfficeHours().removeAll(officeHoursToRemove);
        }
    }

    /**
     * Valida que un docente no intente modificar la unidad curricular o la lista de docentes
     * de un curso al actualizarlo.
     * 
     * @param course Curso que se está actualizando
     * @param request Datos de la actualización del curso
     */
    /**
     * Validates that users WITHOUT administrative roles cannot modify certain restricted fields.
     * Only users with COORDINATOR, ANALYST, or EDUCATION_MANAGER roles in the course's campus
     * can modify: shift, dates, and teachers list.
     * Users without these administrative roles are restricted from modifying these fields.
     */
    private void validateOnlyAdministrativeUsersModifyRestrictedFields(Course course, CourseRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUtecEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("error.user.current-not-found")));
        
        // Get course's campus information
        CurricularUnit curricularUnit = course.getCurricularUnit();
        Term term = curricularUnit.getTerm();
        Program program = term.getProgram();
        
        // Get campuses where this program is offered
        List<Campus> programCampuses = campusRepository.findByProgram(program.getId());
        Set<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .collect(Collectors.toSet());
        
        // Load full user with positions to check administrative roles
        User fullUser = userRepository.findByIdWithPositions(currentUser.getId())
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("error.user.not-found", currentUser.getId())));
        
        // Check if user has administrative roles (COORDINATOR, ANALYST, EDUCATION_MANAGER) in relevant campuses
        boolean hasAdminRoleInCourseCampus = fullUser.getPositions().stream()
            .filter(Position::getIsActive)
            .filter(position -> 
                position.getRole() == Role.COORDINATOR || 
                position.getRole() == Role.ANALYST
            )
            .flatMap(position -> position.getCampuses().stream())
            .map(Campus::getId)
            .anyMatch(programCampusIds::contains);
        
        // If user has administrative roles, they can modify restricted fields - skip validation
        if (hasAdminRoleInCourseCampus) {
            log.debug("User {} has administrative role in course {} campus - allowing all modifications", 
                currentUser.getUtecEmail(), course.getId());
            return;
        }
        
        // User does NOT have administrative roles - enforce restrictions on sensitive fields
        log.debug("User {} does not have administrative roles for course {} - enforcing restrictions", 
            currentUser.getUtecEmail(), course.getId());
        
        // Check if user is trying to change the teachers list
        Set<Long> currentTeacherIds = course.getTeachers().stream()
            .map(teacher -> teacher.getUser().getId())
            .collect(Collectors.toSet());
        Set<Long> requestedTeacherIds = new HashSet<>(request.getUserIds());
        
        if (!currentTeacherIds.equals(requestedTeacherIds)) {
            log.warn("User {} (without admin role) attempted to modify teachers list for course {}", 
                currentUser.getUtecEmail(), course.getId());
            throw new IllegalArgumentException(
                messageService.getMessage("error.course.teacher-cannot-modify-teachers")
            );
        }
        
        // Check if user is trying to change the shift
        if (!course.getShift().equals(request.getShift())) {
            log.warn("User {} (without admin role) attempted to change shift for course {}", 
                currentUser.getUtecEmail(), course.getId());
            throw new IllegalArgumentException(
                messageService.getMessage("error.course.teacher-cannot-modify-shift")
            );
        }
        
        // Check if user is trying to change the start date
        if (!course.getStartDate().equals(request.getStartDate())) {
            log.warn("User {} (without admin role) attempted to change start date for course {}", 
                currentUser.getUtecEmail(), course.getId());
            throw new IllegalArgumentException(
                messageService.getMessage("error.course.teacher-cannot-modify-dates")
            );
        }
        
        // Check if user is trying to change the end date
        if (!course.getEndDate().equals(request.getEndDate())) {
            log.warn("User {} (without admin role) attempted to change end date for course {}", 
                currentUser.getUtecEmail(), course.getId());
            throw new IllegalArgumentException(
                messageService.getMessage("error.course.teacher-cannot-modify-dates")
            );
        }
        
        log.debug("User {} (without admin role) validation passed for course {} update", 
            currentUser.getUtecEmail(), course.getId());
    }
    
    /**
     * Clears all planning data for a course when teachers are completely replaced.
     * This includes: WeeklyPlannings, ProgrammaticContents, Activities, and bibliographic references.
     * OfficeHours are preserved as they are not part of the weekly planning structure.
     */
    private void clearCoursePlanning(Course course) {
        log.info("Clearing all planning data for course {} due to complete teacher replacement", course.getId());
        
        int weeklyPlanningsCount = course.getWeeklyPlannings().size();
        
        // Clear all weekly plannings (cascade will remove programmatic contents and activities)
        course.getWeeklyPlannings().clear();
        
        // Regenerate empty weekly plannings based on course dates
        List<WeeklyPlanning> emptyPlannings = WeeklyPlanningGenerator.generateWeeklyPlannings(
            course.getStartDate(),
            course.getEndDate()
        );
        course.getWeeklyPlannings().addAll(emptyPlannings);
        
        log.info("Cleared {} weekly plannings and regenerated {} empty plannings for course {}", 
            weeklyPlanningsCount, emptyPlannings.size(), course.getId());
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        log.debug("Updating course with id: {}", id);
        
        // Validate update access (ANALYST/COORDINATOR in campus OR teacher of the course)
        accessControlService.validateCourseUpdateAccess(id);

        Course course = courseRepository.findByIdWithWeeklyPlannings(id)
            .orElseThrow(() -> new ResourceNotFoundException(messageService.getMessage("error.course.not-found")));
        
        // Validate that CurricularUnit is not being modified (not allowed for any user)
        if (!course.getCurricularUnit().getId().equals(request.getCurricularUnitId())) {
            log.warn("Attempt to change curricularUnit for course {} from {} to {}", 
                course.getId(), course.getCurricularUnit().getId(), request.getCurricularUnitId());
            throw new IllegalArgumentException(
                messageService.getMessage("error.course.cannot-modify-cu")
            );
        }
        
        // Additional validation: Teachers cannot modify shift or dates
        validateOnlyAdministrativeUsersModifyRestrictedFields(course, request);
        
        // Validate that the course has not finished
        accessControlService.validateCourseNotExpired(id);
        
        // Check if we need to clear planning due to teacher changes
        Set<Long> currentTeacherIds = course.getTeachers().stream()
            .map(teacher -> teacher.getUser().getId())
            .collect(Collectors.toSet());
        Set<Long> newTeacherIds = new HashSet<>(request.getUserIds());
        
        boolean hasCommonTeachers = currentTeacherIds.stream().anyMatch(newTeacherIds::contains);
        boolean shouldClearPlanning = !hasCommonTeachers && !currentTeacherIds.equals(newTeacherIds);
        
        if (shouldClearPlanning) {
            log.info("Clearing planning for course {} due to complete teacher change (no common teachers)", id);
        }
        
        CurricularUnit curricularUnit = curricularUnitRepository.findById(request.getCurricularUnitId())
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.curricular-unit.not-found")
            ));
        
        // Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException(
                messageService.getMessage("error.course.start-not-before-end")
            );
        }
        
        // Validate and update teachers
        Long programId = curricularUnit.getTerm().getProgram().getId();
        List<Campus> programCampuses = campusRepository.findByProgram(programId);
        
        if (programCampuses.isEmpty()) {
            throw new IllegalStateException(
                messageService.getMessage("error.course.program-no-campuses")
            );
        }
        
        List<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .toList();
        
        log.debug("Validating teachers for program offered at {} campus(es): {}", programCampuses.size(), programCampusIds);
        
        List<Teacher> teachers = new ArrayList<>();
        for (Long userId : request.getUserIds()) {
            User user = userRepository.findByIdWithPositions(userId)
                .orElseThrow(() -> new ResourceNotFoundException(messageService.getMessage("error.user.not-found")));
            
            Teacher teacher = user.getPositions().stream()
                .filter(pos -> pos instanceof Teacher)
                .map(pos -> (Teacher) pos)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(messageService.getMessage("error.course.user-not-teacher")));
            
            boolean teacherBelongsToValidCampus = teacher.getCampuses().stream()
                .anyMatch(campus -> programCampusIds.contains(campus.getId()));
            
            if (!teacherBelongsToValidCampus) {
                throw new IllegalArgumentException(
                    String.format("Teacher with id %d does not belong to any campus where this program is offered. Required campuses: %s", 
                    userId, programCampusIds)
                );
            }
            
            teachers.add(teacher);
        }
        
        log.debug("Validated {} teacher(s) for course update", teachers.size());
        
        // If multiple teachers, validate they share at least one common campus within the program's campuses
        if (teachers.size() > 1) {
            Set<Long> commonCampusIds = teachers.get(0).getCampuses().stream()
                .map(Campus::getId)
                .filter(programCampusIds::contains)
                .collect(Collectors.toSet());
            
            for (int i = 1; i < teachers.size(); i++) {
                Set<Long> teacherCampusIds = teachers.get(i).getCampuses().stream()
                    .map(Campus::getId)
                    .filter(programCampusIds::contains)
                    .collect(Collectors.toSet());
                
                commonCampusIds.retainAll(teacherCampusIds);
            }
            
            if (commonCampusIds.isEmpty()) {
                throw new IllegalArgumentException(
                    messageService.getMessage("error.course.teachers-no-common-campus")
                );
            }
            
            log.debug("Teachers share {} common campus(es): {}", commonCampusIds.size(), commonCampusIds);
        }
        
        // Clear planning if all teachers were replaced
        if (shouldClearPlanning) {
            clearCoursePlanning(course);
        }
        
        // Update fields
        course.setShift(request.getShift());
        course.setDescription(request.getDescription());
        course.setStartDate(request.getStartDate());
        course.setEndDate(request.getEndDate());
        course.setPartialGradingSystem(request.getPartialGradingSystem());
        course.setIsRelatedToInvestigation(request.getIsRelatedToInvestigation());
        course.setInvolvesActivitiesWithProductiveSector(request.getInvolvesActivitiesWithProductiveSector());
        course.setCurricularUnit(curricularUnit);
        
        // Update teachers
        course.getTeachers().clear();
        course.getTeachers().addAll(teachers);
        
        // Update collections
        course.getHoursPerDeliveryFormat().clear();
        if (request.getHoursPerDeliveryFormat() != null) {
            // Validar que todas las horas sean no negativas
            for (Map.Entry<DeliveryFormat, Integer> entry : request.getHoursPerDeliveryFormat().entrySet()) {
                if (entry.getValue() != null && entry.getValue() < 0) {
                    throw new IllegalArgumentException(
                        messageService.getMessage("error.course.invalid-hours-format")
                    );
                }
            }
            course.getHoursPerDeliveryFormat().putAll(request.getHoursPerDeliveryFormat());
        }
        
        course.getSustainableDevelopmentGoals().clear();
        if (request.getSustainableDevelopmentGoals() != null) {
            course.getSustainableDevelopmentGoals().addAll(request.getSustainableDevelopmentGoals());
        }
        
        course.getUniversalDesignLearningPrinciples().clear();
        if (request.getUniversalDesignLearningPrinciples() != null) {
            course.getUniversalDesignLearningPrinciples().addAll(request.getUniversalDesignLearningPrinciples());
        }
        
        // Adjust WeeklyPlannings if dates changed (preserves existing content)
        adjustWeeklyPlanningsIfNeeded(course, request.getStartDate(), request.getEndDate(), request.getUserIds());
        
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Course updated successfully with id: {} and {} teacher(s)", id, teachers.size());
        
        return courseMapper.toResponse(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        log.debug("Deleting course with id: {}", id);
        
        // Validate delete access (only ANALYST/COORDINATOR can delete courses)
        accessControlService.validateCourseDeleteAccess(id);

        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.course.not-found")
            ));
        
        // Validate that the course doesn't have OfficeHours
        if (course.getOfficeHours() != null && !course.getOfficeHours().isEmpty()) {
            throw new IllegalStateException(
                messageService.getMessage("error.course.has-office-hours")
            );
        }
        
        // Validate that the course doesn't have Modifications
        if (course.getModifications() != null && !course.getModifications().isEmpty()) {
            throw new IllegalStateException(
                messageService.getMessage("error.course.has-modifications")
            );
        }
        
        // WeeklyPlannings will be automatically deleted due to CascadeType.ALL and orphanRemoval
        courseRepository.deleteById(id);
        
        log.info("Course deleted successfully with id: {} (along with {} weekly plannings)", 
            id, course.getWeeklyPlannings() != null ? course.getWeeklyPlannings().size() : 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseBasicResponse> getCourses(Long userId, Long campusId, String period, String searchText, Pageable pageable) {
        log.debug("Getting courses - userId: {}, campusId: {}, period: {}, searchText: {}, page: {}, size: {}", 
            userId, campusId, period, searchText, pageable.getPageNumber(), pageable.getPageSize()
        );

        Page<Course> coursesPage = courseRepository.findAll(
            CourseSpecification.withFilters(userId, campusId, period, searchText),
            pageable
        );

        log.debug("Found {} courses (page {} of {})", 
            coursesPage.getNumberOfElements(), 
            coursesPage.getNumber() + 1, 
            coursesPage.getTotalPages());

        return coursesPage.map(courseMapper::toBasicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodResponse> getPeriods(Long campusId, Long userId) {
        List<Course> courses = courseRepository.findAll(
            CourseSpecification.withFilters(userId, campusId, null, null)
        );

        List<PeriodResponse> periods = courses.stream()
            .map(Course::getPeriod)
            .filter(Objects::nonNull)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .map(period -> PeriodResponse.builder().period(period).build())
            .toList();

        log.debug("Found {} unique periods for campus {} and userId {}", periods.size(), campusId, userId);

        return periods;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseBriefResponse> getCoursesForCurrentUserInCampus(Long campusId, Long courseId) {

        accessControlService.validateCampusAccess(campusId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        List<Course> courses = courseRepository.findAll(
            CourseSpecification.withFilters(currentUser.getId(), campusId, null, null)
        );

        // Exclude courses whose endDate is after today
        LocalDate today = LocalDate.now();
        List<Course> visibleCourses = courses.stream()
            .filter(c -> c.getEndDate() == null || !c.getEndDate().isAfter(today))
            .toList();

        List<CourseBriefResponse> brief = visibleCourses.stream()
            .map(course -> CourseBriefResponse.builder()
                .id(course.getId())
                .curricularUnitName(course.getCurricularUnit() != null ? course.getCurricularUnit().getName() : null)
                .startDate(course.getStartDate())
                .shift(course.getShift())
                .build())
            .toList();

        if (courseId != null) {
            boolean found = brief.stream().anyMatch(b -> b.getId().equals(courseId));
            if (!found) {
                log.warn("Requested course {} is not accessible by current user in campus {}", courseId, campusId);
                boolean exists = courseRepository.existsById(courseId);
                if (!exists) {
                    throw new ResourceNotFoundException(
                        messageService.getMessage("error.course.no-course-found")
                    );
                }
                throw new ForbiddenException(
                    messageService.getMessage("error.course.no-access-in-campus")
                );
            }
            return brief.stream().filter(b -> b.getId().equals(courseId)).toList();
        }

        return brief;
    }

    // ==================== Sustainable Development Goals (ODS) ====================

    @Override
    @Transactional
    public CourseResponse addSustainableDevelopmentGoal(Long courseId, SustainableDevelopmentGoal goal) {
        log.debug("Adding Sustainable Development Goal {} to course {}", goal, courseId);
        
        // Validate planning management access to course
        accessControlService.validateCoursePlanningManagement(courseId);

        Course course = courseRepository.findByIdWithWeeklyPlannings(courseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.course.not-found", courseId)
            ));
        
        course.getSustainableDevelopmentGoals().add(goal);
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Sustainable Development Goal {} added to course {}", goal, courseId);
        
        return courseMapper.toResponse(updatedCourse);
    }

    @Override
    @Transactional
    public CourseResponse removeSustainableDevelopmentGoal(Long courseId, SustainableDevelopmentGoal goal) {
        log.debug("Removing Sustainable Development Goal {} from course {}", goal, courseId);
        
        // Validate planning management access to course
        accessControlService.validateCoursePlanningManagement(courseId);

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.course.not-found")
            ));
        
        boolean removed = course.getSustainableDevelopmentGoals().remove(goal);
        
        if (!removed) {
            log.warn("Sustainable Development Goal {} was not found in course {}", goal, courseId);
        }
        
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Sustainable Development Goal {} removed from course {}", goal, courseId);
        
        return courseMapper.toResponse(updatedCourse);
    }

    // ==================== Universal Design Learning Principles ====================

    @Override
    @Transactional
    public CourseResponse addUniversalDesignLearningPrinciple(Long courseId, UniversalDesignLearningPrinciple principle) {
        log.debug("Adding Universal Design Learning Principle {} to course {}", principle, courseId);
        
        // Validate planning management access to course
        accessControlService.validateCoursePlanningManagement(courseId);

        Course course = courseRepository.findByIdWithWeeklyPlannings(courseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.course.not-found")
            ));
        
        course.getUniversalDesignLearningPrinciples().add(principle);
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Universal Design Learning Principle {} added to course {}", principle, courseId);
        
        return courseMapper.toResponse(updatedCourse);
    }

    @Override
    @Transactional
    public CourseResponse removeUniversalDesignLearningPrinciple(Long courseId, UniversalDesignLearningPrinciple principle) {
        log.debug("Removing Universal Design Learning Principle {} from course {}", principle, courseId);
        
        // Validate planning management access to course
        accessControlService.validateCoursePlanningManagement(courseId);

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException(messageService.getMessage("error.course.not-found")));
        
        boolean removed = course.getUniversalDesignLearningPrinciples().remove(principle);
        
        if (!removed) {
            log.warn("Universal Design Learning Principle {} was not found in course {}", principle, courseId);
        }
        
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Universal Design Learning Principle {} removed from course {}", principle, courseId);
        
        return courseMapper.toResponse(updatedCourse);
    }

    // ==================== PDF Data Export ====================

    @Override
    @Transactional(readOnly = true)
    public CoursePdfDataResponse getCoursePdfData(Long courseId) {
        log.debug("Getting PDF data for course {}", courseId);

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException(messageService.getMessage("error.course.not-found")));

        // Build teacher info list
        List<CoursePdfDataResponse.TeacherInfo> teacherInfoList = course.getTeachers().stream()
            .map(teacher -> {
                User user = teacher.getUser();
                return CoursePdfDataResponse.TeacherInfo.builder()
                    .name(user.getPersonalData() != null ? user.getPersonalData().getName() : null)
                    .lastName(user.getPersonalData() != null ? user.getPersonalData().getLastName() : null)
                    .email(user.getUtecEmail())
                    .build();
            })
            .toList();

        // Build curricular unit info
        CurricularUnit curricularUnit = course.getCurricularUnit();
        CoursePdfDataResponse.CurricularUnitInfo curricularUnitInfo = CoursePdfDataResponse.CurricularUnitInfo.builder()
            .name(curricularUnit.getName())
            .credits(curricularUnit.getCredits())
            .build();

        // Get program name through curricular unit -> term -> program
        String programName = curricularUnit.getTerm() != null && curricularUnit.getTerm().getProgram() != null
            ? curricularUnit.getTerm().getProgram().getName()
            : null;

        // Build weekly planning info list
        List<CoursePdfDataResponse.WeeklyPlanningInfo> weeklyPlanningInfoList = course.getWeeklyPlannings().stream()
            .map(weeklyPlanning -> {
                // Extract content titles from programmatic contents
                List<String> contentTitles = weeklyPlanning.getProgrammaticContents().stream()
                    .map(content -> content.getTitle())
                    .toList();

                return CoursePdfDataResponse.WeeklyPlanningInfo.builder()
                    .weekNumber(weeklyPlanning.getWeekNumber())
                    .startDate(weeklyPlanning.getStartDate())
                    .endDate(weeklyPlanning.getEndDate())
                    .contentTitles(contentTitles)
                    .bibliographicReferences(weeklyPlanning.getBibliographicReferences())
                    .build();
            })
            .toList();

        // Collect all bibliographic references from all weekly plannings
        List<String> allBibliography = course.getWeeklyPlannings().stream()
            .flatMap(weeklyPlanning -> weeklyPlanning.getBibliographicReferences().stream())
            .distinct()
            .toList();

        // Build response
        CoursePdfDataResponse response = CoursePdfDataResponse.builder()
            .description(course.getDescription())
            .startDate(course.getStartDate())
            .endDate(course.getEndDate())
            .shift(course.getShift())
            .involvesActivitiesWithProductiveSector(course.getInvolvesActivitiesWithProductiveSector())
            .partialGradingSystem(course.getPartialGradingSystem())
            .isRelatedToInvestigation(course.getIsRelatedToInvestigation())
            .hoursPerDeliveryFormat(course.getHoursPerDeliveryFormat())
            .teachers(teacherInfoList)
            .programName(programName)
            .curricularUnit(curricularUnitInfo)
            .weeklyPlannings(weeklyPlanningInfoList)
            .bibliography(allBibliography)
            .build();

        log.info("PDF data retrieved successfully for course {}", courseId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CourseStatisticsDto getCourseStatistics(Long courseId) {
        log.debug("Retrieving statistics for course {}", courseId);

        accessControlService.validateCourseAccess(courseId);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException(messageService.getMessage("error.course.not-found")));
        
        CourseStatisticsDto statistics = courseStatisticsMapper.calculateStatistics(course);
        
        log.info("Statistics retrieved for course {}", courseId);
        
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherCourseResponse> getTeacherCoursesByCurricularUnit(Long teacherId, Long curricularUnitId) {
        log.debug("Getting courses for teacher {} and curricular unit {}", teacherId, curricularUnitId);

        // Validate that the teacher exists
        User teacherUser = userRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException(messageService.getMessage("error.user.not-found")));

        // Validate that the curricular unit exists
        if (!curricularUnitRepository.existsById(curricularUnitId)) {
            throw new ResourceNotFoundException(messageService.getMessage("error.curricular-unit.not-found"));
        }

        // Find the teacher position
        Teacher teacher = (Teacher) teacherUser.getPositions().stream()
            .filter(p -> p instanceof Teacher)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                messageService.getMessage("error.course.user-not-teacher")
            ));

        // Get all courses where this teacher is assigned AND the curricular unit matches
        List<Course> courses = courseRepository.findAll(
            CourseSpecification.withFilters(teacherId, null, null, null)
        ).stream()
            .filter(course -> course.getCurricularUnit().getId().equals(curricularUnitId))
            .toList();

        // Build response with formatted display names
        List<TeacherCourseResponse> response = courses.stream()
            .flatMap(course -> {
                if (course.getCurricularUnit().getTerm() == null || course.getCurricularUnit().getTerm().getProgram() == null) {
                    return java.util.stream.Stream.empty();
                }

                Long programId = course.getCurricularUnit().getTerm().getProgram().getId();
                List<Campus> programCampuses = campusRepository.findByProgram(programId);

                // Filter teacher's campuses that offer this program
                return teacher.getCampuses().stream()
                    .filter(campus -> programCampuses.stream()
                        .anyMatch(pc -> pc.getId().equals(campus.getId())))
                    .map(campus -> {
                        String curricularUnitName = course.getCurricularUnit().getName();
                        String period = course.getPeriod();
                        String campusName = campus.getName();
                        String displayName = String.format("%s - %s - %s", curricularUnitName, period, campusName);

                        return TeacherCourseResponse.builder()
                            .teacherId(teacherId)
                            .courseId(course.getId())
                            .displayName(displayName)
                            .curricularUnitName(curricularUnitName)
                            .period(period)
                            .campusName(campusName)
                            .build();
                    });
            })
            .sorted((a, b) -> a.getDisplayName().compareTo(b.getDisplayName()))
            .toList();

        log.info("Found {} courses for teacher {} and curricular unit {}", response.size(), teacherId, curricularUnitId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailedInfoResponse getCourseDetailedInfo(Long courseId) {
        log.debug("Getting detailed info for course {}", courseId);

        // Find course with necessary relationships
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException(messageService.getMessage("error.course.not-found", courseId)));

        // Get curricular unit
        CurricularUnit curricularUnit = course.getCurricularUnit();
        
        // Get term and program
        String programName = curricularUnit.getTerm() != null && curricularUnit.getTerm().getProgram() != null
            ? curricularUnit.getTerm().getProgram().getName()
            : null;
        
        Integer semesterNumber = curricularUnit.getTerm() != null 
            ? curricularUnit.getTerm().getNumber()
            : null;

        // Get teachers information
        List<CourseDetailedInfoResponse.TeacherInfo> teachersInfo = 
            course.getTeachers().stream()
                .map(teacher -> {
                    User user = teacher.getUser();
                    return CourseDetailedInfoResponse.TeacherInfo.builder()
                        .name(user.getPersonalData() != null ? user.getPersonalData().getName() : null)
                        .lastName(user.getPersonalData() != null ? user.getPersonalData().getLastName() : null)
                        .email(user.getUtecEmail())
                        .build();
                })
                .toList();

        // Convert domain areas to display values
        List<String> domainAreasDisplay = curricularUnit.getDomainAreas().stream()
            .map(area -> area.getDisplayValue())
            .toList();

        // Convert professional competencies to display values
        List<String> professionalCompetenciesDisplay = curricularUnit.getProfessionalCompetencies().stream()
            .map(competency -> competency.getDisplayValue())
            .toList();

        // Build response
        CourseDetailedInfoResponse response = 
            CourseDetailedInfoResponse.builder()
                .courseId(course.getId())
                .programName(programName)
                .curricularUnitName(curricularUnit.getName())
                .teachers(teachersInfo)
                .credits(curricularUnit.getCredits())
                .semesterNumber(semesterNumber)
                .domainAreas(domainAreasDisplay)
                .professionalCompetencies(professionalCompetenciesDisplay)
                .build();

        log.info("Retrieved detailed info for course {}", courseId);

        return response;
    }

    @Override
    @Transactional
    public CourseResponse copyPlanningFromSourceCourse(Long targetCourseId, Long sourceCourseId) {
        log.info("Copying planning from source course {} to target course {}", sourceCourseId, targetCourseId);
        
        // Validate that both courses exist
        Course targetCourse = courseRepository.findByIdWithWeeklyPlannings(targetCourseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.course.not-found")
            ));
        
        Course sourceCourse = courseRepository.findByIdWithWeeklyPlannings(sourceCourseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.course.not-found")
            ));
        
        // Validate that the current user is a teacher of the target course
        accessControlService.validateCourseAccess(targetCourseId);
        
        // Validate that the target course has not finished
        accessControlService.validateCourseNotExpired(targetCourseId);
        
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUtecEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("error.user.current-not-found")));
        
        // Validate that the user is a teacher of the target course
        boolean isTeacherOfTargetCourse = targetCourse.getTeachers().stream()
            .anyMatch(teacher -> teacher.getUser().getId().equals(currentUser.getId()));
        
        if (!isTeacherOfTargetCourse) {
            throw new ForbiddenException(
                messageService.getMessage("error.course.user-not-teacher-of-course")
            );
        }
        
        // Validate that both courses have the same curricular unit
        if (!targetCourse.getCurricularUnit().getId().equals(sourceCourse.getCurricularUnit().getId())) {
            throw new IllegalArgumentException(
                "Cannot copy planning from course with different curricular unit. Source: " + 
                sourceCourse.getCurricularUnit().getName() + ", Target: " + 
                targetCourse.getCurricularUnit().getName()
            );
        }
        
        // Load programmatic contents and activities for source course
        courseRepository.loadProgrammaticContents(sourceCourseId);
        courseRepository.loadProgrammaticContentActivities(sourceCourseId);
        
        // Refresh source course to get loaded collections
        sourceCourse = courseRepository.findByIdWithWeeklyPlannings(sourceCourseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.course.not-found")
            ));
        
        // Clear existing planning from target course
        log.debug("Clearing existing planning from target course {}", targetCourseId);
        for (WeeklyPlanning targetWeekly : targetCourse.getWeeklyPlannings()) {
            targetWeekly.getBibliographicReferences().clear();
            targetWeekly.getProgrammaticContents().clear(); // orphanRemoval will delete activities too
        }
        
        // Map source weekly plannings by week number
        java.util.Map<Integer, WeeklyPlanning> sourceWeeklyPlanningMap = sourceCourse.getWeeklyPlannings().stream()
            .collect(java.util.stream.Collectors.toMap(
                WeeklyPlanning::getWeekNumber,
                wp -> wp
            ));
        
        int copiedWeeks = 0;
        int copiedContents = 0;
        int copiedActivities = 0;
        
        // Copy planning to target course
        for (WeeklyPlanning targetWeekly : targetCourse.getWeeklyPlannings()) {
            Integer weekNumber = targetWeekly.getWeekNumber();
            WeeklyPlanning sourceWeekly = sourceWeeklyPlanningMap.get(weekNumber);
            
            if (sourceWeekly == null) {
                log.debug("No source planning found for week {} in source course, skipping", weekNumber);
                continue;
            }
            
            // Copy bibliographic references
            targetWeekly.getBibliographicReferences().addAll(
                sourceWeekly.getBibliographicReferences()
            );
            
            // Copy ProgrammaticContents
            for (ProgrammaticContent sourceContent : sourceWeekly.getProgrammaticContents()) {
                ProgrammaticContent targetContent = new ProgrammaticContent(
                    sourceContent.getTitle(),
                    sourceContent.getContent(),
                    targetWeekly
                );
                targetContent.setColor(sourceContent.getColor());
                targetWeekly.getProgrammaticContents().add(targetContent);
                copiedContents++;
                
                // Copy Activities
                for (Activity sourceActivity : sourceContent.getActivities()) {
                    Activity targetActivity = new Activity(
                        sourceActivity.getDescription(),
                        sourceActivity.getDurationInMinutes(),
                        sourceActivity.getLearningModality(),
                        targetContent
                    );
                    targetActivity.setTitle(sourceActivity.getTitle());
                    targetActivity.setColor(sourceActivity.getColor());
                    targetActivity.getCognitiveProcesses().addAll(sourceActivity.getCognitiveProcesses());
                    targetActivity.getTransversalCompetencies().addAll(sourceActivity.getTransversalCompetencies());
                    targetActivity.getTeachingStrategies().addAll(sourceActivity.getTeachingStrategies());
                    targetActivity.getLearningResources().addAll(sourceActivity.getLearningResources());
                    
                    targetContent.getActivities().add(targetActivity);
                    copiedActivities++;
                }
            }
            
            copiedWeeks++;
        }
        
        // Save target course with copied planning
        Course updatedCourse = courseRepository.save(targetCourse);
        
        log.info("Planning copy completed successfully: {} weeks, {} programmatic contents, {} activities copied from course {} to course {}",
            copiedWeeks, copiedContents, copiedActivities, sourceCourseId, targetCourseId);
        
        return courseMapper.toResponse(updatedCourse);
    }
}
