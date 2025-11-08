package edu.utec.planificador.service;

import edu.utec.planificador.entity.*;
import edu.utec.planificador.enumeration.Role;

/**
 * Service for validating user access to resources based on their positions.
 * Ensures that users can only access resources within their assigned RTIs and Campuses.
 */
public interface AccessControlService {

    /**
     * Validates if the current user has access to a specific course.
     * A user has access if they have a position in the campus/RTI where the course belongs.
     *
     * @param courseId Course ID to validate access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have access
     */
    void validateCourseAccess(Long courseId);

    /**
     * Validates if the current user has access to a specific curricular unit.
     * A user has access if they have a position in the campus/RTI where the curricular unit belongs.
     *
     * @param curricularUnitId Curricular Unit ID to validate access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have access
     */
    void validateCurricularUnitAccess(Long curricularUnitId);

    /**
     * Validates if the current user has access to a specific weekly planning.
     * Access is determined by the course's campus/RTI.
     *
     * @param weeklyPlanningId Weekly Planning ID to validate access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have access
     */
    void validateWeeklyPlanningAccess(Long weeklyPlanningId);

    /**
     * Validates if the current user has access to a specific programmatic content.
     * Access is determined by the course's campus/RTI.
     *
     * @param programmaticContentId Programmatic Content ID to validate access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have access
     */
    void validateProgrammaticContentAccess(Long programmaticContentId);

    /**
     * Validates if the current user has access to a specific activity.
     * Access is determined by the course's campus/RTI.
     *
     * @param activityId Activity ID to validate access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have access
     */
    void validateActivityAccess(Long activityId);

    /**
     * Validates if the current user has access to a specific campus.
     *
     * @param campusId Campus ID to validate access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have access
     */
    void validateCampusAccess(Long campusId);

    /**
     * Validates if the current user has access to a specific RTI.
     *
     * @param rtiId RTI ID to validate access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have access
     */
    void validateRtiAccess(Long rtiId);

    /**
     * Validates if the current user has access to a specific program.
     * Access is determined by the campuses where the program is offered.
     *
     * @param programId Program ID to validate access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have access
     */
    void validateProgramAccess(Long programId);

    /**
     * Validates if the current user has access to a specific term.
     * Access is determined by the program's campuses.
     *
     * @param termId Term ID to validate access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have access
     */
    void validateTermAccess(Long termId);

    /**
     * Checks if the current user has access to a specific campus.
     *
     * @param campusId Campus ID
     * @return true if user has access, false otherwise
     */
    boolean hasAccessToCampus(Long campusId);

    /**
     * Checks if the current user has access to a specific RTI.
     *
     * @param rtiId RTI ID
     * @return true if user has access, false otherwise
     */
    boolean hasAccessToRti(Long rtiId);
}

