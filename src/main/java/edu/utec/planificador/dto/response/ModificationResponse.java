package edu.utec.planificador.dto.response;

import edu.utec.planificador.enumeration.ModificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modification response")
public class ModificationResponse {

    @Schema(description = "Modification ID", example = "1")
    private Long id;

    @Schema(description = "Date and time of modification", example = "2025-11-13T14:30:00")
    private LocalDateTime modificationDate;

    @Schema(description = "Description of the modification", example = "Se modificó el contenido programático; el título era: 'Introducción'; ahora es: 'Introducción a Java'")
    private String description;

    @Schema(description = "Type of modification: CREATE (1), UPDATE (2), DELETE (0)", example = "UPDATE")
    private ModificationType type;

    @Schema(description = "Teacher ID who made the modification", example = "5")
    private Long teacherId;

    @Schema(description = "Teacher name who made the modification", example = "Juan Pérez")
    private String teacherName;

    @Schema(description = "Course ID where the modification was made", example = "10")
    private Long courseId;
}
