package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.UserBasicResponse;
import edu.utec.planificador.dto.response.UserPositionsResponse;
import edu.utec.planificador.enumeration.Role;

import java.util.List;

public interface UserPositionService {

    UserPositionsResponse getCurrentUserPositions();

    List<UserBasicResponse> getUsers(Role role, Long campusId);
}
