package edu.utec.planificador.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Chat response from AI agent")
public class ChatResponse {

    @Schema(description = "AI agent reply", example = "El Aprendizaje Basado en Problemas (ABP) es una metodología...")
    private String reply;
}

