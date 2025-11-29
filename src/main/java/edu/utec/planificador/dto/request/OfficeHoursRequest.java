package edu.utec.planificador.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class OfficeHoursRequest {

    @NotNull(message = "{validation.officeHours.date.notNull}")
    private LocalDate date;

    @NotNull(message = "{validation.officeHours.startTime.notNull}")
    private LocalTime startTime;

    @NotNull(message = "{validation.officeHours.endTime.notNull}")
    private LocalTime endTime;

    @NotNull(message = "{validation.officeHours.courseId.notNull}")
    private Long courseId;
}
