package edu.utec.planificador.controller;

import edu.utec.planificador.dto.response.UserBasicResponse;
import edu.utec.planificador.dto.response.UserPositionsResponse;
import edu.utec.planificador.enumeration.Role;
import edu.utec.planificador.service.UserPositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management endpoints")
public class UserController {

    private final UserPositionService userPositionService;

    @GetMapping("/positions")
    @Operation(
        summary = "Get current user positions",
        description = "Returns all positions of the authenticated user with their associated campuses and Regional Technological Institutes",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User positions retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserPositionsResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content
        )
    })
    public ResponseEntity<UserPositionsResponse> getCurrentUserPositions() {
        log.info("GET /users/positions");

        UserPositionsResponse response = userPositionService.getCurrentUserPositions();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/teachers")
    @Operation(
        summary = "Get teachers",
        description = "Returns all users with TEACHER role. " +
                      "Optionally filters by Campus. " +
                      "This endpoint is publicly accessible for course catalog purposes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Teachers retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = UserBasicResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    public ResponseEntity<List<UserBasicResponse>> getTeachers(
        @Parameter(description = "Campus ID to filter teachers", example = "1")
        @RequestParam(required = false) Long campusId
    ) {
        log.info("GET /users/teachers - campusId: {}", campusId);

        List<UserBasicResponse> response = userPositionService.getUsers(Role.TEACHER, campusId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    @Operation(
        summary = "Get users with optional filters",
        description = "Returns users filtered by role and/or Campus. " +
                      "If no filters are specified, returns all users. " +
                      "Role can be TEACHER, COORDINATOR, EDUCATION_MANAGER, etc. " +
                      "This endpoint requires authentication and is intended for administrative use.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = UserBasicResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have USER_READ permission",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid role specified",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    public ResponseEntity<List<UserBasicResponse>> getUsers(
        @Parameter(description = "Role to filter users (TEACHER, COORDINATOR, EDUCATION_MANAGER)", example = "TEACHER")
        @RequestParam(required = false) Role role,
        @Parameter(description = "Campus ID to filter users", example = "1")
        @RequestParam(required = false) Long campusId
    ) {
        log.info("GET /users - role: {}, campusId: {}", role, campusId);

        List<UserBasicResponse> response = userPositionService.getUsers(role, campusId);

        return ResponseEntity.ok(response);
    }
}
