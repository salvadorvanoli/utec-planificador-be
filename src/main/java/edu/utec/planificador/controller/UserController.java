package edu.utec.planificador.controller;

import edu.utec.planificador.dto.response.UserPositionsResponse;
import edu.utec.planificador.service.UserPositionService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

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
}

