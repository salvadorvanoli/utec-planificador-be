package edu.utec.planificador.controller;

import edu.utec.planificador.dto.request.ProgrammaticContentRequest;
import edu.utec.planificador.dto.response.ProgrammaticContentResponse;
import edu.utec.planificador.service.ProgrammaticContentService;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("programmatic-contents")
@RequiredArgsConstructor
@Tag(name = "Programmatic Content", description = "Endpoints for managing programmatic content (associated with weekly planning)")
public class ProgrammaticContentController {

    private final ProgrammaticContentService programmaticContentService;

    @PostMapping
    @PreAuthorize("hasAuthority('PLANNING_WRITE')")
    @Operation(
        summary = "Create programmatic content",
        description = "Creates a new programmatic content and associates it with a weekly planning"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Programmatic content created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProgrammaticContentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Weekly planning not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        )
    })
    public ResponseEntity<ProgrammaticContentResponse> createProgrammaticContent(@Valid @RequestBody ProgrammaticContentRequest request) {
        log.info("POST /programmatic-contents - Creating programmatic content");
        
        ProgrammaticContentResponse response = programmaticContentService.createProgrammaticContent(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNING_READ')")
    @Operation(
        summary = "Get programmatic content by ID",
        description = "Retrieves a programmatic content by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Programmatic content found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProgrammaticContentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Programmatic content not found",
            content = @Content
        )
    })
    public ResponseEntity<ProgrammaticContentResponse> getProgrammaticContentById(@PathVariable Long id) {
        log.info("GET /programmatic-contents/{} - Retrieving programmatic content", id);
        
        ProgrammaticContentResponse response = programmaticContentService.getProgrammaticContentById(id);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNING_WRITE')")
    @Operation(
        summary = "Update programmatic content",
        description = "Updates an existing programmatic content by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Programmatic content updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProgrammaticContentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Programmatic content or weekly planning not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        )
    })
    public ResponseEntity<ProgrammaticContentResponse> updateProgrammaticContent(
        @PathVariable Long id,
        @Valid @RequestBody ProgrammaticContentRequest request
    ) {
        log.info("PUT /programmatic-contents/{} - Updating programmatic content", id);
        
        ProgrammaticContentResponse response = programmaticContentService.updateProgrammaticContent(id, request);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNING_DELETE')")
    @Operation(
        summary = "Delete programmatic content",
        description = "Deletes a programmatic content by its ID (cascades to activities)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Programmatic content deleted successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Programmatic content not found",
            content = @Content
        )
    })
    public ResponseEntity<Void> deleteProgrammaticContent(@PathVariable Long id) {
        log.info("DELETE /programmatic-contents/{} - Deleting programmatic content", id);
        
        programmaticContentService.deleteProgrammaticContent(id);
        
        return ResponseEntity.noContent().build();
    }
}
