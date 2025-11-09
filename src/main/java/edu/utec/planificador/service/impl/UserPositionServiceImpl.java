package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.response.PeriodResponse;
import edu.utec.planificador.dto.response.UserBasicResponse;
import edu.utec.planificador.dto.response.UserPositionsResponse;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.enumeration.Role;
import edu.utec.planificador.mapper.PositionMapper;
import edu.utec.planificador.mapper.UserMapper;
import edu.utec.planificador.repository.CourseRepository;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.service.AccessControlService;
import edu.utec.planificador.service.UserPositionService;
import edu.utec.planificador.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPositionServiceImpl implements UserPositionService {

    private final PositionMapper positionMapper;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final AccessControlService accessControlService;

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

    @Override
    @Transactional(readOnly = true)
    public List<PeriodResponse> getUserPeriodsByCampus(Long campusId) {
        accessControlService.validateCampusAccess(campusId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        log.debug("Getting periods for user: {} in campus: {}", currentUser.getUtecEmail(), campusId);

        List<Course> courses = courseRepository.findByUserIdAndCampusId(currentUser.getId(), campusId);

        List<PeriodResponse> periods = courses.stream()
            .map(Course::getPeriod)
            .filter(period -> period != null)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .map(period -> PeriodResponse.builder().period(period).build())
            .collect(Collectors.toList());

        log.debug("Found {} unique periods for user in campus {}", periods.size(), campusId);

        return periods;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBasicResponse> getUsers(Role role, Long campusId) {
        log.debug("Getting users with role: {}, campusId: {}", role, campusId);
        
        List<User> users = userRepository.findAll(UserSpecification.withFilters(role, campusId));

        return users.stream()
            .map(userMapper::toBasicResponse)
            .collect(Collectors.toList());
    }
}
