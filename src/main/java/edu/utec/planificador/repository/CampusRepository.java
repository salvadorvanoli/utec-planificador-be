package edu.utec.planificador.repository;

import edu.utec.planificador.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {

    Optional<Campus> findByName(String name);

    List<Campus> findByRegionalTechnologicalInstituteId(Long rtiId);

    boolean existsByName(String name);

    @Query("SELECT c FROM Campus c JOIN c.programs p WHERE p.id = :programId")
    List<Campus> findByProgram(@Param("programId") Long programId);
}

