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
@Schema(description = "Course planning suggestions response")
public class SuggestionsResponse {

    @Schema(description = "General analysis of the course planning")
    private String analysis;

    @Schema(description = "Specific pedagogical suggestions for improvement")
    private String pedagogicalSuggestions;
}

