package edu.utec.planificador.controller;

import edu.utec.planificador.dto.response.RegionalTechnologicalInstituteResponse;
import edu.utec.planificador.service.RegionalTechnologicalInstituteService;
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
@RequestMapping("/regional-technological-institutes")
@RequiredArgsConstructor
@Tag(name = "Regional Technological Institutes", description = "Endpoints for Regional Technological Institute information")
public class RegionalTechnologicalInstituteController {

    private final RegionalTechnologicalInstituteService regionalTechnologicalInstituteService;

    @GetMapping
    @Operation(
        summary = "Get Regional Technological Institutes",
        description = "Returns all Regional Technological Institutes, optionally filtered by user. " +
                      "If userId is provided, returns only RTIs where that user has active positions. " +
                      "If userId is not provided, returns all RTIs. " +
                      "This endpoint is publicly accessible - no authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "RTIs retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = RegionalTechnologicalInstituteResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    public ResponseEntity<List<RegionalTechnologicalInstituteResponse>> getRegionalTechnologicalInstitutes(
        @Parameter(description = "User ID to filter RTIs", example = "1")
        @RequestParam(required = false) Long userId
    ) {
        log.info("GET /regional-technological-institutes - userId: {}", userId);
        
        List<RegionalTechnologicalInstituteResponse> response = 
            regionalTechnologicalInstituteService.getRegionalTechnologicalInstitutes(userId);
        
        return ResponseEntity.ok(response);
    }
}
