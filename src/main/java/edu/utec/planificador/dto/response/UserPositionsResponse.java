package edu.utec.planificador.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User positions with campuses and RTI information")
public class UserPositionsResponse {

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "User email", example = "juan.perez@utec.edu.uy")
    private String email;

    @Schema(description = "User full name", example = "Juan Pérez")
    private String fullName;

    @Schema(description = "List of user positions with their campuses and RTI")
    private List<PositionResponse> positions;
}

