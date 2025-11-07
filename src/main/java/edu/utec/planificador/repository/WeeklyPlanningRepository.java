package edu.utec.planificador.repository;

import edu.utec.planificador.entity.WeeklyPlanning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyPlanningRepository extends JpaRepository<WeeklyPlanning, Long> {
    
    @Query("SELECT wp FROM Course c JOIN c.weeklyPlannings wp WHERE c.id = :courseId")
    List<WeeklyPlanning> findByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT wp FROM Course c JOIN c.weeklyPlannings wp WHERE c.id = :courseId AND wp.weekNumber = :weekNumber")
    Optional<WeeklyPlanning> findByCourseIdAndWeekNumber(@Param("courseId") Long courseId, @Param("weekNumber") Integer weekNumber);
    
    @Query("SELECT wp FROM Course c JOIN c.weeklyPlannings wp WHERE c.id = :courseId AND :date BETWEEN wp.startDate AND wp.endDate")
    Optional<WeeklyPlanning> findByCourseIdAndDate(@Param("courseId") Long courseId, @Param("date") LocalDate date);
}
