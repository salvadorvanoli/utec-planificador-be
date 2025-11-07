package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.response.UserPositionsResponse;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.mapper.PositionMapper;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.service.UserPositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPositionServiceImpl implements UserPositionService {

    private final PositionMapper positionMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserPositionsResponse getCurrentUserPositions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        log.debug("Getting positions for user: {}", currentUser.getUtecEmail());

        User user = userRepository.findByIdWithPositions(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getPositions().forEach(position -> {
            position.getCampuses().size();
            position.getCampuses().forEach(campus -> {
                if (campus.getRegionalTechnologicalInstitute() != null) {
                    campus.getRegionalTechnologicalInstitute().getName();
                }
            });
        });

        String fullName = user.getPersonalData() != null
            ? user.getPersonalData().getName() + " " + user.getPersonalData().getLastName()
            : null;

        var positions = user.getPositions().stream()
                .map(positionMapper::toResponse)
                .collect(Collectors.toList());

        return UserPositionsResponse.builder()
                .userId(user.getId())
                .email(user.getUtecEmail())
                .fullName(fullName)
                .positions(positions)
                .build();
    }
}
