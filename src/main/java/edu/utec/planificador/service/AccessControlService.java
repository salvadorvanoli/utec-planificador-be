package edu.utec.planificador.service;

/**
 * Service for validating user access to resources based on their positions.
 * Ensures that users can only access resources within their assigned RTIs and Campuses.
 * For TEACHER role, also validates ownership of courses.
 */
public interface AccessControlService {

    /**
     * Validates if the current user has access to a specific course.
     * - Administrative roles: Access if they have position in the campus/RTI where the course belongs
     * - TEACHER: Access only if they are assigned as teacher to this course AND it's in their campus
     *
     * @param courseId Course ID to validate access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have access
     */
    void validateCourseAccess(Long courseId);

    /**
     * Validates if the current user has access to a specific curricular unit.
     * - Administrative roles: Access if they have position in the campus/RTI where the curricular unit belongs
     * - TEACHER: Access only if they have at least one course in this curricular unit AND it's in their campus
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
     * - Administrative roles: Access if they have position in the campuses where the program is offered
     * - TEACHER: Access only if they have at least one course in this program AND it's in their campus
     *
     * @param programId Program ID to validate access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have access
     */
    void validateProgramAccess(Long programId);

    /**
     * Validates if the current user has access to a specific term.
     * - Administrative roles: Access if they have position in the campuses where the term's program is offered
     * - TEACHER: Access only if they have at least one course in this specific term AND it's in their campus
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

    /**
     * Validates if the current user can manage the planning of a specific course and its planning hierarchy.
     * Requires user to have TEACHER role and be assigned to the course.
     * This ensures that even users with administrative roles can only manage planning for courses they teach.
     * 
     * This validation should be used for planning management operations on:
     * - Weekly Planning (children of Course)
     * - Programmatic Content (children of Weekly Planning)
     * - Activity (children of Programmatic Content)
     * - Office Hours
     *
     * @param courseId Course ID to validate planning management access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have TEACHER role or is not assigned to the course
     */
    void validateCoursePlanningManagement(Long courseId);

    /**
     * Validates if the current user has permission to update a course.
     * 
     * User must meet ONE of the following criteria:
     * 1. Have ANALYST or COORDINATOR role in the campus where the course belongs
     * 2. Be assigned as a teacher to the course
     * 
     * This validation is more permissive than validateCourseDeleteAccess, allowing teachers
     * to update their own courses.
     *
     * @param courseId Course ID to validate update access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't meet any of the criteria
     */
    void validateCourseUpdateAccess(Long courseId);

    /**
     * Validates if the current user has permission to delete a course.
     * 
     * User must have ANALYST or COORDINATOR role in the campus where the course belongs.
     * Teachers are NOT allowed to delete courses, even if they are assigned to them.
     * 
     * This is a more restrictive validation than validateCourseUpdateAccess, as deleting
     * a course is a critical operation that should only be performed by administrative users.
     *
     * @param courseId Course ID to validate delete access
     * @throws edu.utec.planificador.exception.ForbiddenException if user doesn't have administrative role
     */
    void validateCourseDeleteAccess(Long courseId);
}

