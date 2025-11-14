package edu.utec.planificador.repository;

import edu.utec.planificador.entity.Modification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModificationRepository extends JpaRepository<Modification, Long> {
    
    @Query("SELECT m FROM Modification m WHERE m.course.id = :courseId ORDER BY m.modificationDate DESC")
    Page<Modification> findByCourseId(@Param("courseId") Long courseId, Pageable pageable);
}
