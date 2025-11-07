package edu.utec.planificador.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai.agent")
@Getter
@Setter
public class AIAgentProperties {

    private String baseUrl = "http://localhost:8000";
    private int connectTimeout = 10000;
    private int readTimeout = 30000;
}

