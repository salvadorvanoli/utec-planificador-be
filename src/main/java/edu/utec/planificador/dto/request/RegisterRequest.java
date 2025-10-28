package edu.utec.planificador.dto.request;

import edu.utec.planificador.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request")
public class RegisterRequest {

    @Schema(description = "UTEC institutional email", example = "juan.perez@utec.edu.uy")
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    @Size(max = 100, message = "{validation.email.size}")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@utec\\.edu\\.uy$", 
        message = "{validation.email.utec}"
    )
    private String email;

    @Schema(
        description = "User password (minimum 8 characters, must include uppercase, lowercase, numbers and special characters)", 
        example = "SecurePassword123!")
    @NotBlank(message = "{validation.password.required}")
    @StrongPassword(message = "{validation.password.strong}")
    private String password;

    @Schema(description = "User full name", example = "Juan Pérez")
    @NotBlank(message = "{validation.name.required}")
    @Size(min = 2, max = 100, message = "{validation.name.size}")
    @Pattern(
        regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", 
        message = "{validation.name.format}"
    )
    private String fullName;

    @Schema(description = "National ID number", example = "12345678")
    @NotBlank(message = "{validation.ci.required}")
    @Pattern(
        regexp = "^\\d{7,8}$", 
        message = "{validation.ci.format}"
    )
    private String ci;

    @Schema(description = "Contact phone number", example = "+598 99 123 456")
    @Pattern(
        regexp = "^\\+?[0-9\\s\\-()]{7,20}$", 
        message = "{validation.phone.format}"
    )
    @Size(max = 20, message = "{validation.phone.size}")
    private String phone;

    @Schema(description = "User positions with roles and campuses", example = "[{\"type\": \"TEACHER\", \"role\": \"TEACHER\", \"campusIds\": [1, 2]}]")
    @NotNull(message = "{validation.positions.required}")
    @Size(min = 1, message = "{validation.positions.min}")
    @Valid
    private List<PositionRequest> positions;
}
