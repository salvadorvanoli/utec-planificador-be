package edu.utec.planificador.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class OfficeHoursResponse {

    private Long id;
    private LocalDate date;
    private Integer startHour;
    private Integer endHour;
    private Long courseId;
}
