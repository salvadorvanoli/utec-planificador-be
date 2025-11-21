package edu.utec.planificador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("edu.utec.planificador.entity")
@EnableJpaRepositories("edu.utec.planificador.repository")
@EnableScheduling
public class UtecPlanificadorDocenteBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UtecPlanificadorDocenteBackendApplication.class, args);
    }

}
