package edu.utec.planificador.mapper;

import edu.utec.planificador.dto.response.CurricularUnitResponse;
import edu.utec.planificador.dto.response.TermResponse;
import edu.utec.planificador.entity.CurricularUnit;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurricularUnitMapper {

    private final TermMapper termMapper;

    public CurricularUnitResponse toResponse(CurricularUnit curricularUnit) {
        if (curricularUnit == null) {
            return null;
        }

        TermResponse term = termMapper.toResponse(curricularUnit.getTerm());

        return CurricularUnitResponse.builder()
            .id(curricularUnit.getId())
            .name(curricularUnit.getName())
            .credits(curricularUnit.getCredits())
            .domainAreas(curricularUnit.getDomainAreas())
            .professionalCompetencies(curricularUnit.getProfessionalCompetencies())
            .term(term)
            .build();
    }
}
