package edu.utec.planificador.service;

import edu.utec.planificador.dto.request.CurricularUnitRequest;
import edu.utec.planificador.dto.response.CurricularUnitResponse;
import edu.utec.planificador.enumeration.DomainArea;
import edu.utec.planificador.enumeration.ProfessionalCompetency;

public interface CurricularUnitService {

    CurricularUnitResponse createCurricularUnit(CurricularUnitRequest request);

    CurricularUnitResponse getCurricularUnitById(Long id);

    CurricularUnitResponse updateCurricularUnit(Long id, CurricularUnitRequest request);

    void deleteCurricularUnit(Long id);

    // Domain Areas
    CurricularUnitResponse addDomainArea(Long curricularUnitId, DomainArea domainArea);

    CurricularUnitResponse removeDomainArea(Long curricularUnitId, DomainArea domainArea);

    // Professional Competencies
    CurricularUnitResponse addProfessionalCompetency(Long curricularUnitId, ProfessionalCompetency competency);

    CurricularUnitResponse removeProfessionalCompetency(Long curricularUnitId, ProfessionalCompetency competency);
}
