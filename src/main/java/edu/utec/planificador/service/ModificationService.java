package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.ModificationResponse;
import edu.utec.planificador.entity.Activity;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.ProgrammaticContent;
import edu.utec.planificador.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ModificationService {

    Page<ModificationResponse> getModificationsByCourse(Long courseId, Pageable pageable);

    void logProgrammaticContentCreation(ProgrammaticContent content, Teacher teacher, Course course);

    void logProgrammaticContentUpdate(ProgrammaticContent oldContent, ProgrammaticContent newContent, Teacher teacher, Course course);

    void logProgrammaticContentDeletion(ProgrammaticContent content, Teacher teacher, Course course);

    void logActivityCreation(Activity activity, Teacher teacher, Course course);

    void logActivityUpdate(Activity oldActivity, Activity newActivity, Teacher teacher, Course course);

    void logActivityDeletion(Activity activity, Teacher teacher, Course course);

    Teacher getCurrentTeacher();

    Course getCourseByWeeklyPlanningId(Long weeklyPlanningId);
}
