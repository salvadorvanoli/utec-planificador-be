package edu.utec.planificador.dto.aiagent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIChatRequest {

    @JsonProperty("session_id")
    private String sessionId;

    private String message;

    @JsonProperty("coursePlanning")
    private CoursePlanningDto coursePlanning;
}

