package edu.utec.planificador.service;

import edu.utec.planificador.dto.request.WeeklyPlanningRequest;
import edu.utec.planificador.dto.response.WeeklyPlanningResponse;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyPlanningService {

    WeeklyPlanningResponse createWeeklyPlanning(Long courseId, WeeklyPlanningRequest request);

    WeeklyPlanningResponse getWeeklyPlanningById(Long id);

    List<WeeklyPlanningResponse> getWeeklyPlanningsByCourseId(Long courseId);

    WeeklyPlanningResponse getWeeklyPlanningByCourseIdAndWeekNumber(Long courseId, Integer weekNumber);

    WeeklyPlanningResponse getWeeklyPlanningByCourseIdAndDate(Long courseId, LocalDate date);

    WeeklyPlanningResponse updateWeeklyPlanning(Long id, WeeklyPlanningRequest request);

    void deleteWeeklyPlanning(Long id);
}
