package edu.utec.planificador.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class WeeklyPlanningResponse {
    private Long id;
    private Integer weekNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> bibliographicReferences;
    private List<Long> programmaticContentIds;
}
