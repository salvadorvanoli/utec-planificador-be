package edu.utec.planificador.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Academic period information")
public class PeriodResponse {
    
    @Schema(description = "Period label in format YYYY-XS (e.g., '2025-1S', '2025-2S')", example = "2025-1S")
    private String period;
}
