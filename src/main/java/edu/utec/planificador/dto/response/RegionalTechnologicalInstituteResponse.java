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
@Schema(description = "Regional Technological Institute information")
public class RegionalTechnologicalInstituteResponse {

    @Schema(description = "Institute ID", example = "1")
    private Long id;

    @Schema(description = "Institute name", example = "ITR Centro Sur")
    private String name;
}
