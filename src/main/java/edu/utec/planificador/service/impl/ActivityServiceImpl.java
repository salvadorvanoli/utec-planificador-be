package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.request.ActivityRequest;
import edu.utec.planificador.dto.response.ActivityResponse;
import edu.utec.planificador.entity.Activity;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.ProgrammaticContent;
import edu.utec.planificador.entity.Teacher;
import edu.utec.planificador.enumeration.CognitiveProcess;
import edu.utec.planificador.enumeration.LearningModality;
import edu.utec.planificador.enumeration.LearningResource;
import edu.utec.planificador.enumeration.TeachingStrategy;
import edu.utec.planificador.enumeration.TransversalCompetency;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.repository.ActivityRepository;
import edu.utec.planificador.repository.ProgrammaticContentRepository;
import edu.utec.planificador.service.AccessControlService;
import edu.utec.planificador.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ProgrammaticContentRepository programmaticContentRepository;
    private final AccessControlService accessControlService;
    private final ModificationServiceImpl modificationService;

    @Override
    @Transactional
    public ActivityResponse createActivity(ActivityRequest request) {
        log.debug("Creating activity for programmaticContentId={}", request.getProgrammaticContentId());

        // Validate access to programmatic content
        accessControlService.validateProgrammaticContentAccess(request.getProgrammaticContentId());

        ProgrammaticContent pc = programmaticContentRepository.findById(request.getProgrammaticContentId())
            .orElseThrow(() -> new ResourceNotFoundException("ProgrammaticContent not found with id: " + request.getProgrammaticContentId()));

        Activity activity = new Activity(
            request.getDescription(),
            request.getDurationInMinutes(),
            request.getLearningModality(),
            pc
        );

        activity.setTitle(request.getTitle());
        activity.setColor(request.getColor());

        // Set collections
        if (request.getCognitiveProcesses() != null) {
            request.getCognitiveProcesses().forEach(cp ->
                activity.getCognitiveProcesses().add(CognitiveProcess.valueOf(cp))
            );
        }
        if (request.getTransversalCompetencies() != null) {
            request.getTransversalCompetencies().forEach(tc ->
                activity.getTransversalCompetencies().add(TransversalCompetency.valueOf(tc))
            );
        }
        if (request.getTeachingStrategies() != null) {
            request.getTeachingStrategies().forEach(ts ->
                activity.getTeachingStrategies().add(TeachingStrategy.valueOf(ts))
            );
        }
        if (request.getLearningResources() != null) {
            request.getLearningResources().forEach(lr ->
                activity.getLearningResources().add(LearningResource.valueOf(lr))
            );
        }

        pc.getActivities().add(activity);

        Activity saved = activityRepository.save(activity);
        log.info("Created activity with id={}", saved.getId());

        // Log modification
        Teacher teacher = modificationService.getCurrentTeacher();
        if (teacher != null) {
            Course course = modificationService.getCourseByWeeklyPlanningId(pc.getWeeklyPlanning().getId());
            modificationService.logActivityCreation(saved, teacher, course);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse getActivityById(Long id) {
        // Validate access to activity
        accessControlService.validateActivityAccess(id);

        Activity activity = activityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));

        return mapToResponse(activity);
    }

    @Override
    @Transactional
    public ActivityResponse updateActivity(Long id, ActivityRequest request) {
        // Validate access to both activity and new programmatic content
        accessControlService.validateActivityAccess(id);
        accessControlService.validateProgrammaticContentAccess(request.getProgrammaticContentId());

        Activity activity = activityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));

        ProgrammaticContent pc = programmaticContentRepository.findById(request.getProgrammaticContentId())
            .orElseThrow(() -> new ResourceNotFoundException("ProgrammaticContent not found with id: " + request.getProgrammaticContentId()));

        // Save OLD values BEFORE modifying
        String oldTitle = activity.getTitle();
        String oldDescription = activity.getDescription();
        Integer oldDuration = activity.getDurationInMinutes();
        LearningModality oldModality = activity.getLearningModality();
        String oldColor = activity.getColor();
        Set<CognitiveProcess> oldCognitiveProcesses = new HashSet<>(activity.getCognitiveProcesses());
        Set<TransversalCompetency> oldTransversalCompetencies = new HashSet<>(activity.getTransversalCompetencies());
        Set<TeachingStrategy> oldTeachingStrategies = new HashSet<>(activity.getTeachingStrategies());
        Set<LearningResource> oldLearningResources = new HashSet<>(activity.getLearningResources());

        // Now modify the entity
        activity.setDescription(request.getDescription());
        activity.setDurationInMinutes(request.getDurationInMinutes());
        activity.setLearningModality(request.getLearningModality());
        activity.setTitle(request.getTitle());
        activity.setColor(request.getColor());

        // Update collections
        activity.getCognitiveProcesses().clear();
        if (request.getCognitiveProcesses() != null) {
            request.getCognitiveProcesses().forEach(cp ->
                activity.getCognitiveProcesses().add(CognitiveProcess.valueOf(cp))
            );
        }

        activity.getTransversalCompetencies().clear();
        if (request.getTransversalCompetencies() != null) {
            request.getTransversalCompetencies().forEach(tc ->
                activity.getTransversalCompetencies().add(TransversalCompetency.valueOf(tc))
            );
        }

        activity.getTeachingStrategies().clear();
        if (request.getTeachingStrategies() != null) {
            request.getTeachingStrategies().forEach(ts ->
                activity.getTeachingStrategies().add(TeachingStrategy.valueOf(ts))
            );
        }

        activity.getLearningResources().clear();
        if (request.getLearningResources() != null) {
            request.getLearningResources().forEach(lr ->
                activity.getLearningResources().add(LearningResource.valueOf(lr))
            );
        }

        // If changing programmatic content, update relationships
        if (!activity.getProgrammaticContent().getId().equals(pc.getId())) {
            activity.getProgrammaticContent().getActivities().remove(activity);
            activity.setProgrammaticContent(pc);
            pc.getActivities().add(activity);
        }

        Activity updated = activityRepository.save(activity);
        log.info("Updated activity with id={}", updated.getId());

        // Log modification using saved old values
        Teacher teacher = modificationService.getCurrentTeacher();
        if (teacher != null) {
            Course course = modificationService.getCourseByWeeklyPlanningId(pc.getWeeklyPlanning().getId());
            // Create temporary old activity with saved values
            Activity oldActivitySnapshot = new Activity(oldDescription, oldDuration, oldModality, activity.getProgrammaticContent());
            oldActivitySnapshot.setTitle(oldTitle);
            oldActivitySnapshot.setColor(oldColor);
            oldActivitySnapshot.getCognitiveProcesses().addAll(oldCognitiveProcesses);
            oldActivitySnapshot.getTransversalCompetencies().addAll(oldTransversalCompetencies);
            oldActivitySnapshot.getTeachingStrategies().addAll(oldTeachingStrategies);
            oldActivitySnapshot.getLearningResources().addAll(oldLearningResources);
            modificationService.logActivityUpdate(oldActivitySnapshot, updated, teacher, course);
        }

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteActivity(Long id) {
        // Validate access to activity
        accessControlService.validateActivityAccess(id);

        Activity activity = activityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));

        // Log modification before deletion
        Teacher teacher = modificationService.getCurrentTeacher();
        if (teacher != null) {
            Course course = modificationService.getCourseByWeeklyPlanningId(
                activity.getProgrammaticContent().getWeeklyPlanning().getId());
            modificationService.logActivityDeletion(activity, teacher, course);
        }

        activityRepository.deleteById(id);
        log.info("Deleted activity with id={}", id);
    }

    private ActivityResponse mapToResponse(Activity activity) {
        return ActivityResponse.builder()
            .id(activity.getId())
            .title(activity.getTitle())
            .description(activity.getDescription())
            .color(activity.getColor())
            .durationInMinutes(activity.getDurationInMinutes())
            .learningModality(activity.getLearningModality())
            .programmaticContentId(activity.getProgrammaticContent() != null ? activity.getProgrammaticContent().getId() : null)
            .cognitiveProcesses(activity.getCognitiveProcesses().stream().map(Enum::name).collect(Collectors.toSet()))
            .transversalCompetencies(activity.getTransversalCompetencies().stream().map(Enum::name).collect(Collectors.toSet()))
            .teachingStrategies(activity.getTeachingStrategies().stream().map(Enum::name).collect(Collectors.toSet()))
            .learningResources(activity.getLearningResources().stream().map(Enum::name).collect(Collectors.toSet()))
            .build();
    }
}

