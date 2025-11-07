package edu.utec.planificador.dto.response;

import edu.utec.planificador.enumeration.DomainArea;
import edu.utec.planificador.enumeration.ProfessionalCompetency;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Curricular unit information")
public class CurricularUnitResponse {

    @Schema(description = "Curricular unit ID", example = "1")
    private Long id;

    @Schema(description = "Curricular unit name", example = "Programación 1")
    private String name;

    @Schema(description = "Number of credits", example = "8")
    private Integer credits;

    @Schema(description = "Domain areas")
    @Builder.Default
    private Set<DomainArea> domainAreas = new HashSet<>();

    @Schema(description = "Professional competencies")
    @Builder.Default
    private Set<ProfessionalCompetency> professionalCompetencies = new HashSet<>();

    @Schema(description = "Term ID", example = "1")
    private Long termId;
}
