package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.request.ProgrammaticContentRequest;
import edu.utec.planificador.dto.response.ProgrammaticContentResponse;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.ProgrammaticContent;
import edu.utec.planificador.entity.Teacher;
import edu.utec.planificador.entity.WeeklyPlanning;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.repository.CourseRepository;
import edu.utec.planificador.repository.ProgrammaticContentRepository;
import edu.utec.planificador.repository.WeeklyPlanningRepository;
import edu.utec.planificador.service.AccessControlService;
import edu.utec.planificador.service.ModificationService;
import edu.utec.planificador.service.ProgrammaticContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgrammaticContentServiceImpl implements ProgrammaticContentService {

    private final ProgrammaticContentRepository programmaticContentRepository;
    private final WeeklyPlanningRepository weeklyPlanningRepository;
    private final CourseRepository courseRepository;
    private final AccessControlService accessControlService;
    private final ModificationService modificationService;

    @Override
    @Transactional
    public ProgrammaticContentResponse createProgrammaticContent(ProgrammaticContentRequest request) {
        log.debug("Creating programmatic content for weeklyPlanningId={}", request.getWeeklyPlanningId());

        // Find the course associated with this weekly planning
        Course course = courseRepository.findByWeeklyPlanningId(request.getWeeklyPlanningId())
            .orElseThrow(() -> new ResourceNotFoundException("Course not found for weekly planning with id: " + request.getWeeklyPlanningId()));

        // Validate write access to the course (ensures teachers can only modify their own courses)
        accessControlService.validateCourseWriteAccess(course.getId());

        WeeklyPlanning week = weeklyPlanningRepository.findById(request.getWeeklyPlanningId())
            .orElseThrow(() -> new ResourceNotFoundException("Weekly planning not found with id: " + request.getWeeklyPlanningId()));

        ProgrammaticContent pc = new ProgrammaticContent(request.getTitle(), request.getContent(), week);
        pc.setColor(request.getColor());
        week.getProgrammaticContents().add(pc);

        ProgrammaticContent saved = programmaticContentRepository.save(pc);
        log.info("Created programmatic content with id={}", saved.getId());

        // Log modification
        Teacher teacher = modificationService.getCurrentTeacher();
        if (teacher != null) {
            modificationService.logProgrammaticContentCreation(saved, teacher, course);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgrammaticContentResponse getProgrammaticContentById(Long id) {
        // Validate access to programmatic content
        accessControlService.validateProgrammaticContentAccess(id);

        ProgrammaticContent pc = programmaticContentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ProgrammaticContent not found with id: " + id));

        return mapToResponse(pc);
    }

    @Override
    @Transactional
    public ProgrammaticContentResponse updateProgrammaticContent(Long id, ProgrammaticContentRequest request) {
        Course course = courseRepository.findByProgrammaticContentId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found for programmatic content with id: " + id));

        accessControlService.validateCourseWriteAccess(course.getId());

        ProgrammaticContent pc = programmaticContentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ProgrammaticContent not found with id: " + id));

        WeeklyPlanning week = weeklyPlanningRepository.findById(request.getWeeklyPlanningId())
            .orElseThrow(() -> new ResourceNotFoundException("Weekly planning not found with id: " + request.getWeeklyPlanningId()));

        // Save OLD values BEFORE modifying
        String oldTitle = pc.getTitle();
        String oldContent = pc.getContent();
        String oldColor = pc.getColor();

        // Now modify the entity
        pc.setTitle(request.getTitle());
        pc.setContent(request.getContent());
        pc.setColor(request.getColor());

        // If changing week, update relationships
        if (!pc.getWeeklyPlanning().getId().equals(week.getId())) {
            pc.getWeeklyPlanning().getProgrammaticContents().remove(pc);
            pc.setWeeklyPlanning(week);
            week.getProgrammaticContents().add(pc);
        }

        ProgrammaticContent updated = programmaticContentRepository.save(pc);
        log.info("Updated programmatic content with id={}", updated.getId());

        // Log modification using saved old values
        Teacher teacher = modificationService.getCurrentTeacher();
        if (teacher != null) {
            // Create temporary old content with saved values
            ProgrammaticContent oldContentSnapshot = new ProgrammaticContent(oldTitle, oldContent, pc.getWeeklyPlanning());
            oldContentSnapshot.setColor(oldColor);
            modificationService.logProgrammaticContentUpdate(oldContentSnapshot, updated, teacher, course);
        }

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProgrammaticContent(Long id) {
        Course course = courseRepository.findByProgrammaticContentId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found for programmatic content with id: " + id));

        accessControlService.validateCourseWriteAccess(course.getId());

        ProgrammaticContent pc = programmaticContentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ProgrammaticContent not found with id: " + id));

        // Log modification before deletion
        Teacher teacher = modificationService.getCurrentTeacher();
        if (teacher != null) {
            modificationService.logProgrammaticContentDeletion(pc, teacher, course);
        }

        programmaticContentRepository.deleteById(id);
        log.info("Deleted programmatic content with id={}", id);
    }

    private ProgrammaticContentResponse mapToResponse(ProgrammaticContent pc) {
        return ProgrammaticContentResponse.builder()
            .id(pc.getId())
            .title(pc.getTitle())
            .content(pc.getContent())
            .color(pc.getColor())
            .weeklyPlanningId(pc.getWeeklyPlanning() != null ? pc.getWeeklyPlanning().getId() : null)
            .activityIds(pc.getActivities().stream().map(a -> a.getId()).collect(Collectors.toList()))
            .build();
    }
}

