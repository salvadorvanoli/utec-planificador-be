package edu.utec.planificador.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherCourseResponse {
    private Long teacherId;
    private Long courseId;
    private String displayName; // formato: "Unidad Curricular - Periodo - Sede"
    private String curricularUnitName;
    private String period;
    private String campusName;
}
