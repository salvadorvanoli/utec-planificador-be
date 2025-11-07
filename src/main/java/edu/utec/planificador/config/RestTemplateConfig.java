package edu.utec.planificador.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    private final AIAgentProperties aiAgentProperties;

    public RestTemplateConfig(AIAgentProperties aiAgentProperties) {
        this.aiAgentProperties = aiAgentProperties;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofMillis(aiAgentProperties.getConnectTimeout()))
                .readTimeout(Duration.ofMillis(aiAgentProperties.getReadTimeout()))
                .build();
    }
}

