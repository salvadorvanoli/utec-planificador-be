package edu.utec.planificador.service;

import edu.utec.planificador.dto.request.CourseRequest;
import edu.utec.planificador.dto.response.CoursePdfDataResponse;
import edu.utec.planificador.dto.response.CourseResponse;
import edu.utec.planificador.dto.response.PeriodResponse;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {

    CourseResponse createCourse(CourseRequest request);

    CourseResponse getCourseById(Long id);

    CourseResponse updateCourse(Long id, CourseRequest request);

    void deleteCourse(Long id);

    Page<CourseResponse> getCourses(Long userId, Long campusId, String period, Pageable pageable);

    // Returns brief course info (id, curricular unit name, start date, shift) for the current user in a campus
    // If courseId is provided, validates that the course belongs to the current user's courses in that campus,
    // otherwise throws a ForbiddenException or ResourceNotFoundException as appropriate.
    java.util.List<edu.utec.planificador.dto.response.CourseBriefResponse> getCoursesForCurrentUserInCampus(Long campusId, Long courseId);

    List<PeriodResponse> getPeriodsByCampus(Long campusId);

    // Sustainable Development Goals (ODS)
    CourseResponse addSustainableDevelopmentGoal(Long courseId, SustainableDevelopmentGoal goal);

    CourseResponse removeSustainableDevelopmentGoal(Long courseId, SustainableDevelopmentGoal goal);

    // Universal Design Learning Principles
    CourseResponse addUniversalDesignLearningPrinciple(Long courseId, UniversalDesignLearningPrinciple principle);

    CourseResponse removeUniversalDesignLearningPrinciple(Long courseId, UniversalDesignLearningPrinciple principle);

    // PDF Data Export
    CoursePdfDataResponse getCoursePdfData(Long courseId);
}
