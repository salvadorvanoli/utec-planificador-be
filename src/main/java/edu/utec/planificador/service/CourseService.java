package edu.utec.planificador.service;

import edu.utec.planificador.dto.request.CourseRequest;
import edu.utec.planificador.dto.response.CourseResponse;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;

public interface CourseService {

    CourseResponse createCourse(CourseRequest request);

    CourseResponse getCourseById(Long id);

    CourseResponse updateCourse(Long id, CourseRequest request);

    void deleteCourse(Long id);

    // Sustainable Development Goals (ODS)
    CourseResponse addSustainableDevelopmentGoal(Long courseId, SustainableDevelopmentGoal goal);

    CourseResponse removeSustainableDevelopmentGoal(Long courseId, SustainableDevelopmentGoal goal);

    // Universal Design Learning Principles
    CourseResponse addUniversalDesignLearningPrinciple(Long courseId, UniversalDesignLearningPrinciple principle);

    CourseResponse removeUniversalDesignLearningPrinciple(Long courseId, UniversalDesignLearningPrinciple principle);
}
