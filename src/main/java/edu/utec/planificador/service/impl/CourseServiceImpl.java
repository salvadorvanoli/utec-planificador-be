package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.request.CourseRequest;
import edu.utec.planificador.dto.response.CourseResponse;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.CurricularUnit;
import edu.utec.planificador.entity.WeeklyPlanning;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.repository.CourseRepository;
import edu.utec.planificador.repository.CurricularUnitRepository;
import edu.utec.planificador.service.CourseService;
import edu.utec.planificador.util.WeeklyPlanningGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CurricularUnitRepository curricularUnitRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        log.debug("Creating course with description: {}", request.getDescription());
        
        CurricularUnit curricularUnit = curricularUnitRepository.findById(request.getCurricularUnitId())
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + request.getCurricularUnitId()));
        
        Course course = new Course(
            request.getShift(),
            request.getDescription(),
            request.getStartDate(),
            request.getEndDate(),
            request.getPartialGradingSystem(),
            curricularUnit
        );
        
        // Set optional fields
        course.setIsRelatedToInvestigation(request.getIsRelatedToInvestigation());
        course.setInvolvesActivitiesWithProductiveSector(request.getInvolvesActivitiesWithProductiveSector());
        
        // Set collections
        if (request.getHoursPerDeliveryFormat() != null) {
            course.getHoursPerDeliveryFormat().putAll(request.getHoursPerDeliveryFormat());
        }
        
        if (request.getSustainableDevelopmentGoals() != null) {
            course.getSustainableDevelopmentGoals().addAll(request.getSustainableDevelopmentGoals());
        }
        
        if (request.getUniversalDesignLearningPrinciples() != null) {
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
        
        log.info("Course created successfully with id: {}", savedCourse.getId());
        
        return mapToResponse(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        log.debug("Getting course by id: {}", id);
        
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
        // Mapear dentro de la transacción para acceder a colecciones LAZY
        CourseResponse response = mapToResponse(course);
        
        return response;
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        log.debug("Updating course with id: {}", id);
        
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
        CurricularUnit curricularUnit = curricularUnitRepository.findById(request.getCurricularUnitId())
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + request.getCurricularUnitId()));
        
        // Update fields
        course.setShift(request.getShift());
        course.setDescription(request.getDescription());
        course.setStartDate(request.getStartDate());
        course.setEndDate(request.getEndDate());
        course.setPartialGradingSystem(request.getPartialGradingSystem());
        course.setIsRelatedToInvestigation(request.getIsRelatedToInvestigation());
        course.setInvolvesActivitiesWithProductiveSector(request.getInvolvesActivitiesWithProductiveSector());
        course.setCurricularUnit(curricularUnit);
        
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
        
        log.info("Course updated successfully with id: {}", updatedCourse.getId());
        
        return mapToResponse(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        log.debug("Deleting course with id: {}", id);
        
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        
        courseRepository.deleteById(id);
        
        log.info("Course deleted successfully with id: {}", id);
    }

    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
            .id(course.getId())
            .shift(course.getShift())
            .description(course.getDescription())
            .startDate(course.getStartDate())
            .endDate(course.getEndDate())
            .partialGradingSystem(course.getPartialGradingSystem())
            .hoursPerDeliveryFormat(course.getHoursPerDeliveryFormat())
            .isRelatedToInvestigation(course.getIsRelatedToInvestigation())
            .involvesActivitiesWithProductiveSector(course.getInvolvesActivitiesWithProductiveSector())
            .sustainableDevelopmentGoals(course.getSustainableDevelopmentGoals())
            .universalDesignLearningPrinciples(course.getUniversalDesignLearningPrinciples())
            .curricularUnitId(course.getCurricularUnit().getId())
            .build();
    }

    // ==================== Sustainable Development Goals (ODS) ====================

    @Override
    @Transactional
    public CourseResponse addSustainableDevelopmentGoal(Long courseId, SustainableDevelopmentGoal goal) {
        log.debug("Adding Sustainable Development Goal {} to course {}", goal, courseId);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        course.getSustainableDevelopmentGoals().add(goal);
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Sustainable Development Goal {} added to course {}", goal, courseId);
        
        return mapToResponse(updatedCourse);
    }

    @Override
    @Transactional
    public CourseResponse removeSustainableDevelopmentGoal(Long courseId, SustainableDevelopmentGoal goal) {
        log.debug("Removing Sustainable Development Goal {} from course {}", goal, courseId);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        boolean removed = course.getSustainableDevelopmentGoals().remove(goal);
        
        if (!removed) {
            log.warn("Sustainable Development Goal {} was not found in course {}", goal, courseId);
        }
        
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Sustainable Development Goal {} removed from course {}", goal, courseId);
        
        return mapToResponse(updatedCourse);
    }

    // ==================== Universal Design Learning Principles ====================

    @Override
    @Transactional
    public CourseResponse addUniversalDesignLearningPrinciple(Long courseId, UniversalDesignLearningPrinciple principle) {
        log.debug("Adding Universal Design Learning Principle {} to course {}", principle, courseId);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        course.getUniversalDesignLearningPrinciples().add(principle);
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Universal Design Learning Principle {} added to course {}", principle, courseId);
        
        return mapToResponse(updatedCourse);
    }

    @Override
    @Transactional
    public CourseResponse removeUniversalDesignLearningPrinciple(Long courseId, UniversalDesignLearningPrinciple principle) {
        log.debug("Removing Universal Design Learning Principle {} from course {}", principle, courseId);
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        boolean removed = course.getUniversalDesignLearningPrinciples().remove(principle);
        
        if (!removed) {
            log.warn("Universal Design Learning Principle {} was not found in course {}", principle, courseId);
        }
        
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Universal Design Learning Principle {} removed from course {}", principle, courseId);
        
        return mapToResponse(updatedCourse);
    }
}
