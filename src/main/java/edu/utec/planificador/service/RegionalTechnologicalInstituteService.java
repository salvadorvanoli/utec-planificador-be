package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.RegionalTechnologicalInstituteResponse;

import java.util.List;

public interface RegionalTechnologicalInstituteService {

    List<RegionalTechnologicalInstituteResponse> getRegionalTechnologicalInstitutes(Long userId);
}
