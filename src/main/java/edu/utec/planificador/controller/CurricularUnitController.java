package edu.utec.planificador.controller;

import edu.utec.planificador.dto.response.CurricularUnitResponse;
import edu.utec.planificador.service.CurricularUnitService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("curricular-units")
@RequiredArgsConstructor
@Tag(name = "Curricular Units", description = "Endpoints for curricular unit management")
public class CurricularUnitController {

    private final CurricularUnitService curricularUnitService;

    @GetMapping
    @Operation(
        summary = "Get Curricular Units",
        description = "Returns all Curricular Units, optionally filtered by campus. " +
                      "If campusId is provided, returns only curricular units from programs offered at that campus. " +
                      "If campusId is not provided, returns all curricular units. " +
                      "This endpoint is publicly accessible - no authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Curricular units retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = CurricularUnitResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    public ResponseEntity<List<CurricularUnitResponse>> getCurricularUnits(
        @Parameter(description = "Campus ID to filter curricular units", example = "1")
        @RequestParam(required = false) Long campusId
    ) {
        log.info("GET /curricular-units - campusId: {}", campusId);
        
        List<CurricularUnitResponse> response = curricularUnitService.getCurricularUnits(campusId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get curricular unit by ID",
        description = "Retrieves a curricular unit by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Curricular unit found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CurricularUnitResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Curricular unit not found",
            content = @Content
        )
    })
    public ResponseEntity<CurricularUnitResponse> getCurricularUnitById(@PathVariable Long id) {
        log.info("GET /curricular-units/{} - Retrieving curricular unit", id);
        
        CurricularUnitResponse response = curricularUnitService.getCurricularUnitById(id);
        
        return ResponseEntity.ok(response);
    }
}
