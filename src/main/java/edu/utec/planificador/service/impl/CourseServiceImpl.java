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
        
        // Validate update access (ANALYST/COORDINATOR in campus OR teacher of the course)
        accessControlService.validateCourseUpdateAccess(id);
        
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
}
