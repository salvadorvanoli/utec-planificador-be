package edu.utec.planificador.dto.response;

import edu.utec.planificador.enumeration.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User position information")
public class PositionResponse {

    @Schema(description = "Position ID", example = "1")
    private Long id;

    @Schema(
        description = "Position type", 
        example = "TEACHER"
    )
    private String type;

    @Schema(
        description = "Role assigned to this position", 
        example = "TEACHER"
    )
    private Role role;

    @Schema(description = "Regional Technological Institute associated with this position")
    private RegionalTechnologicalInstituteResponse regionalTechnologicalInstitute;

    @Schema(description = "List of campuses where this position applies")
    @Builder.Default
    private List<CampusResponse> campuses = new ArrayList<>();

    @Schema(description = "Whether this position is currently active", example = "true")
    private Boolean isActive;
}
