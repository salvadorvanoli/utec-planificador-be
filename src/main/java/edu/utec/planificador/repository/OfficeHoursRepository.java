package edu.utec.planificador.repository;

import edu.utec.planificador.entity.OfficeHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfficeHoursRepository extends JpaRepository<OfficeHours, Long> {

    @Query("SELECT oh FROM OfficeHours oh WHERE oh.course.id = :courseId ORDER BY oh.date ASC, oh.startHour ASC")
    List<OfficeHours> findByCourseId(@Param("courseId") Long courseId);
}
