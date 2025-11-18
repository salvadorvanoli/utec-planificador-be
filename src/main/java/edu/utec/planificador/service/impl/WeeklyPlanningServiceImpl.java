package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.request.WeeklyPlanningRequest;
import edu.utec.planificador.dto.response.WeeklyPlanningResponse;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.WeeklyPlanning;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.repository.CourseRepository;
import edu.utec.planificador.repository.WeeklyPlanningRepository;
import edu.utec.planificador.service.AccessControlService;
import edu.utec.planificador.service.WeeklyPlanningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyPlanningServiceImpl implements WeeklyPlanningService {

    private final WeeklyPlanningRepository weeklyPlanningRepository;
    private final CourseRepository courseRepository;
    private final AccessControlService accessControlService;

    @Override
    @Transactional
    public WeeklyPlanningResponse createWeeklyPlanning(Long courseId, WeeklyPlanningRequest request) {
        log.debug("Creating weekly planning for courseId={}", courseId);

        // Validate access to course
        accessControlService.validateCourseAccess(courseId);

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        WeeklyPlanning weeklyPlanning = new WeeklyPlanning(
            request.getWeekNumber(),
            request.getStartDate(),
            request.getEndDate()
        );

        if (request.getBibliographicReferences() != null) {
            weeklyPlanning.getBibliographicReferences().addAll(request.getBibliographicReferences());
        }

        course.getWeeklyPlannings().add(weeklyPlanning);

        WeeklyPlanning saved = weeklyPlanningRepository.save(weeklyPlanning);
        log.info("Created weekly planning with id={}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyPlanningResponse getWeeklyPlanningById(Long id) {
        // Validate access to weekly planning
        accessControlService.validateWeeklyPlanningAccess(id);

        WeeklyPlanning weeklyPlanning = weeklyPlanningRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("WeeklyPlanning not found with id: " + id));

        return mapToResponse(weeklyPlanning);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyPlanningResponse> getWeeklyPlanningsByCourseId(Long courseId) {
        log.debug("Getting all weekly plannings for courseId={}", courseId);

        // Validate access to course
        accessControlService.validateCourseAccess(courseId);

        // Verify course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        List<WeeklyPlanning> weeklyPlannings = weeklyPlanningRepository.findByCourseId(courseId);

        return weeklyPlannings.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyPlanningResponse getWeeklyPlanningByCourseIdAndWeekNumber(Long courseId, Integer weekNumber) {
        log.debug("Getting weekly planning for courseId={} and weekNumber={}", courseId, weekNumber);

        // Validate access to course
        accessControlService.validateCourseAccess(courseId);

        WeeklyPlanning weeklyPlanning = weeklyPlanningRepository.findByCourseIdAndWeekNumber(courseId, weekNumber)
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format("WeeklyPlanning not found for courseId=%d and weekNumber=%d", courseId, weekNumber)
            ));

        return mapToResponse(weeklyPlanning);
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyPlanningResponse getWeeklyPlanningByCourseIdAndDate(Long courseId, LocalDate date) {
        log.debug("Getting weekly planning for courseId={} and date={}", courseId, date);

        // Validate access to course
        accessControlService.validateCourseAccess(courseId);

        WeeklyPlanning weeklyPlanning = weeklyPlanningRepository.findByCourseIdAndDate(courseId, date)
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format("WeeklyPlanning not found for courseId=%d and date=%s", courseId, date)
            ));

        return mapToResponse(weeklyPlanning);
    }

    @Override
    @Transactional
    public WeeklyPlanningResponse updateWeeklyPlanning(Long id, WeeklyPlanningRequest request) {
        // Validate access to weekly planning
        accessControlService.validateWeeklyPlanningAccess(id);

        WeeklyPlanning weeklyPlanning = weeklyPlanningRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("WeeklyPlanning not found with id: " + id));

        weeklyPlanning.setWeekNumber(request.getWeekNumber());
        weeklyPlanning.setStartDate(request.getStartDate());
        weeklyPlanning.setEndDate(request.getEndDate());

        weeklyPlanning.getBibliographicReferences().clear();
        if (request.getBibliographicReferences() != null) {
            weeklyPlanning.getBibliographicReferences().addAll(request.getBibliographicReferences());
        }

        WeeklyPlanning updated = weeklyPlanningRepository.save(weeklyPlanning);
        log.info("Updated weekly planning with id={}", updated.getId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteWeeklyPlanning(Long id) {
        // Validate access to weekly planning
        accessControlService.validateWeeklyPlanningAccess(id);

        if (!weeklyPlanningRepository.existsById(id)) {
            throw new ResourceNotFoundException("WeeklyPlanning not found with id: " + id);
        }

        weeklyPlanningRepository.deleteById(id);
        log.info("Deleted weekly planning with id={}", id);
    }

    @Override
    @Transactional
    public void addBibliographicReference(Long weeklyPlanningId, String reference) {
        log.debug("Adding bibliographic reference to weeklyPlanningId={}", weeklyPlanningId);

        // Validate access to weekly planning
        accessControlService.validateWeeklyPlanningAccess(weeklyPlanningId);

        WeeklyPlanning weeklyPlanning = weeklyPlanningRepository.findById(weeklyPlanningId)
            .orElseThrow(() -> new ResourceNotFoundException("WeeklyPlanning not found with id: " + weeklyPlanningId));

        if (!weeklyPlanning.getBibliographicReferences().contains(reference)) {
            weeklyPlanning.getBibliographicReferences().add(reference);
            weeklyPlanningRepository.save(weeklyPlanning);
            log.info("Added bibliographic reference to weekly planning with id={}", weeklyPlanningId);
        } else {
            log.debug("Bibliographic reference already exists in weekly planning with id={}", weeklyPlanningId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getBibliographicReferences(Long weeklyPlanningId) {
        log.debug("Getting bibliographic references for weeklyPlanningId={}", weeklyPlanningId);

        // Validate access to weekly planning
        accessControlService.validateWeeklyPlanningAccess(weeklyPlanningId);

        WeeklyPlanning weeklyPlanning = weeklyPlanningRepository.findById(weeklyPlanningId)
            .orElseThrow(() -> new ResourceNotFoundException("WeeklyPlanning not found with id: " + weeklyPlanningId));

        return weeklyPlanning.getBibliographicReferences();
    }

    @Override
    @Transactional
    public void removeBibliographicReference(Long weeklyPlanningId, String reference) {
        log.debug("Removing bibliographic reference from weeklyPlanningId={}", weeklyPlanningId);

        // Validate access to weekly planning
        accessControlService.validateWeeklyPlanningAccess(weeklyPlanningId);

        WeeklyPlanning weeklyPlanning = weeklyPlanningRepository.findById(weeklyPlanningId)
            .orElseThrow(() -> new ResourceNotFoundException("WeeklyPlanning not found with id: " + weeklyPlanningId));

        if (weeklyPlanning.getBibliographicReferences().remove(reference)) {
            weeklyPlanningRepository.save(weeklyPlanning);
            log.info("Removed bibliographic reference from weekly planning with id={}", weeklyPlanningId);
        } else {
            log.debug("Bibliographic reference not found in weekly planning with id={}", weeklyPlanningId);
        }
    }

    private WeeklyPlanningResponse mapToResponse(WeeklyPlanning weeklyPlanning) {
        return WeeklyPlanningResponse.builder()
            .id(weeklyPlanning.getId())
            .weekNumber(weeklyPlanning.getWeekNumber())
            .startDate(weeklyPlanning.getStartDate())
            .endDate(weeklyPlanning.getEndDate())
            .bibliographicReferences(weeklyPlanning.getBibliographicReferences())
            .programmaticContentIds(weeklyPlanning.getProgrammaticContents().stream()
                .map(pc -> pc.getId())
                .collect(Collectors.toList()))
            .build();
    }
}

