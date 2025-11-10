package edu.utec.planificador.service;

import edu.utec.planificador.dto.request.CourseRequest;
import edu.utec.planificador.dto.response.CourseResponse;
import edu.utec.planificador.dto.response.PeriodResponse;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;

import java.util.List;

public interface CourseService {

    CourseResponse createCourse(CourseRequest request);

    CourseResponse getCourseById(Long id);

    CourseResponse updateCourse(Long id, CourseRequest request);

    void deleteCourse(Long id);

    List<CourseResponse> getCourses(Long userId, Long campusId, String period);

    List<PeriodResponse> getPeriodsByCampus(Long campusId);

    // Sustainable Development Goals (ODS)
    CourseResponse addSustainableDevelopmentGoal(Long courseId, SustainableDevelopmentGoal goal);

    CourseResponse removeSustainableDevelopmentGoal(Long courseId, SustainableDevelopmentGoal goal);

    // Universal Design Learning Principles
    CourseResponse addUniversalDesignLearningPrinciple(Long courseId, UniversalDesignLearningPrinciple principle);

    CourseResponse removeUniversalDesignLearningPrinciple(Long courseId, UniversalDesignLearningPrinciple principle);
}
