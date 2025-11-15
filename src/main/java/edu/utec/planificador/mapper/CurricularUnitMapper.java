package edu.utec.planificador.mapper;

import edu.utec.planificador.dto.response.CurricularUnitResponse;
import edu.utec.planificador.entity.CurricularUnit;
import org.springframework.stereotype.Component;

@Component
public class CurricularUnitMapper {

    public CurricularUnitResponse toResponse(CurricularUnit curricularUnit) {
        if (curricularUnit == null) {
            return null;
        }

        return CurricularUnitResponse.builder()
            .id(curricularUnit.getId())
            .name(curricularUnit.getName())
            .credits(curricularUnit.getCredits())
            .domainAreas(curricularUnit.getDomainAreas())
            .professionalCompetencies(curricularUnit.getProfessionalCompetencies())
            .termId(curricularUnit.getTerm().getId())
            .build();
    }
}
