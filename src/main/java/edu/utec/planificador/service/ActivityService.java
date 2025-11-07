package edu.utec.planificador.service;

import edu.utec.planificador.dto.request.ActivityRequest;
import edu.utec.planificador.dto.response.ActivityResponse;

public interface ActivityService {

    ActivityResponse createActivity(ActivityRequest request);

    ActivityResponse getActivityById(Long id);

    ActivityResponse updateActivity(Long id, ActivityRequest request);

    void deleteActivity(Long id);
}
