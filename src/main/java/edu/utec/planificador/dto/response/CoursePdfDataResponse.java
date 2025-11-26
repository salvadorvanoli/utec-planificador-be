package edu.utec.planificador.dto.response;

import edu.utec.planificador.enumeration.DeliveryFormat;
import edu.utec.planificador.enumeration.PartialGradingSystem;
import edu.utec.planificador.enumeration.Shift;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CoursePdfDataResponse {

    // Datos del curso
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Shift shift;
    private Boolean involvesActivitiesWithProductiveSector;
    private PartialGradingSystem partialGradingSystem;
    private Boolean isRelatedToInvestigation;
    private Map<DeliveryFormat, Integer> hoursPerDeliveryFormat;

    // Profesores del curso
    private List<TeacherInfo> teachers;

    // Información del programa
    private String programName;

    // Información de la unidad curricular
    private CurricularUnitInfo curricularUnit;

    // Planificación semanal con contenidos
    private List<WeeklyPlanningInfo> weeklyPlannings;

    // Bibliografía general del curso
    private List<String> bibliography;

    @Data
    @Builder
    public static class TeacherInfo {
        private String name;
        private String lastName;
        private String email;
    }

    @Data
    @Builder
    public static class CurricularUnitInfo {
        private String name;
        private Integer credits;
    }

    @Data
    @Builder
    public static class WeeklyPlanningInfo {
        private Integer weekNumber;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<String> contentTitles;
        private List<String> bibliographicReferences;
    }
}
