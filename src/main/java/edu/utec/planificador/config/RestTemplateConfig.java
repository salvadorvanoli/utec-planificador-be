package edu.utec.planificador.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private final AIAgentProperties aiAgentProperties;

    public RestTemplateConfig(AIAgentProperties aiAgentProperties) {
        this.aiAgentProperties = aiAgentProperties;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) aiAgentProperties.getConnectTimeout());
        factory.setReadTimeout((int) aiAgentProperties.getReadTimeout());
        
        return builder
                .requestFactory(() -> factory)
                .build();
    }
}

