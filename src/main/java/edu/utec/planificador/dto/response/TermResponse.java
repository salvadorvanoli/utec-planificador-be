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
@Schema(description = "Term information")
public class TermResponse {

    @Schema(description = "Term ID", example = "1")
    private Long id;

    @Schema(description = "Term number", example = "1")
    private Integer number;

    @Schema(description = "Program information")
    private ProgramResponse program;
}
