package edu.utec.planificador.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import edu.utec.planificador.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class WeeklyPlanningRequest {

    @NotNull
    @Min(Constants.MIN_WEEK_NUMBER)
    @Max(Constants.MAX_WEEK_NUMBER)
    private Integer weekNumber;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private List<String> bibliographicReferences;
}
