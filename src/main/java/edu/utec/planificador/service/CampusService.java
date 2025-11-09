package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.CampusResponse;

import java.util.List;

public interface CampusService {

    List<CampusResponse> getCampuses(Long userId);
}
