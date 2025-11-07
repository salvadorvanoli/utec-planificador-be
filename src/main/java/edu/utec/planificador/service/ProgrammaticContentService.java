package edu.utec.planificador.service;

import edu.utec.planificador.dto.request.ProgrammaticContentRequest;
import edu.utec.planificador.dto.response.ProgrammaticContentResponse;

public interface ProgrammaticContentService {

    ProgrammaticContentResponse createProgrammaticContent(ProgrammaticContentRequest request);

    ProgrammaticContentResponse getProgrammaticContentById(Long id);

    ProgrammaticContentResponse updateProgrammaticContent(Long id, ProgrammaticContentRequest request);

    void deleteProgrammaticContent(Long id);
}
