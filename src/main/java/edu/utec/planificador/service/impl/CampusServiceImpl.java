package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.response.CampusResponse;
import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.mapper.CampusMapper;
import edu.utec.planificador.repository.CampusRepository;
import edu.utec.planificador.service.CampusService;
import edu.utec.planificador.specification.CampusSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampusServiceImpl implements CampusService {

    private final CampusRepository campusRepository;
    private final CampusMapper campusMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CampusResponse> getCampuses(Long userId, String period) {
        log.debug("Getting Campuses - userId: {}, period: {}", userId, period);

        List<Campus> campuses = campusRepository.findAll(
            CampusSpecification.withFilters(userId, period)
        );

        return campuses.stream()
            .map(campusMapper::toResponse)
            .collect(Collectors.toList());
    }
}
