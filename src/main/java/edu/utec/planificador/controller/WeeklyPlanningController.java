package edu.utec.planificador.controller;

import edu.utec.planificador.dto.request.BibliographicReferenceRequest;
import edu.utec.planificador.dto.request.WeeklyPlanningRequest;
import edu.utec.planificador.dto.response.WeeklyPlanningResponse;
import edu.utec.planificador.service.WeeklyPlanningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("weekly-plannings")
@RequiredArgsConstructor
@Tag(name = "Weekly Planning", description = "Endpoints for managing weekly planning (associated with courses)")
public class WeeklyPlanningController {

    private final WeeklyPlanningService weeklyPlanningService;

    @PostMapping("/course/{courseId}")
    @Operation(
        summary = "Create weekly planning",
        description = "Creates a new weekly planning for a specific course"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Weekly planning created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WeeklyPlanningResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        )
    })
    public ResponseEntity<WeeklyPlanningResponse> createWeeklyPlanning(
        @PathVariable Long courseId,
        @Valid @RequestBody WeeklyPlanningRequest request
    ) {
        log.info("POST /weekly-plannings/course/{} - Creating weekly planning", courseId);
        
        WeeklyPlanningResponse response = weeklyPlanningService.createWeeklyPlanning(courseId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get weekly planning by ID",
        description = "Retrieves a weekly planning by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Weekly planning found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WeeklyPlanningResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Weekly planning not found",
            content = @Content
        )
    })
    public ResponseEntity<WeeklyPlanningResponse> getWeeklyPlanningById(@PathVariable Long id) {
        log.info("GET /weekly-plannings/{} - Retrieving weekly planning", id);
        
        WeeklyPlanningResponse response = weeklyPlanningService.getWeeklyPlanningById(id);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}")
    @Operation(
        summary = "Get all weekly plannings by course",
        description = "Retrieves all weekly plannings for a specific course"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Weekly plannings found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WeeklyPlanningResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course not found",
            content = @Content
        )
    })
    public ResponseEntity<List<WeeklyPlanningResponse>> getWeeklyPlanningsByCourse(
        @PathVariable Long courseId
    ) {
        log.info("GET /weekly-plannings/course/{} - Retrieving all weekly plannings", courseId);
        
        List<WeeklyPlanningResponse> responses = weeklyPlanningService.getWeeklyPlanningsByCourseId(courseId);
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/course/{courseId}/week/{weekNumber}")
    @Operation(
        summary = "Get weekly planning by course and week number",
        description = "Retrieves a weekly planning for a specific course and week number"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Weekly planning found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WeeklyPlanningResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Weekly planning not found for the given course and week number",
            content = @Content
        )
    })
    public ResponseEntity<WeeklyPlanningResponse> getWeeklyPlanningByWeekNumber(
        @PathVariable Long courseId,
        @PathVariable Integer weekNumber
    ) {
        log.info("GET /weekly-plannings/course/{}/week/{} - Retrieving weekly planning by week number", courseId, weekNumber);
        
        WeeklyPlanningResponse response = weeklyPlanningService.getWeeklyPlanningByCourseIdAndWeekNumber(courseId, weekNumber);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}/date")
    @Operation(
        summary = "Get weekly planning by course and date",
        description = "Retrieves a weekly planning for a specific course that contains the given date"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Weekly planning found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WeeklyPlanningResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Weekly planning not found for the given course and date",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid date format (use yyyy-MM-dd)",
            content = @Content
        )
    })
    public ResponseEntity<WeeklyPlanningResponse> getWeeklyPlanningByDate(
        @PathVariable Long courseId,
        @Parameter(description = "Date in format yyyy-MM-dd (e.g., 2025-03-15)", example = "2025-03-15")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("GET /weekly-plannings/course/{}/date?date={} - Retrieving weekly planning by date", courseId, date);
        
        WeeklyPlanningResponse response = weeklyPlanningService.getWeeklyPlanningByCourseIdAndDate(courseId, date);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update weekly planning",
        description = "Updates an existing weekly planning by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Weekly planning updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WeeklyPlanningResponse.class)
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
    public ResponseEntity<WeeklyPlanningResponse> updateWeeklyPlanning(
        @PathVariable Long id,
        @Valid @RequestBody WeeklyPlanningRequest request
    ) {
        log.info("PUT /weekly-plannings/{} - Updating weekly planning", id);
        
        WeeklyPlanningResponse response = weeklyPlanningService.updateWeeklyPlanning(id, request);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete weekly planning",
        description = "Deletes a weekly planning by its ID (cascades to programmatic contents and activities)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Weekly planning deleted successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Weekly planning not found",
            content = @Content
        )
    })
    public ResponseEntity<Void> deleteWeeklyPlanning(@PathVariable Long id) {
        log.info("DELETE /weekly-plannings/{} - Deleting weekly planning", id);
        
        weeklyPlanningService.deleteWeeklyPlanning(id);
        
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // Bibliographic References Management
    // ============================================

    @PostMapping("/{id}/bibliographic-references")
    @Operation(
        summary = "Add bibliographic reference",
        description = "Adds a new bibliographic reference to a weekly planning"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Bibliographic reference added successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Weekly planning not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid reference data",
            content = @Content
        )
    })
    public ResponseEntity<Void> addBibliographicReference(
        @PathVariable Long id,
        @Valid @RequestBody BibliographicReferenceRequest request
    ) {
        log.info("POST /weekly-plannings/{}/bibliographic-references - Adding bibliographic reference", id);
        
        weeklyPlanningService.addBibliographicReference(id, request.getReference());
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/bibliographic-references")
    @Operation(
        summary = "Get bibliographic references",
        description = "Retrieves all bibliographic references for a weekly planning"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bibliographic references retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Weekly planning not found",
            content = @Content
        )
    })
    public ResponseEntity<List<String>> getBibliographicReferences(@PathVariable Long id) {
        log.info("GET /weekly-plannings/{}/bibliographic-references - Retrieving bibliographic references", id);
        
        List<String> references = weeklyPlanningService.getBibliographicReferences(id);
        
        return ResponseEntity.ok(references);
    }

    @DeleteMapping("/{id}/bibliographic-references")
    @Operation(
        summary = "Remove bibliographic reference",
        description = "Removes a bibliographic reference from a weekly planning"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Bibliographic reference removed successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Weekly planning not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid reference data",
            content = @Content
        )
    })
    public ResponseEntity<Void> removeBibliographicReference(
        @PathVariable Long id,
        @Valid @RequestBody BibliographicReferenceRequest request
    ) {
        log.info("DELETE /weekly-plannings/{}/bibliographic-references - Removing bibliographic reference", id);
        
        weeklyPlanningService.removeBibliographicReference(id, request.getReference());
        
        return ResponseEntity.noContent().build();
    }
}
