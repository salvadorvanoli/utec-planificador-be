package edu.utec.planificador.repository;

import edu.utec.planificador.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {

    Optional<Campus> findByName(String name);
    
    boolean existsByName(String name);
}
