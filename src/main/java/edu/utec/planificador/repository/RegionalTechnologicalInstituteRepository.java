package edu.utec.planificador.repository;

import edu.utec.planificador.entity.RegionalTechnologicalInstitute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionalTechnologicalInstituteRepository extends JpaRepository<RegionalTechnologicalInstitute, Long> {

    Optional<RegionalTechnologicalInstitute> findByName(String name);

    boolean existsByName(String name);
}

