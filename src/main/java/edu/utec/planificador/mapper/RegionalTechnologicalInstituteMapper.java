package edu.utec.planificador.mapper;

import edu.utec.planificador.dto.response.RegionalTechnologicalInstituteResponse;
import edu.utec.planificador.entity.RegionalTechnologicalInstitute;
import org.springframework.stereotype.Component;

@Component
public class RegionalTechnologicalInstituteMapper {

    public RegionalTechnologicalInstituteResponse toResponse(RegionalTechnologicalInstitute institute) {
        if (institute == null) {
            return null;
        }

        return RegionalTechnologicalInstituteResponse.builder()
                .id(institute.getId())
                .name(institute.getName())
                .build();
    }
}
