package edu.utec.planificador.repository;

import edu.utec.planificador.entity.CurricularUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurricularUnitRepository extends JpaRepository<CurricularUnit, Long> {
}
