package edu.utec.planificador.service;

import edu.utec.planificador.dto.request.OfficeHoursRequest;
import edu.utec.planificador.dto.response.OfficeHoursResponse;

import java.util.List;

public interface OfficeHoursService {

    OfficeHoursResponse createOfficeHours(OfficeHoursRequest request);

    List<OfficeHoursResponse> getOfficeHoursByCourseId(Long courseId);

    void deleteOfficeHours(Long id);
}
