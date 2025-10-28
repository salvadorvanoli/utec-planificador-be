package edu.utec.planificador.service;

import edu.utec.planificador.dto.request.PositionRequest;
import edu.utec.planificador.entity.Position;
import edu.utec.planificador.entity.User;

public interface PositionService {

    Position createPosition(User user, PositionRequest request);
}
