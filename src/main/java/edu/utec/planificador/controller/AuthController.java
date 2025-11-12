package edu.utec.planificador.controller;

import edu.utec.planificador.dto.request.LoginRequest;
import edu.utec.planificador.dto.response.AuthResponse;
import edu.utec.planificador.dto.response.UserResponse;
import edu.utec.planificador.service.AuthenticationService;
import edu.utec.planificador.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication and user management")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final CookieUtil cookieUtil;

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates a user with email and password. Sets HttpOnly cookie with JWT token. Supports LOCAL and LDAP authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful authentication. JWT token set in HttpOnly cookie.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        )
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        log.info("POST /auth/login - User: {}", loginRequest.getEmail());

        AuthResponse authResponse = authenticationService.login(loginRequest);

        int maxAgeSeconds = (int) (authResponse.getExpiresIn() / 1000);
        cookieUtil.addJwtCookie(response, authResponse.getAccessToken(), maxAgeSeconds);

        log.info("JWT cookie set for user: {}", loginRequest.getEmail());

        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get current user",
        description = "Returns information about the currently authenticated user",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User information",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated or invalid token",
            content = @Content
        )
    })
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("GET /auth/me");

        UserResponse response = authenticationService.getCurrentUser();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(
        summary = "User logout",
        description = "Logs out the user by clearing the JWT cookie"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Logout successful. JWT cookie cleared."
    )
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        log.info("POST /auth/logout");

        cookieUtil.deleteJwtCookie(response);

        log.info("User logged out successfully, cookie cleared");

        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @Operation(
        summary = "Check authentication status",
        description = "Validates the JWT cookie and returns user information if the session is valid",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Valid session",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired session",
            content = @Content
        )
    })
    public ResponseEntity<UserResponse> checkStatus() {
        log.info("GET /auth/status");

        UserResponse response = authenticationService.getCurrentUser();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Verifies that the authentication service is operational"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Service operational"
    )
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Authentication service is operational");
    }
}

