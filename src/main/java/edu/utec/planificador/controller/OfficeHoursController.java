package edu.utec.planificador.controller;

import edu.utec.planificador.dto.request.OfficeHoursRequest;
import edu.utec.planificador.dto.response.OfficeHoursResponse;
import edu.utec.planificador.service.OfficeHoursService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("office-hours")
@RequiredArgsConstructor
@Tag(name = "Office Hours", description = "Endpoints for managing office hours (horas de consulta)")
@SecurityRequirement(name = "bearerAuth")
public class OfficeHoursController {

    private final OfficeHoursService officeHoursService;

    @PostMapping
    @PreAuthorize("hasAuthority('PLANNING_WRITE')")
    @Operation(
        summary = "Create office hours",
        description = "Creates new office hours for a course. End hour must be after start hour."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Office hours created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OfficeHoursResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data (e.g., end hour before start hour)",
            content = @Content
        )
    })
    public ResponseEntity<OfficeHoursResponse> createOfficeHours(@Valid @RequestBody OfficeHoursRequest request) {
        log.info("POST /office-hours - Creating office hours for course {}", request.getCourseId());

        OfficeHoursResponse response = officeHoursService.createOfficeHours(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('PLANNING_READ')")
    @Operation(
        summary = "Get office hours by course",
        description = "Returns all office hours for a specific course, ordered by date and start hour"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Office hours retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = OfficeHoursResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course not found",
            content = @Content
        )
    })
    public ResponseEntity<List<OfficeHoursResponse>> getOfficeHoursByCourse(@PathVariable Long courseId) {
        log.info("GET /office-hours/course/{} - Getting office hours", courseId);

        List<OfficeHoursResponse> response = officeHoursService.getOfficeHoursByCourseId(courseId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNING_DELETE')")
    @Operation(
        summary = "Delete office hours",
        description = "Deletes office hours by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Office hours deleted successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Office hours not found",
            content = @Content
        )
    })
    public ResponseEntity<Void> deleteOfficeHours(@PathVariable Long id) {
        log.info("DELETE /office-hours/{} - Deleting office hours", id);

        officeHoursService.deleteOfficeHours(id);

        return ResponseEntity.noContent().build();
    }
}
