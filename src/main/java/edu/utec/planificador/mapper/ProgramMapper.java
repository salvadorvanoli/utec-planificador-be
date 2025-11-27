package edu.utec.planificador.mapper;

import org.springframework.stereotype.Component;

import edu.utec.planificador.dto.response.ProgramResponse;
import edu.utec.planificador.entity.Program;

@Component
public class ProgramMapper {

    public ProgramResponse toResponse(Program program) {
        if (program == null) {
            return null;
        }

        return ProgramResponse.builder()
            .id(program.getId())
            .name(program.getName())
            .durationInTerms(program.getDurationInTerms())
            .totalCredits(program.getTotalCredits())
            .build();
    }
}
