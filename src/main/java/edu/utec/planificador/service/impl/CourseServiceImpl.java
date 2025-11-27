package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.aiagent.AIReportRequest.CourseStatisticsDto;
import edu.utec.planificador.dto.request.CourseRequest;
import edu.utec.planificador.dto.response.CourseBasicResponse;
import edu.utec.planificador.dto.response.CourseBriefResponse;
import edu.utec.planificador.dto.response.CoursePdfDataResponse;
import edu.utec.planificador.dto.response.CourseResponse;
import edu.utec.planificador.dto.response.PeriodResponse;
import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.CurricularUnit;
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
import java.util.List;
import java.util.Objects;
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
        
        Course savedCourse = courseRepository.save(course);
        
        log.info("Course created successfully with id: {} and {} teacher(s)", savedCourse.getId(), teachers.size());
        
        return courseMapper.toResponse(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        log.debug("Getting course by id: {}", id);
        
        // Validate access to course
        accessControlService.validateCourseAccess(id);

        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
        // Mapear dentro de la transacción para acceder a colecciones LAZY
        return courseMapper.toResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getLatestCourseByCurricularUnitAndUser(Long curricularUnitId, Long userId) {
        log.debug("Getting latest course for curricular unit: {} and user: {}", curricularUnitId, userId);
        
        // Validate access to curricular unit
        accessControlService.validateCurricularUnitAccess(curricularUnitId);
        
        Course course = courseRepository.findLatestByCurricularUnitAndUser(curricularUnitId, userId)
            .orElse(null);
        
        if (course == null) {
            log.debug("No previous course found for curricular unit: {} and user: {}", curricularUnitId, userId);
            return null;
        }
        
        log.debug("Found previous course with id: {}", course.getId());
        return courseMapper.toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        log.debug("Updating course with id: {}", id);
        
        // Validate write access (ensures teachers can only modify their own courses)
        accessControlService.validateCourseWriteAccess(id);
        accessControlService.validateCurricularUnitAccess(request.getCurricularUnitId());

        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
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
        
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Course updated successfully with id: {} and {} teacher(s)", id, teachers.size());
        
        return courseMapper.toResponse(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        log.debug("Deleting course with id: {}", id);
        
        // Validate access to course
        accessControlService.validateCourseAccess(id);

        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        
        courseRepository.deleteById(id);
        
        log.info("Course deleted successfully with id: {}", id);
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
    public List<PeriodResponse> getPeriodsByCampus(Long campusId) {
        List<Course> courses = courseRepository.findAll(
            CourseSpecification.withFilters(null, campusId, null, null)
        );

        List<PeriodResponse> periods = courses.stream()
            .map(Course::getPeriod)
            .filter(Objects::nonNull)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .map(period -> PeriodResponse.builder().period(period).build())
            .toList();

        log.debug("Found {} unique periods for campus {}", periods.size(), campusId);

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
        
        // Validate access to course
        accessControlService.validateCourseWriteAccess(courseId);

        Course course = courseRepository.findById(courseId)
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
        
        // Validate access to course
        accessControlService.validateCourseWriteAccess(courseId);

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
        
        // Validate access to course
        accessControlService.validateCourseWriteAccess(courseId);

        Course course = courseRepository.findById(courseId)
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
        
        // Validate access to course
        accessControlService.validateCourseWriteAccess(courseId);

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
