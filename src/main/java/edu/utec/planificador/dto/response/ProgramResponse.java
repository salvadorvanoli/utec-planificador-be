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
@Schema(description = "Program information")
public class ProgramResponse {

    @Schema(description = "Program ID", example = "1")
    private Long id;

    @Schema(description = "Program name", example = "Ingeniería en Sistemas")
    private String name;

    @Schema(description = "Program duration in terms", example = "10")
    private Integer durationInTerms;

    @Schema(description = "Total credits", example = "400")
    private Integer totalCredits;
}
