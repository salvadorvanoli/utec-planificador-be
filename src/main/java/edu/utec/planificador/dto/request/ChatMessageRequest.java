package edu.utec.planificador.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Chat message request from frontend")
public class ChatMessageRequest {

    @Schema(description = "User message to the AI agent", example = "¿Cómo implemento el Aprendizaje Basado en Problemas?")
    @NotBlank(message = "El mensaje no puede estar vacío")
    private String message;

    @Schema(description = "Course ID for context (optional)", example = "1")
    private Long courseId;
}

