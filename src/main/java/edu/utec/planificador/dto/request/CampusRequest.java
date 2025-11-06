package edu.utec.planificador.dto.request;

import edu.utec.planificador.datatype.Location;
import edu.utec.planificador.util.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Campus creation/update request")
public class CampusRequest {

    @Schema(description = "Campus name", example = "UTEC Rivera")
    @NotBlank(message = "{validation.campus.name.required}")
    @Size(max = Constants.MAX_CAMPUS_NAME_LENGTH, message = "{validation.campus.name.size}")
    private String name;

    @Schema(description = "Campus location information")
    @Valid
    private Location location;

    @Schema(description = "Regional Technological Institute ID", example = "1")
    @NotNull(message = "{validation.campus.rti.required}")
    private Long regionalTechnologicalInstituteId;
}
