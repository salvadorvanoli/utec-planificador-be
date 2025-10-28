package edu.utec.planificador.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Successful authentication response")
public class AuthResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "Token expiration time in seconds", example = "3600")
    private Long expiresIn;

    @Schema(description = "Authenticated user email", example = "juan.perez@utec.edu.uy")
    private String email;

    @Schema(description = "User full name", example = "Juan Pérez")
    private String fullName;

    @Schema(description = "User roles", example = "[\"TEACHER\", \"COORDINATOR\"]")
    private String[] roles;

    @Schema(description = "Last login date and time")
    private LocalDateTime lastLoginAt;

    public static AuthResponse fromToken(String token, Long expiresIn) {
        return AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .build();
    }
}
