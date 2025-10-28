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
@Schema(description = "Campus information")
public class CampusResponse {

    @Schema(description = "Campus ID", example = "1")
    private Long id;

    @Schema(description = "Campus name", example = "UTEC Rivera")
    private String name;
}
