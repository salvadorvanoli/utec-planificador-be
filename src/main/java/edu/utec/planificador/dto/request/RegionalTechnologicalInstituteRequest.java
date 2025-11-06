package edu.utec.planificador.dto.request;

import edu.utec.planificador.util.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Regional Technological Institute creation/update request")
public class RegionalTechnologicalInstituteRequest {

    @Schema(description = "Institute name", example = "ITR Centro Sur")
    @NotBlank(message = "{validation.rti.name.required}")
    @Size(max = Constants.MAX_RTI_NAME_LENGTH, message = "{validation.rti.name.size}")
    private String name;
}
