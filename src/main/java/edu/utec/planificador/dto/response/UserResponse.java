package edu.utec.planificador.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.utec.planificador.enumeration.AuthProvider;
import edu.utec.planificador.enumeration.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authenticated user information")
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Institutional email", example = "juan.perez@utec.edu.uy")
    private String email;

    @Schema(description = "Full name", example = "Juan Pérez")
    private String fullName;

    @Schema(description = "National ID number", example = "12345678")
    private String ci;

    @Schema(description = "Phone number", example = "+598 99 123 456")
    private String phone;

    @Schema(description = "Authentication provider", example = "LOCAL")
    private AuthProvider authProvider;

    @Schema(description = "User roles")
    private Set<Role> roles;

    @Schema(description = "User enabled status", example = "true")
    private Boolean enabled;

    @Schema(description = "Creation date")
    private LocalDateTime createdAt;

    @Schema(description = "Last login date")
    private LocalDateTime lastLoginAt;
}
