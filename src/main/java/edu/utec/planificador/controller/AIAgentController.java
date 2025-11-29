package edu.utec.planificador.controller;

import edu.utec.planificador.dto.request.ChatMessageRequest;
import edu.utec.planificador.dto.request.ReportGenerationRequest;
import edu.utec.planificador.dto.request.SuggestionsRequest;
import edu.utec.planificador.dto.response.ChatResponse;
import edu.utec.planificador.dto.response.ReportResponse;
import edu.utec.planificador.dto.response.SuggestionsResponse;
import edu.utec.planificador.service.AIAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai-agent")
@RequiredArgsConstructor
@Tag(name = "AI Agent", description = "Endpoints para interactuar con el agente de IA pedagógico")
@SecurityRequirement(name = "bearerAuth")
public class AIAgentController {

    private final AIAgentService aiAgentService;

    @PostMapping("/chat")
    @Operation(
        summary = "Enviar mensaje al chatbot pedagógico",
        description = "Envía un mensaje al agente de IA para consultas sobre prácticas pedagógicas. Opcionalmente puede incluir un courseId para contexto específico."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Respuesta exitosa del chatbot",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChatResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Curso no encontrado (si se proporciona courseId)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error al comunicarse con el agente de IA",
            content = @Content
        )
    })
    public ResponseEntity<ChatResponse> sendChatMessage(@Valid @RequestBody ChatMessageRequest request) {
        log.info("POST /ai-agent/chat - Message from user");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String sessionId = authentication.getName();

        ChatResponse response = aiAgentService.sendChatMessage(
            sessionId,
            request.getMessage(),
            request.getCourseId()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/chat/session")
    @Operation(
        summary = "Limpiar sesión de chat del usuario",
        description = "Elimina el historial de conversación del usuario actual con el agente de IA"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sesión limpiada exitosamente",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error al comunicarse con el agente de IA",
            content = @Content
        )
    })
    public ResponseEntity<Map<String, String>> clearChatSession() {
        log.info("DELETE /ai-agent/chat/session - Clear user session");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String sessionId = authentication.getName();

        aiAgentService.clearChatSession(sessionId);

        return ResponseEntity.ok(Map.of(
            "message", "Sesión de chat limpiada exitosamente"
        ));
    }

    @PostMapping("/suggestions")
    @Operation(
        summary = "Obtener sugerencias pedagógicas para un curso",
        description = "Analiza la planificación completa de un curso y proporciona sugerencias de mejora basadas en las mejores prácticas pedagógicas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sugerencias generadas exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuggestionsResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Curso no encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error al comunicarse con el agente de IA",
            content = @Content
        )
    })
    public ResponseEntity<SuggestionsResponse> getSuggestions(@Valid @RequestBody SuggestionsRequest request) {
        log.info("POST /ai-agent/suggestions - CourseId: {}", request.getCourseId());

        SuggestionsResponse response = aiAgentService.getSuggestions(request.getCourseId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/report")
    @Operation(
        summary = "Generar reporte de calidad del curso",
        description = "Genera un reporte completo analizando la calidad del curso basado en estadísticas y la planificación completa"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Reporte generado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReportResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Curso no encontrado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error al comunicarse con el agente de IA",
            content = @Content
        )
    })
    public ResponseEntity<ReportResponse> generateReport(@Valid @RequestBody ReportGenerationRequest request) {
        log.info("POST /ai-agent/report - CourseId: {}", request.getCourseId());

        ReportResponse response = aiAgentService.generateReport(request.getCourseId());

        return ResponseEntity.ok(response);
    }
}

