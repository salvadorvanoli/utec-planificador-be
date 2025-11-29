package edu.utec.planificador.service.impl;

import edu.utec.planificador.entity.Activity;
import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.CurricularUnit;
import edu.utec.planificador.entity.Position;
import edu.utec.planificador.entity.Program;
import edu.utec.planificador.entity.ProgrammaticContent;
import edu.utec.planificador.entity.RegionalTechnologicalInstitute;
import edu.utec.planificador.entity.Term;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.entity.WeeklyPlanning;
import edu.utec.planificador.enumeration.Role;
import edu.utec.planificador.exception.ForbiddenException;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.repository.ActivityRepository;
import edu.utec.planificador.repository.CampusRepository;
import edu.utec.planificador.repository.CourseRepository;
import edu.utec.planificador.repository.CurricularUnitRepository;
import edu.utec.planificador.repository.ProgramRepository;
import edu.utec.planificador.repository.ProgrammaticContentRepository;
import edu.utec.planificador.repository.RegionalTechnologicalInstituteRepository;
import edu.utec.planificador.repository.TermRepository;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.repository.WeeklyPlanningRepository;
import edu.utec.planificador.service.MessageService;
import edu.utec.planificador.service.AccessControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessControlServiceImpl implements AccessControlService {

    private final CourseRepository courseRepository;
    private final CurricularUnitRepository curricularUnitRepository;
    private final WeeklyPlanningRepository weeklyPlanningRepository;
    private final ProgrammaticContentRepository programmaticContentRepository;
    private final ActivityRepository activityRepository;
    private final CampusRepository campusRepository;
    private final RegionalTechnologicalInstituteRepository rtiRepository;
    private final ProgramRepository programRepository;
    private final TermRepository termRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    @Override
    @Transactional(readOnly = true)
    public void validateCourseAccess(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.course.not-found", courseId)
            ));

        User currentUser = getCurrentUser();
        CurricularUnit curricularUnit = course.getCurricularUnit();
        Term term = curricularUnit.getTerm();
        Program program = term.getProgram();

        // Get campuses where this program is offered
        List<Campus> programCampuses = campusRepository.findByProgram(program.getId());

        if (programCampuses.isEmpty()) {
            throw new ForbiddenException(messageService.getMessage("error.access.no-campuses"));
        }

        Set<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .collect(Collectors.toSet());

        Set<Long> userCampusIds = getUserCampusIds();
        Set<Long> userRtiIds = getUserRtiIds();

        // First check: User must have access to the campus/RTI where the course belongs
        boolean hasCampusAccess = programCampusIds.stream().anyMatch(userCampusIds::contains) ||
                                  programCampuses.stream()
                                      .anyMatch(campus -> userRtiIds.contains(campus.getRegionalTechnologicalInstitute().getId()));

        if (!hasCampusAccess) {
            log.warn("User {} attempted to access course {} without campus access",
                currentUser.getUtecEmail(), courseId);
            throw new ForbiddenException(
                messageService.getMessage("error.access.no-course-access")
            );
        }

        // Second check: If user has ONLY TEACHER role, validate ownership
        if (hasOnlyTeacherRole()) {
            boolean isTeacherOfCourse = course.getTeachers().stream()
                .anyMatch(teacher -> teacher.getUser().getId().equals(currentUser.getId()));

            if (!isTeacherOfCourse) {
                log.warn("User {} attempted to access course {} without being assigned as teacher",
                    currentUser.getUtecEmail(), courseId);
                throw new ForbiddenException(
                    messageService.getMessage("error.access.not-assigned-teacher")
                );
            }
        }

        // Administrative roles (EDUCATION_MANAGER, COORDINATOR, ANALYST) pass with just campus access
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCurricularUnitAccess(Long curricularUnitId) {
        User currentUser = getCurrentUser();
        CurricularUnit curricularUnit = curricularUnitRepository.findById(curricularUnitId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.curricular-unit.not-found", curricularUnitId)
            ));

        Term term = curricularUnit.getTerm();
        Program program = term.getProgram();

        // Get campuses where this program is offered
        List<Campus> programCampuses = campusRepository.findByProgram(program.getId());

        if (programCampuses.isEmpty()) {
            throw new ForbiddenException(
                messageService.getMessage("error.access.no-campuses")
            );
        }

        Set<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .collect(Collectors.toSet());

        Set<Long> userCampusIds = getUserCampusIds();
        Set<Long> userRtiIds = getUserRtiIds();

        // First check: User must have access to the campus/RTI where the curricular unit belongs
        boolean hasCampusAccess = programCampusIds.stream().anyMatch(userCampusIds::contains) ||
                                  programCampuses.stream()
                                      .anyMatch(campus -> userRtiIds.contains(campus.getRegionalTechnologicalInstitute().getId()));

        if (!hasCampusAccess) {
            log.warn("User {} attempted to access curricular unit {} without campus access",
                currentUser.getUtecEmail(), curricularUnitId);
            throw new ForbiddenException(
                messageService.getMessage("error.access.no-curricular-unit-access")
            );
        }

        // Second check: If user has ONLY TEACHER role, validate they have at least one course in this CU
        if (hasOnlyTeacherRole()) {
            boolean hasAssociatedCourse = courseRepository.existsByCurricularUnitIdAndUserId(
                curricularUnitId, 
                currentUser.getId()
            );

            if (!hasAssociatedCourse) {
                log.warn("User {} attempted to access curricular unit {} without having any associated course",
                    currentUser.getUtecEmail(), curricularUnitId);
                throw new ForbiddenException(
                    messageService.getMessage("error.access.no-associated-course-cu")
                );
            }
        }

        // Administrative roles pass with just campus access
    }

    @Override
    @Transactional(readOnly = true)
    public void validateWeeklyPlanningAccess(Long weeklyPlanningId) {
        // Verify weekly planning exists and get its course
        if (!weeklyPlanningRepository.existsById(weeklyPlanningId)) {
            throw new ResourceNotFoundException(
                messageService.getMessage("error.weekly-planning.not-found", weeklyPlanningId)
            );
        }

        // Optimized: use direct query instead of loading all courses
        Course course = courseRepository.findByWeeklyPlanningId(weeklyPlanningId)
            .orElseThrow(() -> new ResourceNotFoundException(messageService.getMessage("error.course.not-found-for-weekly-planning", weeklyPlanningId)));

        validateCourseAccess(course.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateProgrammaticContentAccess(Long programmaticContentId) {
        ProgrammaticContent programmaticContent = programmaticContentRepository.findById(programmaticContentId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.programmatic-content.not-found", programmaticContentId)
            ));

        WeeklyPlanning weeklyPlanning = programmaticContent.getWeeklyPlanning();
        validateWeeklyPlanningAccess(weeklyPlanning.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateActivityAccess(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.activity.not-found", activityId)
            ));

        ProgrammaticContent programmaticContent = activity.getProgrammaticContent();
        validateProgrammaticContentAccess(programmaticContent.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCampusAccess(Long campusId) {
        Campus campus = campusRepository.findById(campusId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.campus.not-found", campusId)
            ));

        Set<Long> userCampusIds = getUserCampusIds();
        Set<Long> userRtiIds = getUserRtiIds();

        boolean hasAccess = userCampusIds.contains(campusId) ||
                           userRtiIds.contains(campus.getRegionalTechnologicalInstitute().getId());

        if (!hasAccess) {
            log.warn("User {} attempted to access campus {} without proper permissions",
                getCurrentUser().getUtecEmail(), campusId);
            throw new ForbiddenException(
                messageService.getMessage("error.access.no-campus-access")
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateRtiAccess(Long rtiId) {
        if (!rtiRepository.existsById(rtiId)) {
            throw new ResourceNotFoundException(
                messageService.getMessage("error.rti.not-found", rtiId)
            );
        }

        Set<Long> userRtiIds = getUserRtiIds();

        if (!userRtiIds.contains(rtiId)) {
            log.warn("User {} attempted to access RTI {} without proper permissions",
                getCurrentUser().getUtecEmail(), rtiId);
            throw new ForbiddenException(
                messageService.getMessage("error.access.no-rti-access")
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateProgramAccess(Long programId) {
        User currentUser = getCurrentUser();
        
        // Verify program exists
        if (!programRepository.existsById(programId)) {
            throw new ResourceNotFoundException(
                messageService.getMessage("error.program.not-found", programId)
            );
        }

        // Get campuses where this program is offered
        List<Campus> programCampuses = campusRepository.findByProgram(programId);

        if (programCampuses.isEmpty()) {
            throw new ForbiddenException(messageService.getMessage("error.access.no-campuses"));
        }

        Set<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .collect(Collectors.toSet());

        Set<Long> userCampusIds = getUserCampusIds();
        Set<Long> userRtiIds = getUserRtiIds();

        // First check: User must have access to the campus/RTI where the program is offered
        boolean hasCampusAccess = programCampusIds.stream().anyMatch(userCampusIds::contains) ||
                                  programCampuses.stream()
                                      .anyMatch(campus -> userRtiIds.contains(campus.getRegionalTechnologicalInstitute().getId()));

        if (!hasCampusAccess) {
            log.warn("User {} attempted to access program {} without campus access",
                currentUser.getUtecEmail(), programId);
            throw new ForbiddenException(
                messageService.getMessage("error.access.no-program-access")
            );
        }

        // Second check: If user has ONLY TEACHER role, validate they have at least one course in this program
        if (hasOnlyTeacherRole()) {
            boolean hasAssociatedCourse = courseRepository.existsByProgramIdAndUserId(
                programId, 
                currentUser.getId()
            );

            if (!hasAssociatedCourse) {
                log.warn("User {} attempted to access program {} without having any associated course",
                    currentUser.getUtecEmail(), programId);
                throw new ForbiddenException(
                    messageService.getMessage("error.access.no-associated-course-program")
                );
            }
        }

        // Administrative roles pass with just campus access
    }

    @Override
    @Transactional(readOnly = true)
    public void validateTermAccess(Long termId) {
        User currentUser = getCurrentUser();
        Term term = termRepository.findById(termId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.term.not-found", termId)
            ));

        Program program = term.getProgram();
        
        // First, validate program access (which already includes campus check and teacher ownership validation)
        validateProgramAccess(program.getId());

        // Additional check: If user has ONLY TEACHER role, validate they have at least one course in this specific term
        // Note: validateProgramAccess already validated they have courses in the program,
        // but we need to ensure they have courses in THIS specific term
        if (hasOnlyTeacherRole()) {
            boolean hasAssociatedCourse = courseRepository.existsByTermIdAndUserId(
                termId, 
                currentUser.getId()
            );

            if (!hasAssociatedCourse) {
                log.warn("User {} attempted to access term {} without having any associated course in this term",
                    currentUser.getUtecEmail(), termId);
                throw new ForbiddenException(
                    messageService.getMessage("error.access.no-associated-course-term")
                );
            }
        }
    }

    @Override
    public boolean hasAccessToCampus(Long campusId) {
        Campus campus = campusRepository.findById(campusId).orElse(null);
        if (campus == null) {
            return false;
        }

        Set<Long> userCampusIds = getUserCampusIds();
        Set<Long> userRtiIds = getUserRtiIds();

        return userCampusIds.contains(campusId) ||
               userRtiIds.contains(campus.getRegionalTechnologicalInstitute().getId());
    }

    @Override
    public boolean hasAccessToRti(Long rtiId) {
        Set<Long> userRtiIds = getUserRtiIds();
        return userRtiIds.contains(rtiId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    private Set<Long> getUserCampusIds() {
        User user = getCurrentUser();
        User fullUser = userRepository.findByIdWithPositions(user.getId())
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("error.user.not-found", user.getId())));

        return fullUser.getPositions().stream()
            .filter(Position::getIsActive)
            .flatMap(position -> position.getCampuses().stream())
            .map(Campus::getId)
            .collect(Collectors.toSet());
    }

    private Set<Long> getUserRtiIds() {
        User user = getCurrentUser();
        User fullUser = userRepository.findByIdWithPositions(user.getId())
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("error.user.not-found", user.getId())));

        return fullUser.getPositions().stream()
            .filter(Position::getIsActive)
            .flatMap(position -> position.getCampuses().stream())
            .map(Campus::getRegionalTechnologicalInstitute)
            .map(RegionalTechnologicalInstitute::getId)
            .collect(Collectors.toSet());
    }

    /**
     * Checks if the current user has ONLY the TEACHER role in all their active positions.
     * Returns true if ALL active positions are TEACHER role.
     * Returns false if user has any administrative role (EDUCATION_MANAGER, COORDINATOR, ANALYST).
     *
     * @return true if user has only TEACHER role in all active positions
     */
    private boolean hasOnlyTeacherRole() {
        User user = getCurrentUser();
        User fullUser = userRepository.findByIdWithPositions(user.getId())
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("error.user.not-found", user.getId())));

        List<Position> activePositions = fullUser.getPositions().stream()
            .filter(Position::getIsActive)
            .toList();

        if (activePositions.isEmpty()) {
            return false;
        }

        // Check if ALL active positions are TEACHER role
        return activePositions.stream()
            .allMatch(position -> position.getRole() == Role.TEACHER);
    }

    /**
     * Checks if the current user has AT LEAST one TEACHER role in their active positions.
     * Used for write access validation - even if user has administrative roles,
     * if they also have TEACHER role, ownership must be validated for write operations.
     *
     * @return true if user has at least one active TEACHER position
     */
    private boolean hasTeacherRole() {
        User user = getCurrentUser();
        User fullUser = userRepository.findByIdWithPositions(user.getId())
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("error.user.not-found", user.getId())));

        return fullUser.getPositions().stream()
            .filter(Position::getIsActive)
            .anyMatch(position -> position.getRole() == Role.TEACHER);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCoursePlanningManagement(Long courseId) {
        // First, validate basic course access (campus/RTI check)
        validateCourseAccess(courseId);

        // If user has TEACHER role (regardless of other roles), validate ownership for planning management operations
        // This applies to all planning hierarchy (WeeklyPlanning, ProgrammaticContent, Activity, OfficeHours)
        if (hasTeacherRole()) {
            User currentUser = getCurrentUser();
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(messageService.getMessage("error.course.not-found", courseId)));

            boolean isTeacherOfCourse = course.getTeachers().stream()
                .anyMatch(teacher -> teacher.getUser().getId().equals(currentUser.getId()));

            if (!isTeacherOfCourse) {
                log.warn("User {} attempted to manage planning for course {} without being assigned as teacher",
                    currentUser.getUtecEmail(), courseId);
                throw new ForbiddenException(
                    messageService.getMessage("error.access.cannot-manage-planning")
                );
            }
            
            log.debug("User {} validated as teacher of course {} for planning management", currentUser.getUtecEmail(), courseId);
        } else {
            // If user doesn't have TEACHER role at all, throw exception
            // (administrative operations should use validateCourseUpdateAccess or validateCourseDeleteAccess instead)
            User currentUser = getCurrentUser();
            log.warn("User {} without TEACHER role attempted to use validateCoursePlanningManagement on course {}",
                currentUser.getUtecEmail(), courseId);
            throw new ForbiddenException(
                messageService.getMessage("error.access.must-have-teacher-role")
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCourseUpdateAccess(Long courseId) {
        User currentUser = getCurrentUser();
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.course.not-found", courseId)
            ));

        CurricularUnit curricularUnit = course.getCurricularUnit();
        Term term = curricularUnit.getTerm();
        Program program = term.getProgram();

        // Get campuses where this program is offered
        List<Campus> programCampuses = campusRepository.findByProgram(program.getId());

        if (programCampuses.isEmpty()) {
            throw new ForbiddenException(messageService.getMessage("error.access.no-campuses"));
        }

        Set<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .collect(Collectors.toSet());

        // Get user's active positions with ANALYST or COORDINATOR roles in the relevant campuses
        User fullUser = userRepository.findByIdWithPositions(currentUser.getId())
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("error.user.not-found", currentUser.getId())));

        boolean hasAdminRole = fullUser.getPositions().stream()
            .filter(Position::getIsActive)
            .filter(position -> position.getRole() == Role.ANALYST || position.getRole() == Role.COORDINATOR)
            .flatMap(position -> position.getCampuses().stream())
            .map(Campus::getId)
            .anyMatch(programCampusIds::contains);

        // Check if user is a teacher of this course
        boolean isTeacherOfCourse = course.getTeachers().stream()
            .anyMatch(teacher -> teacher.getUser().getId().equals(currentUser.getId()));

        // User must have either administrative role OR be a teacher of the course
        if (!hasAdminRole && !isTeacherOfCourse) {
            log.warn("User {} attempted to update course {} without ANALYST/COORDINATOR role or being assigned as teacher",
                currentUser.getUtecEmail(), courseId);
            throw new ForbiddenException(
                messageService.getMessage("error.access.cannot-update-course")
            );
        }

        if (hasAdminRole) {
            log.debug("User {} has update access to course {} through ANALYST/COORDINATOR role", 
                currentUser.getUtecEmail(), courseId);
        } else {
            log.debug("User {} has update access to course {} as assigned teacher", 
                currentUser.getUtecEmail(), courseId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCourseDeleteAccess(Long courseId) {
        User currentUser = getCurrentUser();
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException(messageService.getMessage("error.course.not-found", courseId)));

        CurricularUnit curricularUnit = course.getCurricularUnit();
        Term term = curricularUnit.getTerm();
        Program program = term.getProgram();

        // Get campuses where this program is offered
        List<Campus> programCampuses = campusRepository.findByProgram(program.getId());

        if (programCampuses.isEmpty()) {
            throw new ForbiddenException(messageService.getMessage("error.access.no-campuses"));
        }

        Set<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .collect(Collectors.toSet());

        // Get user's active positions with ANALYST or COORDINATOR roles in the relevant campuses
        User fullUser = userRepository.findByIdWithPositions(currentUser.getId())
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("error.user.not-found", currentUser.getId())));

        boolean hasAdminRole = fullUser.getPositions().stream()
            .filter(Position::getIsActive)
            .filter(position -> position.getRole() == Role.ANALYST || position.getRole() == Role.COORDINATOR)
            .flatMap(position -> position.getCampuses().stream())
            .map(Campus::getId)
            .anyMatch(programCampusIds::contains);

        // Only users with ANALYST or COORDINATOR roles can delete courses
        // Teachers are NOT allowed to delete courses
        if (!hasAdminRole) {
            log.warn("User {} attempted to delete course {} without ANALYST or COORDINATOR role in the appropriate campus",
                currentUser.getUtecEmail(), courseId);
            throw new ForbiddenException(
                messageService.getMessage("error.access.cannot-delete-course")
            );
        }

        log.debug("User {} has delete access to course {} through ANALYST/COORDINATOR role", 
            currentUser.getUtecEmail(), courseId);
    }
}
