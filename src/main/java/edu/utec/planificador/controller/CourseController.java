package edu.utec.planificador.controller;

import edu.utec.planificador.dto.request.CourseRequest;
import edu.utec.planificador.dto.response.CoursePdfDataResponse;
import edu.utec.planificador.dto.response.CourseResponse;
import edu.utec.planificador.dto.response.PeriodResponse;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;
import edu.utec.planificador.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Endpoints for course management")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @Operation(
        summary = "Create course",
        description = "Creates a new course with a default weekly planning (week 1) starting on the course start date"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Course created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Curricular unit not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        )
    })
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        log.info("POST /courses - Creating course with description: {}", request.getDescription());
        
        CourseResponse response = courseService.createCourse(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
        summary = "Get courses with optional filters and pagination",
        description = "Returns courses filtered by user (teacher), campus, and/or period with pagination support. " +
                      "If no filters are specified, returns all courses. " +
                      "Period format: 'YYYY-1S' or 'YYYY-2S' (e.g., '2024-1S' for first semester of 2024). " +
                      "This endpoint is publicly accessible - no authentication required. " +
                      "Results are sorted by startDate in descending order (most recent first)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Courses retrieved successfully with pagination metadata",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    public ResponseEntity<Page<CourseResponse>> getCourses(
        @Parameter(description = "User ID to filter courses by teacher", example = "1")
        @RequestParam(required = false) Long userId,
        @Parameter(description = "Campus ID to filter courses", example = "1")
        @RequestParam(required = false) Long campusId,
        @Parameter(description = "Period to filter courses (format: YYYY-1S or YYYY-2S)", example = "2024-1S")
        @RequestParam(required = false) String period,
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size (number of items per page)", example = "10")
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET /courses - userId: {}, campusId: {}, period: {}, page: {}, size: {}", 
                 userId, campusId, period, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<CourseResponse> response = courseService.getCourses(userId, campusId, period, pageable);
        
        log.info("Returning {} courses (page {} of {}, total: {})", 
            response.getNumberOfElements(), 
            response.getNumber() + 1, 
            response.getTotalPages(),
            response.getTotalElements()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/campus/{campusId}/my-courses")
    @Operation(
        summary = "Get brief courses for current user in a campus",
        description = "Returns all courses for the authenticated user in the specified campus with only the fields: id, curricularUnitName, startDate and shift. " +
                      "If query parameter 'courseId' is provided, validates that the course belongs to the current user in that campus and returns only that course. " +
                      "If the course does not belong to the user's courses in that campus, returns 403 Forbidden."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Brief courses retrieved successfully", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Access denied to this campus or course", content = @Content),
        @ApiResponse(responseCode = "404", description = "Campus or course not found", content = @Content)
    })
    public ResponseEntity<java.util.List<edu.utec.planificador.dto.response.CourseBriefResponse>> getBriefCoursesForCurrentUser(
        @Parameter(description = "Campus ID", required = true, example = "1")
        @PathVariable Long campusId,
        @Parameter(description = "Optional course ID to validate ownership and return only that course", example = "10")
        @RequestParam(required = false) Long courseId
    ) {
        log.info("GET /courses/campus/{}/my-courses - courseId={}", campusId, courseId);

        java.util.List<edu.utec.planificador.dto.response.CourseBriefResponse> response =
            courseService.getCoursesForCurrentUserInCampus(campusId, courseId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/periods")
    @Operation(
        summary = "Get course periods by campus",
        description = "Returns all unique academic periods for the authenticated user's courses in a specific campus. " +
                      "Periods are formatted as 'YYYY-XS' where X is 1 for odd semesters or 2 for even semesters. " +
                      "For example: '2025-1S' for semester 3 in 2025, or '2025-2S' for semester 4 in 2025.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Periods retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = PeriodResponse.class))
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "User does not have access to the specified campus",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Campus not found",
            content = @Content
        )
    })
    public ResponseEntity<List<PeriodResponse>> getPeriodsByCampus(
        @Parameter(description = "Campus ID to filter courses", required = true, example = "1")
        @RequestParam Long campusId
    ) {
        log.info("GET /courses/periods - campusId: {}", campusId);

        List<PeriodResponse> response = courseService.getPeriodsByCampus(campusId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get course by ID",
        description = "Retrieves a course by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Course found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course not found",
            content = @Content
        )
    })
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        log.info("GET /courses/{} - Retrieving course", id);
        
        CourseResponse response = courseService.getCourseById(id);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update course",
        description = "Updates an existing course by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Course updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course or curricular unit not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content
        )
    })
    public ResponseEntity<CourseResponse> updateCourse(
        @PathVariable Long id,
        @Valid @RequestBody CourseRequest request
    ) {
        log.info("PUT /courses/{} - Updating course", id);
        
        CourseResponse response = courseService.updateCourse(id, request);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete course",
        description = "Deletes a course by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Course deleted successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course not found",
            content = @Content
        )
    })
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.info("DELETE /courses/{} - Deleting course", id);
        
        courseService.deleteCourse(id);
        
        return ResponseEntity.noContent().build();
    }

    // ==================== Sustainable Development Goals (ODS) ====================

    @PostMapping("/{id}/sustainable-development-goals/{goal}")
    @Operation(
        summary = "Add Sustainable Development Goal",
        description = "Adds a Sustainable Development Goal (ODS) to a course"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Goal added successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course not found",
            content = @Content
        )
    })
    public ResponseEntity<CourseResponse> addSustainableDevelopmentGoal(
        @PathVariable Long id,
        @PathVariable SustainableDevelopmentGoal goal
    ) {
        log.info("POST /courses/{}/sustainable-development-goals/{} - Adding ODS", id, goal);
        
        CourseResponse response = courseService.addSustainableDevelopmentGoal(id, goal);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/sustainable-development-goals/{goal}")
    @Operation(
        summary = "Remove Sustainable Development Goal",
        description = "Removes a Sustainable Development Goal (ODS) from a course"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Goal removed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course not found",
            content = @Content
        )
    })
    public ResponseEntity<CourseResponse> removeSustainableDevelopmentGoal(
        @PathVariable Long id,
        @PathVariable SustainableDevelopmentGoal goal
    ) {
        log.info("DELETE /courses/{}/sustainable-development-goals/{} - Removing ODS", id, goal);
        
        CourseResponse response = courseService.removeSustainableDevelopmentGoal(id, goal);
        
        return ResponseEntity.ok(response);
    }

    // ==================== Universal Design Learning Principles ====================

    @PostMapping("/{id}/universal-design-learning-principles/{principle}")
    @Operation(
        summary = "Add Universal Design Learning Principle",
        description = "Adds a Universal Design Learning Principle to a course"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Principle added successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course not found",
            content = @Content
        )
    })
    public ResponseEntity<CourseResponse> addUniversalDesignLearningPrinciple(
        @PathVariable Long id,
        @PathVariable UniversalDesignLearningPrinciple principle
    ) {
        log.info("POST /courses/{}/universal-design-learning-principles/{} - Adding principle", id, principle);
        
        CourseResponse response = courseService.addUniversalDesignLearningPrinciple(id, principle);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/universal-design-learning-principles/{principle}")
    @Operation(
        summary = "Remove Universal Design Learning Principle",
        description = "Removes a Universal Design Learning Principle from a course"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Principle removed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course not found",
            content = @Content
        )
    })
    public ResponseEntity<CourseResponse> removeUniversalDesignLearningPrinciple(
        @PathVariable Long id,
        @PathVariable UniversalDesignLearningPrinciple principle
    ) {
        log.info("DELETE /courses/{}/universal-design-learning-principles/{} - Removing principle", id, principle);
        
        CourseResponse response = courseService.removeUniversalDesignLearningPrinciple(id, principle);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/pdf-data")
    @Operation(
        summary = "Get course data for PDF generation",
        description = "Returns all course data needed for PDF generation in the frontend. " +
                      "Includes: course description, dates, shift, investigation flags, hours per delivery format, " +
                      "teachers information, program name, and curricular unit details (name and credits)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "PDF data retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CoursePdfDataResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Course not found",
            content = @Content
        )
    })
    public ResponseEntity<CoursePdfDataResponse> getCoursePdfData(@PathVariable Long id) {
        log.info("GET /courses/{}/pdf-data - Getting PDF data", id);
        
        CoursePdfDataResponse response = courseService.getCoursePdfData(id);
        
        return ResponseEntity.ok(response);
    }
}

