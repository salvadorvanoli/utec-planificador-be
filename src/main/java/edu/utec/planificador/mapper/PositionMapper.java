package edu.utec.planificador.mapper;

import edu.utec.planificador.dto.response.PositionResponse;
import edu.utec.planificador.dto.response.RegionalTechnologicalInstituteResponse;
import edu.utec.planificador.entity.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PositionMapper {

    private final CampusMapper campusMapper;
    private final RegionalTechnologicalInstituteMapper rtiMapper;

    public PositionResponse toResponse(Position position) {
        if (position == null) {
            return null;
        }

        RegionalTechnologicalInstituteResponse rtiResponse = null;
        if (!position.getCampuses().isEmpty()) {
            var firstCampus = position.getCampuses().get(0);
            if (firstCampus.getRegionalTechnologicalInstitute() != null) {
                rtiResponse = rtiMapper.toResponse(firstCampus.getRegionalTechnologicalInstitute());
            }
        }

        return PositionResponse.builder()
                .id(position.getId())
                .type(position.getClass().getSimpleName())
                .role(position.getRole())
                .regionalTechnologicalInstitute(rtiResponse)
                .campuses(
                    position.getCampuses().stream()
                        .map(campusMapper::toResponse)
                        .collect(Collectors.toList())
                )
                .isActive(position.getIsActive())
                .build();
    }
}

