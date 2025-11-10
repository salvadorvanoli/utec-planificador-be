package edu.utec.planificador.repository;

import edu.utec.planificador.entity.RegionalTechnologicalInstitute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionalTechnologicalInstituteRepository extends JpaRepository<RegionalTechnologicalInstitute, Long> {

    Optional<RegionalTechnologicalInstitute> findByName(String name);

    boolean existsByName(String name);

    @Query("""
        SELECT DISTINCT rti FROM RegionalTechnologicalInstitute rti
        JOIN Campus campus ON campus.regionalTechnologicalInstitute.id = rti.id
        JOIN Position position ON campus MEMBER OF position.campuses
        WHERE position.user.id = :userId
        AND position.isActive = true
        ORDER BY rti.name ASC
        """)
    List<RegionalTechnologicalInstitute> findByUserId(@Param("userId") Long userId);
}

