package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.response.RegionalTechnologicalInstituteResponse;
import edu.utec.planificador.entity.RegionalTechnologicalInstitute;
import edu.utec.planificador.mapper.RegionalTechnologicalInstituteMapper;
import edu.utec.planificador.repository.RegionalTechnologicalInstituteRepository;
import edu.utec.planificador.service.RegionalTechnologicalInstituteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionalTechnologicalInstituteServiceImpl implements RegionalTechnologicalInstituteService {

    private final RegionalTechnologicalInstituteRepository regionalTechnologicalInstituteRepository;
    private final RegionalTechnologicalInstituteMapper regionalTechnologicalInstituteMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RegionalTechnologicalInstituteResponse> getRegionalTechnologicalInstitutes(Long userId) {
        log.debug("Getting Regional Technological Institutes - userId: {}", userId);

        List<RegionalTechnologicalInstitute> rtis = userId != null
            ? regionalTechnologicalInstituteRepository.findByUserId(userId)
            : regionalTechnologicalInstituteRepository.findAll();

        return rtis.stream()
            .map(regionalTechnologicalInstituteMapper::toResponse)
            .collect(Collectors.toList());
    }
}
