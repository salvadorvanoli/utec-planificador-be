package edu.utec.planificador.repository;

import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.ProgrammaticContent;
import edu.utec.planificador.entity.WeeklyPlanning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    // Primera query: cargar el curso con la estructura básica
    @Query("""
        SELECT DISTINCT c FROM Course c
        LEFT JOIN FETCH c.curricularUnit cu
        LEFT JOIN FETCH cu.term t
        LEFT JOIN FETCH t.program p
        LEFT JOIN FETCH c.weeklyPlannings wp
        WHERE c.id = :courseId
        """)
    Optional<Course> findByIdWithWeeklyPlannings(@Param("courseId") Long courseId);

    // Segunda query: cargar programmatic contents
    @Query("""
        SELECT DISTINCT wp FROM WeeklyPlanning wp
        LEFT JOIN FETCH wp.programmaticContents pc
        WHERE wp.id IN (
            SELECT wp2.id FROM Course c
            JOIN c.weeklyPlannings wp2
            WHERE c.id = :courseId
        )
        """)
    List<WeeklyPlanning> loadProgrammaticContents(@Param("courseId") Long courseId);

    // Tercera query: cargar activities de programmatic contents
    @Query("""
        SELECT DISTINCT pc FROM ProgrammaticContent pc
        LEFT JOIN FETCH pc.activities
        WHERE pc.id IN (
            SELECT pc2.id FROM Course c
            JOIN c.weeklyPlannings wp
            JOIN wp.programmaticContents pc2
            WHERE c.id = :courseId
        )
        """)
    List<ProgrammaticContent> loadProgrammaticContentActivities(@Param("courseId") Long courseId);

    // Metodo helper para cargar lo necesario
    default Optional<Course> findByIdWithFullDetails(Long courseId) {
        Optional<Course> courseOpt = findByIdWithWeeklyPlannings(courseId);
        if (courseOpt.isPresent()) {
            loadProgrammaticContents(courseId);
            loadProgrammaticContentActivities(courseId);
        }
        return courseOpt;
    }

    // Query optimizada para encontrar curso por weekly planning ID
    @Query("""
        SELECT c FROM Course c
        JOIN c.weeklyPlannings wp
        WHERE wp.id = :weeklyPlanningId
        """)
    Optional<Course> findByWeeklyPlanningId(@Param("weeklyPlanningId") Long weeklyPlanningId);

    // Teacher ownership validation queries
    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Course c
        JOIN c.teachers t
        JOIN c.curricularUnit cu
        JOIN cu.term term
        WHERE term.program.id = :programId
        AND t.user.id = :userId
        """)
    boolean existsByProgramIdAndUserId(@Param("programId") Long programId, @Param("userId") Long userId);

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Course c
        JOIN c.teachers t
        JOIN c.curricularUnit cu
        WHERE cu.term.id = :termId
        AND t.user.id = :userId
        """)
    boolean existsByTermIdAndUserId(@Param("termId") Long termId, @Param("userId") Long userId);

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Course c
        JOIN c.teachers t
        WHERE c.curricularUnit.id = :curricularUnitId
        AND t.user.id = :userId
        """)
    boolean existsByCurricularUnitIdAndUserId(@Param("curricularUnitId") Long curricularUnitId, @Param("userId") Long userId);
}
