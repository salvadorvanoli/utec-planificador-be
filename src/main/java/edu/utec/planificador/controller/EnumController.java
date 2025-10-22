package edu.utec.planificador.controller;

import edu.utec.planificador.dto.response.EnumResponse;
import edu.utec.planificador.service.EnumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/enums")
@RequiredArgsConstructor
@Tag(name = "Enumerations", description = "Endpoints to retrieve system enumeration values")
public class EnumController {

    private final EnumService enumService;

    @Operation(
            summary = "Get all enumerations",
            description = "Returns a map with all enumerations available in the system. " +
                    "The result is cached for 24 hours to optimize performance."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enumerations retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all")
    public ResponseEntity<Map<String, List<EnumResponse>>> getAllEnums() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getAllEnums());
    }

    @Operation(
            summary = "Get domain areas",
            description = "Returns the list of available domain areas"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain areas retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/domain-areas")
    public ResponseEntity<List<EnumResponse>> getDomainAreas() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getDomainAreas());
    }

    @Operation(
            summary = "Get cognitive processes",
            description = "Returns the list of available cognitive processes"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cognitive processes retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/cognitive-processes")
    public ResponseEntity<List<EnumResponse>> getCognitiveProcesses() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getCognitiveProcesses());
    }

    @Operation(
            summary = "Get shifts",
            description = "Returns the list of available shifts"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shifts retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/shifts")
    public ResponseEntity<List<EnumResponse>> getShifts() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getShifts());
    }

    @Operation(
            summary = "Get delivery formats",
            description = "Returns the list of available delivery formats"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery formats retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/delivery-formats")
    public ResponseEntity<List<EnumResponse>> getDeliveryFormats() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getDeliveryFormats());
    }

    @Operation(
            summary = "Get transversal competencies",
            description = "Returns the list of available transversal competencies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transversal competencies retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/transversal-competencies")
    public ResponseEntity<List<EnumResponse>> getTransversalCompetencies() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getTransversalCompetencies());
    }

    @Operation(
            summary = "Get partial grading systems",
            description = "Returns the list of available partial grading systems"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partial grading systems retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/partial-grading-systems")
    public ResponseEntity<List<EnumResponse>> getPartialGradingSystems() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getPartialGradingSystems());
    }

    @Operation(
            summary = "Get professional competencies",
            description = "Returns the list of available professional competencies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Professional competencies retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/professional-competencies")
    public ResponseEntity<List<EnumResponse>> getProfessionalCompetencies() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getProfessionalCompetencies());
    }

    @Operation(
            summary = "Get sustainable development goals",
            description = "Returns the list of available sustainable development goals (SDGs)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SDGs retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sustainable-development-goals")
    public ResponseEntity<List<EnumResponse>> getSustainableDevelopmentGoals() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getSustainableDevelopmentGoals());
    }

    @Operation(
            summary = "Get teaching strategies",
            description = "Returns the list of available teaching strategies"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teaching strategies retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/teaching-strategies")
    public ResponseEntity<List<EnumResponse>> getTeachingStrategies() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getTeachingStrategies());
    }

    @Operation(
            summary = "Get learning modalities",
            description = "Returns the list of available learning modalities"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Learning modalities retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/learning-modalities")
    public ResponseEntity<List<EnumResponse>> getLearningModalities() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getLearningModalities());
    }

    @Operation(
            summary = "Get learning resources",
            description = "Returns the list of available learning resources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Learning resources retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/learning-resources")
    public ResponseEntity<List<EnumResponse>> getLearningResources() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getLearningResources());
    }

    @Operation(
            summary = "Get universal design for learning principles",
            description = "Returns the list of available Universal Design for Learning (UDL) principles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UDL principles retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/universal-design-learning-principles")
    public ResponseEntity<List<EnumResponse>> getUniversalDesignLearningPrinciples() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getUniversalDesignLearningPrinciples());
    }
}
