package edu.utec.planificador.repository;

import edu.utec.planificador.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long>, JpaSpecificationExecutor<Campus> {

    Optional<Campus> findByName(String name);

    List<Campus> findByRegionalTechnologicalInstituteId(Long rtiId);

    boolean existsByName(String name);

    @Query("SELECT c FROM Campus c JOIN c.programs p WHERE p.id = :programId")
    List<Campus> findByProgram(@Param("programId") Long programId);

    @Query("""
        SELECT DISTINCT c FROM Campus c
        JOIN Position position ON c MEMBER OF position.campuses
        WHERE position.user.id = :userId
        AND position.isActive = true
        ORDER BY c.name ASC
        """)
    List<Campus> findByUserId(@Param("userId") Long userId);
}

