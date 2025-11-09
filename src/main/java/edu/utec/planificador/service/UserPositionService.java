package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.PeriodResponse;
import edu.utec.planificador.dto.response.UserPositionsResponse;

import java.util.List;

public interface UserPositionService {

    UserPositionsResponse getCurrentUserPositions();

    List<PeriodResponse> getUserPeriodsByCampus(Long campusId);
}
