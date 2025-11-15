package edu.utec.planificador.repository;

import edu.utec.planificador.entity.CurricularUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurricularUnitRepository extends JpaRepository<CurricularUnit, Long> {

    @Query("""
        SELECT DISTINCT cu FROM CurricularUnit cu
        JOIN cu.term t
        JOIN t.program p
        JOIN Campus c ON p MEMBER OF c.programs
        WHERE c.id = :campusId
        ORDER BY cu.name ASC
        """)
    List<CurricularUnit> findByCampusId(@Param("campusId") Long campusId);
}
