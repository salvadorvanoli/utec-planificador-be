package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.response.CurricularUnitResponse;
import edu.utec.planificador.entity.CurricularUnit;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.mapper.CurricularUnitMapper;
import edu.utec.planificador.repository.CurricularUnitRepository;
import edu.utec.planificador.service.CurricularUnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurricularUnitServiceImpl implements CurricularUnitService {

    private final CurricularUnitRepository curricularUnitRepository;
    private final CurricularUnitMapper curricularUnitMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CurricularUnitResponse> getCurricularUnits(Long campusId) {
        log.debug("Getting Curricular Units - campusId: {}", campusId);

        List<CurricularUnit> curricularUnits = campusId != null
            ? curricularUnitRepository.findByCampusId(campusId)
            : curricularUnitRepository.findAll();

        return curricularUnits.stream()
            .map(curricularUnitMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CurricularUnitResponse getCurricularUnitById(Long id) {
        log.debug("Getting curricular unit by id: {}", id);

        CurricularUnit curricularUnit = curricularUnitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Curricular unit not found with id: " + id));

        return curricularUnitMapper.toResponse(curricularUnit);
    }
}

