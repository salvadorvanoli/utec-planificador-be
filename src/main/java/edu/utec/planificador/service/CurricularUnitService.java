package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.CurricularUnitResponse;

import java.util.List;

public interface CurricularUnitService {

    List<CurricularUnitResponse> getCurricularUnits(Long campusId);

    CurricularUnitResponse getCurricularUnitById(Long id);
}
