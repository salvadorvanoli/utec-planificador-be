package edu.utec.planificador.controller;

import edu.utec.planificador.dto.response.PeriodResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
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
        log.info("GET /user/positions");

        UserPositionsResponse response = userPositionService.getCurrentUserPositions();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/periods")
    @Operation(
        summary = "Get user periods by campus",
        description = "Returns all unique academic periods for the authenticated user's courses in a specific campus. " +
                      "Periods are formatted as 'YYYY-XS' where X is 1 for odd semesters or 2 for even semesters. " +
                      "For example: '2025-1S' for semester 3 in 2025, or '2025-2S' for semester 4 in 2025.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Periods retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = PeriodResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "User does not have access to the specified campus",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Campus not found",
            content = @Content
        )
    })
    public ResponseEntity<List<PeriodResponse>> getUserPeriodsByCampus(
        @Parameter(description = "Campus ID to filter courses", required = true, example = "1")
        @RequestParam Long campusId
    ) {
        log.info("GET /user/periods - campusId: {}", campusId);

        List<PeriodResponse> response = userPositionService.getUserPeriodsByCampus(campusId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
        summary = "Get users with optional filters",
        description = "Returns users filtered by role and/or Regional Technological Institute. " +
                      "If no filters are specified, returns all users. " +
                      "Role can be TEACHER, COORDINATOR, EDUCATION_MANAGER, etc. " +
                      "No authentication required."
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
        @Parameter(description = "Regional Technological Institute ID to filter users", example = "1")
        @RequestParam(required = false) Long rtiId
    ) {
        log.info("GET /user - role: {}, rtiId: {}", role, rtiId);

        List<UserBasicResponse> response = userPositionService.getUsers(role, rtiId);

        return ResponseEntity.ok(response);
    }
}
