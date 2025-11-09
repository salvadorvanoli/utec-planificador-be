package edu.utec.planificador.controller;

import edu.utec.planificador.dto.response.CampusResponse;
import edu.utec.planificador.service.CampusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/campuses")
@RequiredArgsConstructor
@Tag(name = "Campuses", description = "Endpoints for Campus information")
public class CampusController {

    private final CampusService campusService;

    @GetMapping
    @Operation(
        summary = "Get Campuses",
        description = "Returns all Campuses, optionally filtered by user. " +
                      "If userId is provided, returns only Campuses where that user has active positions. " +
                      "If userId is not provided, returns all Campuses. " +
                      "This endpoint is publicly accessible - no authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Campuses retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = CampusResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    public ResponseEntity<List<CampusResponse>> getCampuses(
        @Parameter(description = "User ID to filter Campuses", example = "1")
        @RequestParam(required = false) Long userId
    ) {
        log.info("GET /campuses - userId: {}", userId);
        
        List<CampusResponse> response = campusService.getCampuses(userId);
        
        return ResponseEntity.ok(response);
    }
}
