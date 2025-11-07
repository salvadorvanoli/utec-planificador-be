package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.request.CurricularUnitRequest;
import edu.utec.planificador.dto.response.CurricularUnitResponse;
import edu.utec.planificador.entity.CurricularUnit;
import edu.utec.planificador.entity.Term;
import edu.utec.planificador.enumeration.DomainArea;
import edu.utec.planificador.enumeration.ProfessionalCompetency;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.repository.CurricularUnitRepository;
import edu.utec.planificador.repository.TermRepository;
import edu.utec.planificador.service.CurricularUnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurricularUnitServiceImpl implements CurricularUnitService {

    private final CurricularUnitRepository curricularUnitRepository;
    private final TermRepository termRepository;

    @Override
    @Transactional
    public CurricularUnitResponse createCurricularUnit(CurricularUnitRequest request) {
        log.debug("Creating curricular unit with name: {}", request.getName());
        
        Term term = termRepository.findById(request.getTermId())
            .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + request.getTermId()));
        
        CurricularUnit curricularUnit = new CurricularUnit(
            request.getName(),
            request.getCredits(),
            term
        );
        
        // Set collections
        if (request.getDomainAreas() != null) {
            curricularUnit.getDomainAreas().addAll(request.getDomainAreas());
        }
        
        if (request.getProfessionalCompetencies() != null) {
            curricularUnit.getProfessionalCompetencies().addAll(request.getProfessionalCompetencies());
        }
        
        CurricularUnit savedCurricularUnit = curricularUnitRepository.save(curricularUnit);
        
        log.info("Curricular unit created successfully with id: {}", savedCurricularUnit.getId());
        
        return mapToResponse(savedCurricularUnit);
    }

    @Override
    @Transactional(readOnly = true)
    public CurricularUnitResponse getCurricularUnitById(Long id) {
        log.debug("Getting curricular unit by id: {}", id);
        
        CurricularUnit curricularUnit = curricularUnitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + id));
        
        return mapToResponse(curricularUnit);
    }

    @Override
    @Transactional
    public CurricularUnitResponse updateCurricularUnit(Long id, CurricularUnitRequest request) {
        log.debug("Updating curricular unit with id: {}", id);
        
        CurricularUnit curricularUnit = curricularUnitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + id));
        
        Term term = termRepository.findById(request.getTermId())
            .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + request.getTermId()));
        
        // Update fields
        curricularUnit.setName(request.getName());
        curricularUnit.setCredits(request.getCredits());
        curricularUnit.setTerm(term);
        
        // Update collections
        curricularUnit.getDomainAreas().clear();
        if (request.getDomainAreas() != null) {
            curricularUnit.getDomainAreas().addAll(request.getDomainAreas());
        }
        
        curricularUnit.getProfessionalCompetencies().clear();
        if (request.getProfessionalCompetencies() != null) {
            curricularUnit.getProfessionalCompetencies().addAll(request.getProfessionalCompetencies());
        }
        
        CurricularUnit updatedCurricularUnit = curricularUnitRepository.save(curricularUnit);
        
        log.info("Curricular unit updated successfully with id: {}", updatedCurricularUnit.getId());
        
        return mapToResponse(updatedCurricularUnit);
    }

    @Override
    @Transactional
    public void deleteCurricularUnit(Long id) {
        log.debug("Deleting curricular unit with id: {}", id);
        
        if (!curricularUnitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Curricular unit not found with id: " + id);
        }
        
        curricularUnitRepository.deleteById(id);
        
        log.info("Curricular unit deleted successfully with id: {}", id);
    }

    private CurricularUnitResponse mapToResponse(CurricularUnit curricularUnit) {
        return CurricularUnitResponse.builder()
            .id(curricularUnit.getId())
            .name(curricularUnit.getName())
            .credits(curricularUnit.getCredits())
            .domainAreas(curricularUnit.getDomainAreas())
            .professionalCompetencies(curricularUnit.getProfessionalCompetencies())
            .termId(curricularUnit.getTerm().getId())
            .build();
    }

    // ==================== Domain Areas ====================

    @Override
    @Transactional
    public CurricularUnitResponse addDomainArea(Long curricularUnitId, DomainArea domainArea) {
        log.debug("Adding Domain Area {} to curricular unit {}", domainArea, curricularUnitId);
        
        CurricularUnit curricularUnit = curricularUnitRepository.findById(curricularUnitId)
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + curricularUnitId));
        
        curricularUnit.getDomainAreas().add(domainArea);
        CurricularUnit updatedCurricularUnit = curricularUnitRepository.save(curricularUnit);
        
        log.info("Domain Area {} added to curricular unit {}", domainArea, curricularUnitId);
        
        return mapToResponse(updatedCurricularUnit);
    }

    @Override
    @Transactional
    public CurricularUnitResponse removeDomainArea(Long curricularUnitId, DomainArea domainArea) {
        log.debug("Removing Domain Area {} from curricular unit {}", domainArea, curricularUnitId);
        
        CurricularUnit curricularUnit = curricularUnitRepository.findById(curricularUnitId)
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + curricularUnitId));
        
        boolean removed = curricularUnit.getDomainAreas().remove(domainArea);
        
        if (!removed) {
            log.warn("Domain Area {} was not found in curricular unit {}", domainArea, curricularUnitId);
        }
        
        CurricularUnit updatedCurricularUnit = curricularUnitRepository.save(curricularUnit);
        
        log.info("Domain Area {} removed from curricular unit {}", domainArea, curricularUnitId);
        
        return mapToResponse(updatedCurricularUnit);
    }

    // ==================== Professional Competencies ====================

    @Override
    @Transactional
    public CurricularUnitResponse addProfessionalCompetency(Long curricularUnitId, ProfessionalCompetency competency) {
        log.debug("Adding Professional Competency {} to curricular unit {}", competency, curricularUnitId);
        
        CurricularUnit curricularUnit = curricularUnitRepository.findById(curricularUnitId)
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + curricularUnitId));
        
        curricularUnit.getProfessionalCompetencies().add(competency);
        CurricularUnit updatedCurricularUnit = curricularUnitRepository.save(curricularUnit);
        
        log.info("Professional Competency {} added to curricular unit {}", competency, curricularUnitId);
        
        return mapToResponse(updatedCurricularUnit);
    }

    @Override
    @Transactional
    public CurricularUnitResponse removeProfessionalCompetency(Long curricularUnitId, ProfessionalCompetency competency) {
        log.debug("Removing Professional Competency {} from curricular unit {}", competency, curricularUnitId);
        
        CurricularUnit curricularUnit = curricularUnitRepository.findById(curricularUnitId)
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + curricularUnitId));
        
        boolean removed = curricularUnit.getProfessionalCompetencies().remove(competency);
        
        if (!removed) {
            log.warn("Professional Competency {} was not found in curricular unit {}", competency, curricularUnitId);
        }
        
        CurricularUnit updatedCurricularUnit = curricularUnitRepository.save(curricularUnit);
        
        log.info("Professional Competency {} removed from curricular unit {}", competency, curricularUnitId);
        
        return mapToResponse(updatedCurricularUnit);
    }
}
