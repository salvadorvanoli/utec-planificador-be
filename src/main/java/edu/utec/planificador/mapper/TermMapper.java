package edu.utec.planificador.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import edu.utec.planificador.dto.response.ProgramResponse;
import edu.utec.planificador.dto.response.TermResponse;
import edu.utec.planificador.entity.Term;

@Component
@RequiredArgsConstructor
public class TermMapper {

    private final ProgramMapper programMapper;

    public TermResponse toResponse(Term term) {
        if (term == null) {
            return null;
        }

        ProgramResponse program = programMapper.toResponse(term.getProgram());

        return TermResponse.builder()
            .id(term.getId())
            .number(term.getNumber())
            .program(program)
            .build();
    }
}