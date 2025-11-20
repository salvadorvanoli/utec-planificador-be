package edu.utec.planificador.controller;

import edu.utec.planificador.dto.request.ActivityRequest;
import edu.utec.planificador.dto.response.ActivityResponse;
import edu.utec.planificador.service.ActivityService;
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
@RequestMapping("activities")
@RequiredArgsConstructor
@Tag(name = "Activities", description = "Endpoints for managing activities (associated with programmatic content)")
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    @PreAuthorize("hasAuthority('PLANNING_WRITE')")
    @Operation(
        summary = "Create activity",
        description = "Creates a new activity and associates it with programmatic content"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Activity created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ActivityResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Programmatic content not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        )
    })
    public ResponseEntity<ActivityResponse> createActivity(@Valid @RequestBody ActivityRequest request) {
        log.info("POST /activities - Creating activity");
        
        ActivityResponse response = activityService.createActivity(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNING_READ')")
    @Operation(
        summary = "Get activity by ID",
        description = "Retrieves an activity by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Activity found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ActivityResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Activity not found",
            content = @Content
        )
    })
    public ResponseEntity<ActivityResponse> getActivityById(@PathVariable Long id) {
        log.info("GET /activities/{} - Retrieving activity", id);
        
        ActivityResponse response = activityService.getActivityById(id);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNING_WRITE')")
    @Operation(
        summary = "Update activity",
        description = "Updates an existing activity by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Activity updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ActivityResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Activity or programmatic content not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        )
    })
    public ResponseEntity<ActivityResponse> updateActivity(
        @PathVariable Long id,
        @Valid @RequestBody ActivityRequest request
    ) {
        log.info("PUT /activities/{} - Updating activity", id);
        
        ActivityResponse response = activityService.updateActivity(id, request);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNING_DELETE')")
    @Operation(
        summary = "Delete activity",
        description = "Deletes an activity by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Activity deleted successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Activity not found",
            content = @Content
        )
    })
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        log.info("DELETE /activities/{} - Deleting activity", id);
        
        activityService.deleteActivity(id);
        
        return ResponseEntity.noContent().build();
    }
}
