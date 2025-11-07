package edu.utec.planificador.service.impl;

import edu.utec.planificador.config.AIAgentProperties;
import edu.utec.planificador.dto.aiagent.*;
import edu.utec.planificador.dto.response.ChatResponse;
import edu.utec.planificador.dto.response.ReportResponse;
import edu.utec.planificador.dto.response.SuggestionsResponse;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.exception.AIAgentException;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.mapper.CoursePlanningMapper;
import edu.utec.planificador.mapper.CourseStatisticsMapper;
import edu.utec.planificador.repository.CourseRepository;
import edu.utec.planificador.service.AIAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAgentServiceImpl implements AIAgentService {

    private final RestTemplate restTemplate;
    private final AIAgentProperties aiAgentProperties;
    private final CourseRepository courseRepository;
    private final CoursePlanningMapper coursePlanningMapper;
    private final CourseStatisticsMapper courseStatisticsMapper;

    @Override
    @Transactional(readOnly = true)
    public ChatResponse sendChatMessage(String sessionId, String message, Long courseId) {
        log.info("Sending chat message for session: {}, courseId: {}", sessionId, courseId);

        AIChatRequest request = AIChatRequest.builder()
                .sessionId(sessionId)
                .message(message)
                .build();

        // If courseId is provided, fetch and include course planning
        if (courseId != null) {
            Course course = getCourseWithDetails(courseId);
            CoursePlanningDto coursePlanningDto = coursePlanningMapper.toDto(course);
            request.setCoursePlanning(coursePlanningDto);
        }

        try {
            String url = aiAgentProperties.getBaseUrl() + "/agent/chat/message";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AIChatRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ChatResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ChatResponse.class
            );

            if (response.getBody() == null) {
                throw new AIAgentException("Respuesta vacía del agente de IA");
            }

            log.info("Chat message sent successfully");
            return response.getBody();

        } catch (RestClientException e) {
            log.error("Error comunicándose con el agente de IA: {}", e.getMessage());
            throw new AIAgentException("Error al comunicarse con el agente de IA: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SuggestionsResponse getSuggestions(Long courseId) {
        log.info("Getting suggestions for course: {}", courseId);

        Course course = getCourseWithDetails(courseId);
        CoursePlanningDto coursePlanningDto = coursePlanningMapper.toDto(course);

        AISuggestionsRequest request = AISuggestionsRequest.builder()
                .coursePlanning(coursePlanningDto)
                .build();

        try {
            String url = aiAgentProperties.getBaseUrl() + "/agent/suggestions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AISuggestionsRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<SuggestionsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    SuggestionsResponse.class
            );

            if (response.getBody() == null) {
                throw new AIAgentException("Respuesta vacía del agente de IA");
            }

            log.info("Suggestions retrieved successfully");
            return response.getBody();

        } catch (RestClientException e) {
            log.error("Error comunicándose con el agente de IA: {}", e.getMessage());
            throw new AIAgentException("Error al comunicarse con el agente de IA: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse generateReport(Long courseId) {
        log.info("Generating report for course: {}", courseId);

        Course course = getCourseWithDetails(courseId);
        CoursePlanningDto coursePlanningDto = coursePlanningMapper.toDto(course);
        AIReportRequest.CourseStatisticsDto statistics = courseStatisticsMapper.calculateStatistics(course);

        AIReportRequest request = AIReportRequest.builder()
                .courseId(String.valueOf(courseId))
                .statistics(statistics)
                .coursePlanning(coursePlanningDto)
                .build();

        try {
            String url = aiAgentProperties.getBaseUrl() + "/agent/report/generate";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AIReportRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ReportResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ReportResponse.class
            );

            if (response.getBody() == null) {
                throw new AIAgentException("Respuesta vacía del agente de IA");
            }

            log.info("Report generated successfully");
            return response.getBody();

        } catch (RestClientException e) {
            log.error("Error comunicándose con el agente de IA: {}", e.getMessage());
            throw new AIAgentException("Error al comunicarse con el agente de IA: " + e.getMessage());
        }
    }

    @Override
    public void clearChatSession(String sessionId) {
        log.info("Clearing chat session: {}", sessionId);

        try {
            String url = aiAgentProperties.getBaseUrl() + "/agent/chat/session/" + sessionId;

            restTemplate.delete(url);

            log.info("Chat session cleared successfully");

        } catch (RestClientException e) {
            log.error("Error comunicándose con el agente de IA: {}", e.getMessage());
            throw new AIAgentException("Error al comunicarse con el agente de IA: " + e.getMessage());
        }
    }

    private Course getCourseWithDetails(Long courseId) {
        return courseRepository.findByIdWithFullDetails(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
    }
}

