package edu.utec.planificador.controller;

import edu.utec.planificador.dto.request.LoginRequest;
import edu.utec.planificador.dto.request.RegisterRequest;
import edu.utec.planificador.dto.response.AuthResponse;
import edu.utec.planificador.dto.response.UserResponse;
import edu.utec.planificador.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates a user with email and password. Supports LOCAL and LDAP authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful authentication",
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("POST /auth/login - User: {}", loginRequest.getEmail());
        
        AuthResponse response = authenticationService.login(loginRequest);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Creates a new user with LOCAL authentication. Does not apply to LDAP users."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "User already exists",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        )
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("POST /auth/register - User: {}", registerRequest.getEmail());
        
        AuthResponse response = authenticationService.register(registerRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
