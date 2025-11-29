package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.aiagent.AIReportRequest.CourseStatisticsDto;
import edu.utec.planificador.dto.request.CourseRequest;
import edu.utec.planificador.dto.response.CourseBasicResponse;
import edu.utec.planificador.dto.response.CourseBriefResponse;
import edu.utec.planificador.dto.response.CoursePdfDataResponse;
import edu.utec.planificador.dto.response.CourseResponse;
import edu.utec.planificador.dto.response.PeriodResponse;
import edu.utec.planificador.entity.Activity;
import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.CurricularUnit;
import edu.utec.planificador.entity.ProgrammaticContent;
import edu.utec.planificador.entity.Teacher;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.entity.WeeklyPlanning;
import edu.utec.planificador.enumeration.DeliveryFormat;
import edu.utec.planificador.enumeration.PartialGradingSystem;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.mapper.CourseMapper;
import edu.utec.planificador.mapper.CourseStatisticsMapper;
import edu.utec.planificador.repository.CampusRepository;
import edu.utec.planificador.repository.CourseRepository;
import edu.utec.planificador.repository.CurricularUnitRepository;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.service.AccessControlService;
import edu.utec.planificador.service.CourseService;
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

    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        log.debug("Creating course for curricular unit: {}", request.getCurricularUnitId());
        
        // Validate startDate <= endDate
        if (request.getStartDate() != null && request.getEndDate() != null && 
            request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        accessControlService.validateCurricularUnitAccess(request.getCurricularUnitId());

        CurricularUnit curricularUnit = curricularUnitRepository.findById(request.getCurricularUnitId())
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + request.getCurricularUnitId()));
        
        Long programId = curricularUnit.getTerm().getProgram().getId();
        List<Campus> programCampuses = campusRepository.findByProgram(programId);
        
        if (programCampuses.isEmpty()) {
            throw new IllegalStateException("Program with id " + programId + " is not offered at any campus");
        }
        
        List<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .toList();
        
        log.debug("Curricular unit's program is offered at {} campus(es): {}", programCampuses.size(), programCampusIds);
        
        List<Teacher> teachers = new ArrayList<>();
        for (Long userId : request.getUserIds()) {
            User user = userRepository.findByIdWithPositions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            
            Teacher teacher = user.getPositions().stream()
                .filter(pos -> pos instanceof Teacher)
                .map(pos -> (Teacher) pos)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User with id " + userId + " does not have a teacher position"));
            
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
                    "All teachers must share at least one common campus where the program is offered. " +
                    "Teachers provided do not have any campus in common."
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
                        String.format("Las horas para el formato %s no pueden ser negativas. Valor recibido: %d",
                            entry.getKey(), entry.getValue())
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
        log.info("Generated {} weekly planning(s) for course", weeklyPlannings.size());
        
        // Primero guardar el curso para obtener su ID y persistir los WeeklyPlannings vacíos
        Course savedCourse = courseRepository.save(course);
        log.info("Course created with id: {}", savedCourse.getId());
        
        // Ahora buscar el último curso DIFERENTE del actual para copiar su planificación
        // Considerar TODOS los docentes asignados al curso para encontrar el curso más reciente
        // que comparta al menos uno de ellos
        Optional<Course> previousCourseOpt = courseRepository.findLatestByCurricularUnitAndUsers(
            request.getCurricularUnitId(), 
            request.getUserIds(),
            savedCourse.getId()
        );
        
        if (previousCourseOpt.isPresent()) {
            Course previousCourse = previousCourseOpt.get();
            log.debug("Found previous course with id: {} for copying planning", previousCourse.getId());
            
            // Copiar la planificación del curso anterior al curso recién creado
            copyPlanningFromPreviousCourse(previousCourse.getId(), savedCourse);
            
            // Guardar nuevamente para persistir la planificación copiada
            savedCourse = courseRepository.save(savedCourse);
            log.info("Planning copied and persisted for course {}", savedCourse.getId());
        } else {
            log.debug("No previous course found for teachers {} and curricular unit {}, course has empty plannings",
                request.getUserIds(), request.getCurricularUnitId());
        }
        
        log.info("Course created successfully with id: {} and {} teacher(s)", savedCourse.getId(), teachers.size());
        
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
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
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
     * Copia la planificación (WeeklyPlanning, ProgrammaticContent y Activity) de un curso anterior a un nuevo curso.
     * 
     * Mapea las semanas del curso anterior a las del nuevo curso basándose en el número de semana,
     * creando copias de todos los ProgrammaticContent y Activities asociados.
     * 
     * @param sourceCourseId ID del curso del cual copiar la planificación
     * @param targetCourse Curso al cual copiar la planificación
     */
    private void copyPlanningFromPreviousCourse(Long sourceCourseId, Course targetCourse) {
        log.debug("Copying planning from course {} to new course", sourceCourseId);
        
        // Cargar el curso fuente con todos sus detalles
        Optional<Course> sourceCourseOpt = courseRepository.findByIdWithWeeklyPlannings(sourceCourseId);
        if (sourceCourseOpt.isEmpty()) {
            log.warn("Source course {} not found, skipping planning copy", sourceCourseId);
            return;
        }
        
        // Cargar programmatic contents y activities en queries separadas para evitar MultipleBagFetchException
        courseRepository.loadProgrammaticContents(sourceCourseId);
        courseRepository.loadProgrammaticContentActivities(sourceCourseId);
        
        Course sourceCourse = sourceCourseOpt.get();
        
        // Mapear WeeklyPlannings del curso fuente por número de semana
        java.util.Map<Integer, WeeklyPlanning> sourceWeeklyPlanningMap = sourceCourse.getWeeklyPlannings().stream()
            .collect(java.util.stream.Collectors.toMap(
                WeeklyPlanning::getWeekNumber,
                wp -> wp
            ));
        
        int copiedWeeks = 0;
        int copiedContents = 0;
        int copiedActivities = 0;
        
        // Iterar sobre los WeeklyPlannings del nuevo curso y copiar contenido del curso fuente
        for (WeeklyPlanning targetWeeklyPlanning : targetCourse.getWeeklyPlannings()) {
            Integer weekNumber = targetWeeklyPlanning.getWeekNumber();
            WeeklyPlanning sourceWeeklyPlanning = sourceWeeklyPlanningMap.get(weekNumber);
            
            if (sourceWeeklyPlanning == null) {
                log.debug("No source planning found for week {}, skipping", weekNumber);
                continue;
            }
            
            // Copiar referencias bibliográficas
            targetWeeklyPlanning.getBibliographicReferences().addAll(
                sourceWeeklyPlanning.getBibliographicReferences()
            );
            
            // Copiar ProgrammaticContents
            for (ProgrammaticContent sourceProgrammaticContent : sourceWeeklyPlanning.getProgrammaticContents()) {
                ProgrammaticContent targetProgrammaticContent = new ProgrammaticContent(
                    sourceProgrammaticContent.getTitle(),
                    sourceProgrammaticContent.getContent(),
                    targetWeeklyPlanning
                );
                
                targetProgrammaticContent.setColor(sourceProgrammaticContent.getColor());
                targetWeeklyPlanning.getProgrammaticContents().add(targetProgrammaticContent);
                copiedContents++;
                
                // Copiar Activities del ProgrammaticContent
                for (Activity sourceActivity : sourceProgrammaticContent.getActivities()) {
                    Activity targetActivity = new Activity(
                        sourceActivity.getDescription(),
                        sourceActivity.getDurationInMinutes(),
                        sourceActivity.getLearningModality(),
                        targetProgrammaticContent
                    );
                    
                    targetActivity.setTitle(sourceActivity.getTitle());
                    targetActivity.setColor(sourceActivity.getColor());
                    targetActivity.getCognitiveProcesses().addAll(sourceActivity.getCognitiveProcesses());
                    targetActivity.getTransversalCompetencies().addAll(sourceActivity.getTransversalCompetencies());
                    targetActivity.getTeachingStrategies().addAll(sourceActivity.getTeachingStrategies());
                    targetActivity.getLearningResources().addAll(sourceActivity.getLearningResources());
                    
                    targetProgrammaticContent.getActivities().add(targetActivity);
                    copiedActivities++;
                }
            }
            
            copiedWeeks++;
        }
        
        log.info("Planning copy completed: {} weeks, {} programmatic contents, {} activities copied from course {} to new course",
            copiedWeeks, copiedContents, copiedActivities, sourceCourseId);
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
        
        List<edu.utec.planificador.entity.OfficeHours> officeHoursToRemove = course.getOfficeHours().stream()
            .filter(oh -> oh.getDate().isBefore(newStartDate) || oh.getDate().isAfter(newEndDate))
            .toList();
        
        if (!officeHoursToRemove.isEmpty()) {
            log.info("Removing {} office hour(s) that fall outside new course date range [{} to {}]", 
                officeHoursToRemove.size(), newStartDate, newEndDate);
            
            for (edu.utec.planificador.entity.OfficeHours oh : officeHoursToRemove) {
                log.debug("Removing office hour on {} (outside new range)", oh.getDate());
            }
            
            course.getOfficeHours().removeAll(officeHoursToRemove);
        }
    }

    /**
     * Reemplaza completamente la planificación de un curso cuando cambian los docentes o la unidad curricular.
     * 
     * Elimina todos los WeeklyPlannings existentes (con sus ProgrammaticContents y Activities)
     * y crea nuevos basados en las fechas actuales del curso, intentando copiar la planificación
     * del curso más reciente de los nuevos docentes y unidad curricular.
     * 
     * @param course Curso cuya planificación se va a reemplazar
     * @param newTeacherIds IDs de los nuevos docentes
     * @param newCurricularUnitId ID de la nueva unidad curricular
     */
    private void resetAndCopyPlanningFromLatest(Course course, List<Long> newTeacherIds, Long newCurricularUnitId) {
        log.info("Resetting and replacing planning for course {}", course.getId());
        
        // Eliminar todos los WeeklyPlannings existentes (orphanRemoval se encargará del resto)
        int removedWeeks = course.getWeeklyPlannings().size();
        course.getWeeklyPlannings().clear();
        log.debug("Removed {} existing weekly planning(s)", removedWeeks);
        
        // Generar nuevos WeeklyPlannings vacíos basados en las fechas del curso
        List<WeeklyPlanning> newWeeklyPlannings = WeeklyPlanningGenerator.generateWeeklyPlannings(
            course.getStartDate(),
            course.getEndDate()
        );
        course.getWeeklyPlannings().addAll(newWeeklyPlannings);
        log.info("Generated {} new empty weekly planning(s)", newWeeklyPlannings.size());
        
        // Buscar curso anterior con la nueva unidad curricular y nuevos docentes
        Optional<Course> previousCourseOpt = courseRepository.findLatestByCurricularUnitAndUsers(
            newCurricularUnitId,
            newTeacherIds,
            course.getId()
        );
        
        if (previousCourseOpt.isPresent()) {
            Course previousCourse = previousCourseOpt.get();
            log.info("Found previous course {} for new configuration, copying planning", previousCourse.getId());
            
            // Cargar los detalles del curso fuente
            courseRepository.findByIdWithWeeklyPlannings(previousCourse.getId());
            courseRepository.loadProgrammaticContents(previousCourse.getId());
            courseRepository.loadProgrammaticContentActivities(previousCourse.getId());
            
            // Refrescar el curso fuente para obtener las colecciones cargadas
            previousCourse = courseRepository.findByIdWithWeeklyPlannings(previousCourse.getId()).orElse(null);
            
            if (previousCourse != null) {
                // Mapear WeeklyPlannings del curso fuente por número de semana
                java.util.Map<Integer, WeeklyPlanning> sourceWeeklyPlanningMap = previousCourse.getWeeklyPlannings().stream()
                    .collect(java.util.stream.Collectors.toMap(
                        WeeklyPlanning::getWeekNumber,
                        wp -> wp
                    ));
                
                int copiedWeeks = 0;
                int copiedContents = 0;
                int copiedActivities = 0;
                
                // Copiar contenido a los nuevos WeeklyPlannings
                for (WeeklyPlanning targetWeekly : course.getWeeklyPlannings()) {
                    WeeklyPlanning sourceWeekly = sourceWeeklyPlanningMap.get(targetWeekly.getWeekNumber());
                    
                    if (sourceWeekly != null) {
                        // Copiar referencias bibliográficas
                        targetWeekly.getBibliographicReferences().addAll(sourceWeekly.getBibliographicReferences());
                        
                        // Copiar ProgrammaticContents y Activities
                        for (ProgrammaticContent sourceContent : sourceWeekly.getProgrammaticContents()) {
                            ProgrammaticContent newContent = new ProgrammaticContent(
                                sourceContent.getTitle(),
                                sourceContent.getContent(),
                                targetWeekly
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
                                copiedActivities++;
                            }
                            
                            targetWeekly.getProgrammaticContents().add(newContent);
                            copiedContents++;
                        }
                        
                        copiedWeeks++;
                    }
                }
                
                log.info("Planning replacement completed: {} weeks, {} contents, {} activities copied from course {}", 
                    copiedWeeks, copiedContents, copiedActivities, previousCourse.getId());
            }
        } else {
            log.info("No previous course found for new configuration (CU: {}, Teachers: {}), course has empty planning", 
                newCurricularUnitId, newTeacherIds);
        }
    }

    /**
     * Valida que un docente no intente modificar la unidad curricular o la lista de docentes
     * de un curso al actualizarlo.
     * 
     * @param course Curso que se está actualizando
     * @param request Datos de la actualización del curso
     */
    private void validateTeacherDoesntModifyTeachersListOrCurricularUnit(Course course, CourseRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUtecEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        boolean isTeacherOfCourse = course.getTeachers().stream()
            .anyMatch(teacher -> teacher.getUser().getId().equals(currentUser.getId()));
        
        if (isTeacherOfCourse) {
            // Check if teacher is trying to change curricularUnit
            if (!course.getCurricularUnit().getId().equals(request.getCurricularUnitId())) {
                log.warn("Teacher {} attempted to change curricularUnit for course {}", 
                    currentUser.getUtecEmail(), course.getId());
                throw new IllegalArgumentException("Teachers cannot modify the curricular unit of a course");
            }
            
            // Check if teacher is trying to change the teachers list
            Set<Long> currentTeacherIds = course.getTeachers().stream()
                .map(teacher -> teacher.getUser().getId())
                .collect(Collectors.toSet());
            Set<Long> requestedTeacherIds = new HashSet<>(request.getUserIds());
            
            if (!currentTeacherIds.equals(requestedTeacherIds)) {
                log.warn("Teacher {} attempted to modify teachers list for course {}", 
                    currentUser.getUtecEmail(), course.getId());
                throw new IllegalArgumentException("Teachers cannot modify the assigned teachers of a course");
            }
            
            log.debug("Teacher {} validation passed for course {} update", 
                currentUser.getUtecEmail(), course.getId());
        }
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        log.debug("Updating course with id: {}", id);
        
        // Validate update access (ANALYST/COORDINATOR in campus OR teacher of the course)
        accessControlService.validateCourseUpdateAccess(id);
        
        accessControlService.validateCurricularUnitAccess(request.getCurricularUnitId());

        Course course = courseRepository.findByIdWithWeeklyPlannings(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
        // Additional validation: Teachers cannot modify curricularUnit or teachers list
        validateTeacherDoesntModifyTeachersListOrCurricularUnit(course, request);
        
        // Validate that the course has not finished
        if (course.getEndDate() != null && course.getEndDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot modify a course that has already finished");
        }
        
        // CRITICAL: Detect changes BEFORE modifying the course entity
        boolean curricularUnitChanged = !course.getCurricularUnit().getId().equals(request.getCurricularUnitId());
        
        Set<Long> originalTeacherIds = course.getTeachers().stream()
            .map(teacher -> teacher.getUser().getId())
            .collect(Collectors.toSet());
        Set<Long> newTeacherIds = new HashSet<>(request.getUserIds());
        boolean teachersChanged = !originalTeacherIds.equals(newTeacherIds);
        
        // Check if there's at least one teacher in common (intersection)
        boolean hasCommonTeacher = originalTeacherIds.stream()
            .anyMatch(newTeacherIds::contains);
        
        // Only replace planning if:
        // 1. Curricular unit changed (different content) OR
        // 2. ALL teachers were replaced (no continuity in teaching team)
        boolean shouldReplacePlanning = curricularUnitChanged || (teachersChanged && !hasCommonTeacher);
        
        if (shouldReplacePlanning) {
            log.info("Critical change detected in course {} requiring planning replacement: curricularUnit={}, allTeachersReplaced={}", 
                id, curricularUnitChanged, (teachersChanged && !hasCommonTeacher));
            
            if (curricularUnitChanged) {
                log.info("Curricular unit will change from {} to {} - planning will be replaced", 
                    course.getCurricularUnit().getId(), request.getCurricularUnitId());
            }
            
            if (teachersChanged && !hasCommonTeacher) {
                log.info("All teachers will be replaced (from {} to {}) - planning will be replaced", 
                    originalTeacherIds, newTeacherIds);
            }
        } else if (teachersChanged && hasCommonTeacher) {
            log.info("Teachers partially changed for course {} (from {} to {}) but at least one teacher remains - planning will be preserved", 
                id, originalTeacherIds, newTeacherIds);
        }
        
        CurricularUnit curricularUnit = curricularUnitRepository.findById(request.getCurricularUnitId())
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + request.getCurricularUnitId()));
        
        // Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        // Validate and update teachers
        Long programId = curricularUnit.getTerm().getProgram().getId();
        List<Campus> programCampuses = campusRepository.findByProgram(programId);
        
        if (programCampuses.isEmpty()) {
            throw new IllegalStateException("Program with id " + programId + " is not offered at any campus");
        }
        
        List<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .toList();
        
        log.debug("Validating teachers for program offered at {} campus(es): {}", programCampuses.size(), programCampusIds);
        
        List<Teacher> teachers = new ArrayList<>();
        for (Long userId : request.getUserIds()) {
            User user = userRepository.findByIdWithPositions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            
            Teacher teacher = user.getPositions().stream()
                .filter(pos -> pos instanceof Teacher)
                .map(pos -> (Teacher) pos)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User with id " + userId + " does not have a teacher position"));
            
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
                    "All teachers must share at least one common campus where the program is offered. " +
                    "Teachers provided do not have any campus in common."
                );
            }
            
            log.debug("Teachers share {} common campus(es): {}", commonCampusIds.size(), commonCampusIds);
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
                        String.format("Las horas para el formato %s no pueden ser negativas. Valor recibido: %d",
                            entry.getKey(), entry.getValue())
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
        
        // Apply planning changes based on what was detected earlier (BEFORE course modification)
        if (shouldReplacePlanning) {
            // Replace planning completely: curricular unit changed OR all teachers replaced
            resetAndCopyPlanningFromLatest(course, request.getUserIds(), request.getCurricularUnitId());
        } else {
            // Adjust WeeklyPlannings if dates changed (preserves existing content)
            adjustWeeklyPlanningsIfNeeded(course, request.getStartDate(), request.getEndDate(), request.getUserIds());
        }
        
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
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
        // Validate that the course doesn't have OfficeHours
        if (course.getOfficeHours() != null && !course.getOfficeHours().isEmpty()) {
            throw new IllegalStateException(
                "Cannot delete course with id " + id + " because it has " + 
                course.getOfficeHours().size() + " associated office hours. " +
                "Please remove all office hours before deleting the course."
            );
        }
        
        // Validate that the course doesn't have Modifications
        if (course.getModifications() != null && !course.getModifications().isEmpty()) {
            throw new IllegalStateException(
                "Cannot delete course with id " + id + " because it has " + 
                course.getModifications().size() + " associated modifications. " +
                "Please remove all modifications before deleting the course."
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
                    throw new edu.utec.planificador.exception.ResourceNotFoundException("Course not found with id: " + courseId);
                }
                throw new edu.utec.planificador.exception.ForbiddenException("You don't have access to the specified course in this campus");
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
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
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
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
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
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
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
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
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
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

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
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        CourseStatisticsDto statistics = courseStatisticsMapper.calculateStatistics(course);
        
        log.info("Statistics retrieved for course {}", courseId);
        
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public List<edu.utec.planificador.dto.response.TeacherCourseResponse> getTeacherCoursesByCurricularUnit(Long teacherId, Long curricularUnitId) {
        log.debug("Getting courses for teacher {} and curricular unit {}", teacherId, curricularUnitId);

        // Validate that the teacher exists
        User teacherUser = userRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + teacherId));

        // Validate that the curricular unit exists
        CurricularUnit curricularUnit = curricularUnitRepository.findById(curricularUnitId)
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + curricularUnitId));

        // Find the teacher position
        Teacher teacher = (Teacher) teacherUser.getPositions().stream()
            .filter(p -> p instanceof Teacher)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("User with id " + teacherId + " is not a teacher"));

        // Get all courses where this teacher is assigned AND the curricular unit matches
        List<Course> courses = courseRepository.findAll(
            CourseSpecification.withFilters(teacherId, null, null, null)
        ).stream()
            .filter(course -> course.getCurricularUnit().getId().equals(curricularUnitId))
            .toList();

        // Build response with formatted display names
        List<edu.utec.planificador.dto.response.TeacherCourseResponse> response = courses.stream()
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

                        return edu.utec.planificador.dto.response.TeacherCourseResponse.builder()
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
    public edu.utec.planificador.dto.response.CourseDetailedInfoResponse getCourseDetailedInfo(Long courseId) {
        log.debug("Getting detailed info for course {}", courseId);

        // Find course with necessary relationships
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

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
        List<edu.utec.planificador.dto.response.CourseDetailedInfoResponse.TeacherInfo> teachersInfo = 
            course.getTeachers().stream()
                .map(teacher -> {
                    User user = teacher.getUser();
                    return edu.utec.planificador.dto.response.CourseDetailedInfoResponse.TeacherInfo.builder()
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
        edu.utec.planificador.dto.response.CourseDetailedInfoResponse response = 
            edu.utec.planificador.dto.response.CourseDetailedInfoResponse.builder()
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
}
