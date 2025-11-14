package edu.utec.planificador.controller;

import edu.utec.planificador.dto.response.ModificationResponse;
import edu.utec.planificador.service.AccessControlService;
import edu.utec.planificador.service.ModificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/modifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Modifications", description = "Modification tracking API")
public class ModificationController {

    private final ModificationService modificationService;
    private final AccessControlService accessControlService;

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyAuthority('COURSE_READ', 'COURSE_WRITE')")
    @Operation(
        summary = "Get modifications by course",
        description = "Retrieves a paginated list of modifications for a specific course, ordered by date (most recent first)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Modifications retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "403", description = "Access denied to this course"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<Page<ModificationResponse>> getModificationsByCourse(
        @Parameter(description = "Course ID", required = true)
        @PathVariable Long courseId,
        
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/v1/modifications/courses/{} - page={}, size={}", courseId, page, size);

        // Validate access to course
        accessControlService.validateCourseAccess(courseId);

        Pageable pageable = PageRequest.of(page, size);
        Page<ModificationResponse> modifications = modificationService.getModificationsByCourse(courseId, pageable);

        return ResponseEntity.ok(modifications);
    }
}
