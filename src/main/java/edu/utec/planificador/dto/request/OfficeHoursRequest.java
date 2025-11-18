package edu.utec.planificador.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OfficeHoursRequest {

    @NotNull(message = "{validation.officeHours.date.notNull}")
    private LocalDate date;

    @NotNull(message = "{validation.officeHours.startHour.notNull}")
    @Min(value = 0, message = "{validation.officeHours.startHour.min}")
    @Max(value = 23, message = "{validation.officeHours.startHour.max}")
    private Integer startHour;

    @NotNull(message = "{validation.officeHours.endHour.notNull}")
    @Min(value = 0, message = "{validation.officeHours.endHour.min}")
    @Max(value = 23, message = "{validation.officeHours.endHour.max}")
    private Integer endHour;

    @NotNull(message = "{validation.officeHours.courseId.notNull}")
    private Long courseId;
}
