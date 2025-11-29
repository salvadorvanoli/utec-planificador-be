package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.response.ModificationResponse;
import edu.utec.planificador.entity.Activity;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.Modification;
import edu.utec.planificador.entity.ProgrammaticContent;
import edu.utec.planificador.entity.Teacher;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.enumeration.ModificationType;
import edu.utec.planificador.repository.CourseRepository;
import edu.utec.planificador.repository.ModificationRepository;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.service.AccessControlService;
import edu.utec.planificador.service.MessageService;
import edu.utec.planificador.service.ModificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModificationServiceImpl implements ModificationService {

    private final ModificationRepository modificationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final AccessControlService accessControlService;
    private final MessageService messageService;

    @Override
    @Transactional(readOnly = true)
    public Page<ModificationResponse> getModificationsByCourse(Long courseId, Pageable pageable) {
        log.debug("Getting modifications for courseId={}, page={}, size={}", 
            courseId, pageable.getPageNumber(), pageable.getPageSize());

        accessControlService.validateCourseAccess(courseId);

        Page<Modification> modifications = modificationRepository.findByCourseId(courseId, pageable);

        return modifications.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void logProgrammaticContentCreation(ProgrammaticContent content, Teacher teacher, Course course) {
        String title = content.getTitle() != null ? content.getTitle() : "Sin título";
        String description = String.format("Se creó el contenido programático '%s'", title);
        
        log.debug("Logging content creation - title: {}, description: {}", title, description);
        saveModification(description, ModificationType.CREATE, teacher, course);
    }

    @Override
    @Transactional
    public void logProgrammaticContentUpdate(ProgrammaticContent oldContent, ProgrammaticContent newContent, Teacher teacher, Course course) {
        String title = newContent.getTitle() != null ? newContent.getTitle() : "Sin título";
        StringBuilder changes = new StringBuilder(String.format("Se modificó el contenido programático '%s'", title));

        if (!equals(oldContent.getTitle(), newContent.getTitle())) {
            String oldTitle = oldContent.getTitle() != null ? oldContent.getTitle() : "Sin título";
            String newTitle = newContent.getTitle() != null ? newContent.getTitle() : "Sin título";
            changes.append(String.format("; el título era: '%s'; ahora es: '%s'", oldTitle, newTitle));
        }

        if (!equals(oldContent.getContent(), newContent.getContent())) {
            changes.append("; se actualizó el contenido");
        }

        if (!equals(oldContent.getColor(), newContent.getColor())) {
            changes.append(String.format("; el color era: '%s'; ahora es: '%s'",
                oldContent.getColor() != null ? oldContent.getColor() : "Sin color",
                newContent.getColor() != null ? newContent.getColor() : "Sin color"));
        }

        saveModification(changes.toString(), ModificationType.UPDATE, teacher, course);
    }

    @Override
    @Transactional
    public void logProgrammaticContentDeletion(ProgrammaticContent content, Teacher teacher, Course course) {
        String title = content.getTitle() != null ? content.getTitle() : "Sin título";
        String description = String.format("Se eliminó el contenido programático '%s'", title);

        saveModification(description, ModificationType.DELETE, teacher, course);
    }

    @Override
    @Transactional
    public void logActivityCreation(Activity activity, Teacher teacher, Course course) {
        String title = activity.getTitle() != null ? activity.getTitle() : "Sin título";
        String description = String.format("Se creó la actividad '%s'", title);
        
        log.debug("Logging activity creation - title: {}, description: {}", title, description);
        saveModification(description, ModificationType.CREATE, teacher, course);
    }

    @Override
    @Transactional
    public void logActivityUpdate(Activity oldActivity, Activity newActivity, Teacher teacher, Course course) {
        String title = newActivity.getTitle() != null ? newActivity.getTitle() : "Sin título";
        StringBuilder changes = new StringBuilder(String.format("Se modificó la actividad '%s'", title));

        if (!equals(oldActivity.getTitle(), newActivity.getTitle())) {
            changes.append(String.format("; el título era: '%s'; ahora es: '%s'",
                oldActivity.getTitle() != null ? oldActivity.getTitle() : "Sin título",
                newActivity.getTitle() != null ? newActivity.getTitle() : "Sin título"));
        }

        if (!equals(oldActivity.getDescription(), newActivity.getDescription())) {
            changes.append("; se actualizó la descripción");
        }

        if (!equals(oldActivity.getDurationInMinutes(), newActivity.getDurationInMinutes())) {
            changes.append(String.format("; la duración era: %d minutos; ahora es: %d minutos",
                oldActivity.getDurationInMinutes(), newActivity.getDurationInMinutes()));
        }

        if (!equals(oldActivity.getLearningModality(), newActivity.getLearningModality())) {
            changes.append(String.format("; la modalidad era: %s; ahora es: %s",
                oldActivity.getLearningModality(), newActivity.getLearningModality()));
        }

        if (!oldActivity.getCognitiveProcesses().equals(newActivity.getCognitiveProcesses())) {
            changes.append(String.format("; los procesos cognitivos eran: [%s]; ahora son: [%s]",
                oldActivity.getCognitiveProcesses().stream().map(Enum::name).collect(Collectors.joining(", ")),
                newActivity.getCognitiveProcesses().stream().map(Enum::name).collect(Collectors.joining(", "))));
        }

        if (!oldActivity.getTransversalCompetencies().equals(newActivity.getTransversalCompetencies())) {
            changes.append(String.format("; las competencias transversales eran: [%s]; ahora son: [%s]",
                oldActivity.getTransversalCompetencies().stream().map(Enum::name).collect(Collectors.joining(", ")),
                newActivity.getTransversalCompetencies().stream().map(Enum::name).collect(Collectors.joining(", "))));
        }

        if (!oldActivity.getTeachingStrategies().equals(newActivity.getTeachingStrategies())) {
            changes.append(String.format("; las estrategias de enseñanza eran: [%s]; ahora son: [%s]",
                oldActivity.getTeachingStrategies().stream().map(Enum::name).collect(Collectors.joining(", ")),
                newActivity.getTeachingStrategies().stream().map(Enum::name).collect(Collectors.joining(", "))));
        }

        if (!oldActivity.getLearningResources().equals(newActivity.getLearningResources())) {
            changes.append(String.format("; los recursos de aprendizaje eran: [%s]; ahora son: [%s]",
                oldActivity.getLearningResources().stream().map(Enum::name).collect(Collectors.joining(", ")),
                newActivity.getLearningResources().stream().map(Enum::name).collect(Collectors.joining(", "))));
        }

        if (!equals(oldActivity.getColor(), newActivity.getColor())) {
            changes.append(String.format("; el color era: '%s'; ahora es: '%s'",
                oldActivity.getColor() != null ? oldActivity.getColor() : "Sin color",
                newActivity.getColor() != null ? newActivity.getColor() : "Sin color"));
        }

        saveModification(changes.toString(), ModificationType.UPDATE, teacher, course);
    }

    @Override
    @Transactional
    public void logActivityDeletion(Activity activity, Teacher teacher, Course course) {
        String title = activity.getTitle() != null ? activity.getTitle() : "Sin título";
        String description = String.format("Se eliminó la actividad '%s'", title);

        saveModification(description, ModificationType.DELETE, teacher, course);
    }

    private void saveModification(String description, ModificationType type, Teacher teacher, Course course) {
        Modification modification = new Modification(description, type, teacher, course);
        modificationRepository.save(modification);
        log.info("Created modification: {}", description);
    }

    private boolean equals(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    public Teacher getCurrentTeacher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        User fullUser = userRepository.findByIdWithPositions(user.getId())
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("error.user.not-found")));

        return fullUser.getPositions().stream()
            .filter(position -> position instanceof Teacher)
            .filter(position -> position.getIsActive())
            .map(position -> (Teacher) position)
            .findFirst()
            .orElse(null);
    }

    public Course getCourseByWeeklyPlanningId(Long weeklyPlanningId) {
        return courseRepository.findAll().stream()
            .filter(course -> course.getWeeklyPlannings().stream()
                .anyMatch(wp -> wp.getId().equals(weeklyPlanningId)))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("error.course.not-found-for-weekly-planning")));
    }

    private ModificationResponse mapToResponse(Modification modification) {
        String teacherName = modification.getTeacher().getUser().getPersonalData().getName();
        String teacherLastName = modification.getTeacher().getUser().getPersonalData().getLastName();
        String fullName = (teacherName != null ? teacherName : "") + " " + (teacherLastName != null ? teacherLastName : "");
        
        return ModificationResponse.builder()
            .id(modification.getId())
            .modificationDate(modification.getModificationDate())
            .description(modification.getDescription())
            .type(modification.getType())
            .teacherId(modification.getTeacher().getId())
            .teacherName(fullName.trim())
            .courseId(modification.getCourse().getId())
            .build();
    }
}
