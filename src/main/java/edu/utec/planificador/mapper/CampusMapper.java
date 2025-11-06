package edu.utec.planificador.mapper;

import edu.utec.planificador.dto.response.CampusResponse;
import edu.utec.planificador.entity.Campus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CampusMapper {

    private final RegionalTechnologicalInstituteMapper rtiMapper;

    public CampusResponse toResponse(Campus campus) {
        if (campus == null) {
            return null;
        }

        return CampusResponse.builder()
                .id(campus.getId())
                .name(campus.getName())
                .regionalTechnologicalInstitute(
                    rtiMapper.toResponse(campus.getRegionalTechnologicalInstitute())
                )
                .build();
    }

    public CampusResponse toSimplifiedResponse(Campus campus) {
        if (campus == null) {
            return null;
        }

        return CampusResponse.builder()
                .id(campus.getId())
                .name(campus.getName())
                .build();
    }
}
