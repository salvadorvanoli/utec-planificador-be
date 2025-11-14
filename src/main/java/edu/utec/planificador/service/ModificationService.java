package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.ModificationResponse;
import edu.utec.planificador.entity.Activity;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.ProgrammaticContent;
import edu.utec.planificador.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ModificationService {

    /**
     * Gets paginated modifications for a course
     */
    Page<ModificationResponse> getModificationsByCourse(Long courseId, Pageable pageable);

    /**
     * Creates a modification record for a programmatic content creation
     */
    void logProgrammaticContentCreation(ProgrammaticContent content, Teacher teacher, Course course);

    /**
     * Creates a modification record for a programmatic content update
     */
    void logProgrammaticContentUpdate(ProgrammaticContent oldContent, ProgrammaticContent newContent, Teacher teacher, Course course);

    /**
     * Creates a modification record for a programmatic content deletion
     */
    void logProgrammaticContentDeletion(ProgrammaticContent content, Teacher teacher, Course course);

    /**
     * Creates a modification record for an activity creation
     */
    void logActivityCreation(Activity activity, Teacher teacher, Course course);

    /**
     * Creates a modification record for an activity update
     */
    void logActivityUpdate(Activity oldActivity, Activity newActivity, Teacher teacher, Course course);

    /**
     * Creates a modification record for an activity deletion
     */
    void logActivityDeletion(Activity activity, Teacher teacher, Course course);
}
