package edu.utec.planificador.controller;

import edu.utec.planificador.dto.request.CurricularUnitRequest;
import edu.utec.planificador.dto.response.CurricularUnitResponse;
import edu.utec.planificador.enumeration.DomainArea;
import edu.utec.planificador.enumeration.ProfessionalCompetency;
import edu.utec.planificador.service.CurricularUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("curricular-units")
@RequiredArgsConstructor
@Tag(name = "Curricular Units", description = "Endpoints for curricular unit management")
public class CurricularUnitController {

    private final CurricularUnitService curricularUnitService;

    @PostMapping
    @Operation(
        summary = "Create curricular unit",
        description = "Creates a new curricular unit"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Curricular unit created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CurricularUnitResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Term not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        )
    })
    public ResponseEntity<CurricularUnitResponse> createCurricularUnit(@Valid @RequestBody CurricularUnitRequest request) {
        log.info("POST /curricular-units - Creating curricular unit with name: {}", request.getName());
        
        CurricularUnitResponse response = curricularUnitService.createCurricularUnit(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    @PutMapping("/{id}")
    @Operation(
        summary = "Update curricular unit",
        description = "Updates an existing curricular unit by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Curricular unit updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CurricularUnitResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Curricular unit or term not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        )
    })
    public ResponseEntity<CurricularUnitResponse> updateCurricularUnit(
        @PathVariable Long id,
        @Valid @RequestBody CurricularUnitRequest request
    ) {
        log.info("PUT /curricular-units/{} - Updating curricular unit", id);
        
        CurricularUnitResponse response = curricularUnitService.updateCurricularUnit(id, request);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete curricular unit",
        description = "Deletes a curricular unit by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Curricular unit deleted successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Curricular unit not found",
            content = @Content
        )
    })
    public ResponseEntity<Void> deleteCurricularUnit(@PathVariable Long id) {
        log.info("DELETE /curricular-units/{} - Deleting curricular unit", id);
        
        curricularUnitService.deleteCurricularUnit(id);
        
        return ResponseEntity.noContent().build();
    }

    // ==================== Domain Areas ====================

    @PostMapping("/{id}/domain-areas/{domainArea}")
    @Operation(
        summary = "Add Domain Area",
        description = "Adds a Domain Area to a curricular unit"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Domain area added successfully",
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
    public ResponseEntity<CurricularUnitResponse> addDomainArea(
        @PathVariable Long id,
        @PathVariable DomainArea domainArea
    ) {
        log.info("POST /curricular-units/{}/domain-areas/{} - Adding domain area", id, domainArea);
        
        CurricularUnitResponse response = curricularUnitService.addDomainArea(id, domainArea);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/domain-areas/{domainArea}")
    @Operation(
        summary = "Remove Domain Area",
        description = "Removes a Domain Area from a curricular unit"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Domain area removed successfully",
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
    public ResponseEntity<CurricularUnitResponse> removeDomainArea(
        @PathVariable Long id,
        @PathVariable DomainArea domainArea
    ) {
        log.info("DELETE /curricular-units/{}/domain-areas/{} - Removing domain area", id, domainArea);
        
        CurricularUnitResponse response = curricularUnitService.removeDomainArea(id, domainArea);
        
        return ResponseEntity.ok(response);
    }

    // ==================== Professional Competencies ====================

    @PostMapping("/{id}/professional-competencies/{competency}")
    @Operation(
        summary = "Add Professional Competency",
        description = "Adds a Professional Competency to a curricular unit"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professional competency added successfully",
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
    public ResponseEntity<CurricularUnitResponse> addProfessionalCompetency(
        @PathVariable Long id,
        @PathVariable ProfessionalCompetency competency
    ) {
        log.info("POST /curricular-units/{}/professional-competencies/{} - Adding competency", id, competency);
        
        CurricularUnitResponse response = curricularUnitService.addProfessionalCompetency(id, competency);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/professional-competencies/{competency}")
    @Operation(
        summary = "Remove Professional Competency",
        description = "Removes a Professional Competency from a curricular unit"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professional competency removed successfully",
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
    public ResponseEntity<CurricularUnitResponse> removeProfessionalCompetency(
        @PathVariable Long id,
        @PathVariable ProfessionalCompetency competency
    ) {
        log.info("DELETE /curricular-units/{}/professional-competencies/{} - Removing competency", id, competency);
        
        CurricularUnitResponse response = curricularUnitService.removeProfessionalCompetency(id, competency);
        
        return ResponseEntity.ok(response);
    }
}
