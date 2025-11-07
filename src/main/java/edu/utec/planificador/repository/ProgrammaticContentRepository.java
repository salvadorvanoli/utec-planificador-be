package edu.utec.planificador.repository;

import edu.utec.planificador.entity.ProgrammaticContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgrammaticContentRepository extends JpaRepository<ProgrammaticContent, Long> {
}
