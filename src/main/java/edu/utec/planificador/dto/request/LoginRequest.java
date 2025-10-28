package edu.utec.planificador.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request")
public class LoginRequest {

    @Schema(description = "UTEC institutional email", example = "juan.perez@utec.edu.uy")
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    @Size(max = 100, message = "{validation.email.size}")
    private String email;

    @Schema(description = "User password", example = "SecurePassword123!")
    @NotBlank(message = "{validation.password.required}")
    @Size(min = 1, max = 128, message = "{validation.password.size}")
    private String password;
}
