package edu.utec.planificador.dto.request;

import edu.utec.planificador.enumeration.DomainArea;
import edu.utec.planificador.enumeration.ProfessionalCompetency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Curricular unit creation/update request")
public class CurricularUnitRequest {

    @Schema(description = "Curricular unit name", example = "Programación 1")
    @NotBlank(message = "{validation.curricularUnit.name.required}")
    @Size(max = 100, message = "{validation.curricularUnit.name.size}")
    private String name;

    @Schema(description = "Number of credits", example = "8")
    @NotNull(message = "{validation.curricularUnit.credits.required}")
    @Min(value = 1, message = "{validation.curricularUnit.credits.min}")
    private Integer credits;

    @Schema(description = "Domain areas")
    @Builder.Default
    private Set<DomainArea> domainAreas = new HashSet<>();

    @Schema(description = "Professional competencies")
    @Builder.Default
    private Set<ProfessionalCompetency> professionalCompetencies = new HashSet<>();

    @Schema(description = "Term ID", example = "1")
    @NotNull(message = "{validation.curricularUnit.termId.required}")
    private Long termId;
}
