package edu.utec.planificador.service.impl;

import edu.utec.planificador.entity.*;
import edu.utec.planificador.enumeration.Role;
import edu.utec.planificador.exception.ForbiddenException;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.repository.*;
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

    @Override
    @Transactional(readOnly = true)
    public void validateCourseAccess(Long courseId) {

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        CurricularUnit curricularUnit = course.getCurricularUnit();
        Term term = curricularUnit.getTerm();
        Program program = term.getProgram();

        // Get campuses where this program is offered
        List<Campus> programCampuses = campusRepository.findByProgram(program.getId());

        if (programCampuses.isEmpty()) {
            throw new ForbiddenException("No campuses found for this course's program");
        }

        // Check if user has access to any of these campuses
        Set<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .collect(Collectors.toSet());

        Set<Long> userCampusIds = getUserCampusIds();
        Set<Long> userRtiIds = getUserRtiIds();

        // User has access if they have position in any campus that offers the program
        // OR if they have position in the RTI that owns any of those campuses
        boolean hasAccess = programCampusIds.stream().anyMatch(userCampusIds::contains) ||
                           programCampuses.stream()
                               .anyMatch(campus -> userRtiIds.contains(campus.getRegionalTechnologicalInstitute().getId()));

        if (!hasAccess) {
            log.warn("User {} attempted to access course {} without proper permissions",
                getCurrentUser().getUtecEmail(), courseId);
            throw new ForbiddenException("You don't have access to this course");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCurricularUnitAccess(Long curricularUnitId) {

        CurricularUnit curricularUnit = curricularUnitRepository.findById(curricularUnitId)
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + curricularUnitId));

        Term term = curricularUnit.getTerm();
        Program program = term.getProgram();

        // Get campuses where this program is offered
        List<Campus> programCampuses = campusRepository.findByProgram(program.getId());

        if (programCampuses.isEmpty()) {
            throw new ForbiddenException("No campuses found for this curricular unit's program");
        }

        Set<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .collect(Collectors.toSet());

        Set<Long> userCampusIds = getUserCampusIds();
        Set<Long> userRtiIds = getUserRtiIds();

        boolean hasAccess = programCampusIds.stream().anyMatch(userCampusIds::contains) ||
                           programCampuses.stream()
                               .anyMatch(campus -> userRtiIds.contains(campus.getRegionalTechnologicalInstitute().getId()));

        if (!hasAccess) {
            log.warn("User {} attempted to access curricular unit {} without proper permissions",
                getCurrentUser().getUtecEmail(), curricularUnitId);
            throw new ForbiddenException("You don't have access to this curricular unit");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateWeeklyPlanningAccess(Long weeklyPlanningId) {

        // Verify weekly planning exists
        if (!weeklyPlanningRepository.existsById(weeklyPlanningId)) {
            throw new ResourceNotFoundException("Weekly planning not found with id: " + weeklyPlanningId);
        }

        // Find the course that owns this weekly planning
        Course course = courseRepository.findAll().stream()
            .filter(c -> c.getWeeklyPlannings().stream()
                .anyMatch(wp -> wp.getId().equals(weeklyPlanningId)))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Course not found for weekly planning: " + weeklyPlanningId));

        validateCourseAccess(course.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateProgrammaticContentAccess(Long programmaticContentId) {

        ProgrammaticContent programmaticContent = programmaticContentRepository.findById(programmaticContentId)
            .orElseThrow(() -> new ResourceNotFoundException("Programmatic content not found with id: " + programmaticContentId));

        WeeklyPlanning weeklyPlanning = programmaticContent.getWeeklyPlanning();
        validateWeeklyPlanningAccess(weeklyPlanning.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateActivityAccess(Long activityId) {

        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        ProgrammaticContent programmaticContent = activity.getProgrammaticContent();
        validateProgrammaticContentAccess(programmaticContent.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCampusAccess(Long campusId) {

        Campus campus = campusRepository.findById(campusId)
            .orElseThrow(() -> new ResourceNotFoundException("Campus not found with id: " + campusId));

        Set<Long> userCampusIds = getUserCampusIds();
        Set<Long> userRtiIds = getUserRtiIds();

        boolean hasAccess = userCampusIds.contains(campusId) ||
                           userRtiIds.contains(campus.getRegionalTechnologicalInstitute().getId());

        if (!hasAccess) {
            log.warn("User {} attempted to access campus {} without proper permissions",
                getCurrentUser().getUtecEmail(), campusId);
            throw new ForbiddenException("You don't have access to this campus");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateRtiAccess(Long rtiId) {

        if (!rtiRepository.existsById(rtiId)) {
            throw new ResourceNotFoundException("RTI not found with id: " + rtiId);
        }

        Set<Long> userRtiIds = getUserRtiIds();

        if (!userRtiIds.contains(rtiId)) {
            log.warn("User {} attempted to access RTI {} without proper permissions",
                getCurrentUser().getUtecEmail(), rtiId);
            throw new ForbiddenException("You don't have access to this Regional Technological Institute");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateProgramAccess(Long programId) {

        // Verify program exists
        if (!programRepository.existsById(programId)) {
            throw new ResourceNotFoundException("Program not found with id: " + programId);
        }

        // Get campuses where this program is offered
        List<Campus> programCampuses = campusRepository.findByProgram(programId);

        if (programCampuses.isEmpty()) {
            throw new ForbiddenException("No campuses found for this program");
        }

        Set<Long> programCampusIds = programCampuses.stream()
            .map(Campus::getId)
            .collect(Collectors.toSet());

        Set<Long> userCampusIds = getUserCampusIds();
        Set<Long> userRtiIds = getUserRtiIds();

        boolean hasAccess = programCampusIds.stream().anyMatch(userCampusIds::contains) ||
                           programCampuses.stream()
                               .anyMatch(campus -> userRtiIds.contains(campus.getRegionalTechnologicalInstitute().getId()));

        if (!hasAccess) {
            log.warn("User {} attempted to access program {} without proper permissions",
                getCurrentUser().getUtecEmail(), programId);
            throw new ForbiddenException("You don't have access to this program");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateTermAccess(Long termId) {

        Term term = termRepository.findById(termId)
            .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + termId));

        Program program = term.getProgram();
        validateProgramAccess(program.getId());
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
            .orElseThrow(() -> new RuntimeException("User not found"));

        return fullUser.getPositions().stream()
            .filter(Position::getIsActive)
            .flatMap(position -> position.getCampuses().stream())
            .map(Campus::getId)
            .collect(Collectors.toSet());
    }

    private Set<Long> getUserRtiIds() {
        User user = getCurrentUser();
        User fullUser = userRepository.findByIdWithPositions(user.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        return fullUser.getPositions().stream()
            .filter(Position::getIsActive)
            .flatMap(position -> position.getCampuses().stream())
            .map(Campus::getRegionalTechnologicalInstitute)
            .map(RegionalTechnologicalInstitute::getId)
            .collect(Collectors.toSet());
    }
}

