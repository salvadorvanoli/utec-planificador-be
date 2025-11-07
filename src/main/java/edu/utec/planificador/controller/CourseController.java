package edu.utec.planificador.controller;

import edu.utec.planificador.dto.request.CourseRequest;
import edu.utec.planificador.dto.response.CourseResponse;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;
import edu.utec.planificador.service.CourseService;
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
}

