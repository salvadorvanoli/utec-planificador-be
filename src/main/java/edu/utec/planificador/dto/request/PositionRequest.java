package edu.utec.planificador.dto.request;

import edu.utec.planificador.enumeration.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User position request with role and campuses")
public class PositionRequest {

    @Schema(
        description = "Position type", 
        example = "TEACHER",
        allowableValues = {"TEACHER", "COORDINATOR", "EDUCATION_MANAGER", "ANALYST"}
    )
    @NotNull(message = "{validation.position.type.required}")
    private PositionType type;

    @Schema(
        description = "Role assigned to this position", 
        example = "TEACHER",
        allowableValues = {"ADMIN", "EDUCATION_MANAGER", "COORDINATOR", "TEACHER", "ANALYST"}
    )
    @NotNull(message = "{validation.position.role.required}")
    private Role role;

    @Schema(
        description = "List of campus IDs where this position applies", 
        example = "[1, 2, 3]"
    )
    @Builder.Default
    private List<Long> campusIds = new ArrayList<>();

    public enum PositionType {
        TEACHER,
        COORDINATOR,
        EDUCATION_MANAGER,
        ANALYST
    }
}
