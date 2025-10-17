package edu.utec.planificador.controller;

import edu.utec.planificador.dto.response.EnumResponse;
import edu.utec.planificador.service.EnumService;
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
public class EnumController {

    private final EnumService enumService;

    public EnumController(EnumService enumService) {
        this.enumService = enumService;
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, List<EnumResponse>>> getAllEnums() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getAllEnums());
    }

    @GetMapping("/domain-areas")
    public ResponseEntity<List<EnumResponse>> getDomainAreas() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getDomainAreas());
    }

    @GetMapping("/cognitive-processes")
    public ResponseEntity<List<EnumResponse>> getCognitiveProcesses() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getCognitiveProcesses());
    }

    @GetMapping("/shifts")
    public ResponseEntity<List<EnumResponse>> getShifts() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getShifts());
    }

    @GetMapping("/delivery-formats")
    public ResponseEntity<List<EnumResponse>> getDeliveryFormats() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getDeliveryFormats());
    }

    @GetMapping("/transversal-competencies")
    public ResponseEntity<List<EnumResponse>> getTransversalCompetencies() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getTransversalCompetencies());
    }

    @GetMapping("/partial-grading-systems")
    public ResponseEntity<List<EnumResponse>> getPartialGradingSystems() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getPartialGradingSystems());
    }

    @GetMapping("/professional-competencies")
    public ResponseEntity<List<EnumResponse>> getProfessionalCompetencies() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getProfessionalCompetencies());
    }

    @GetMapping("/sustainable-development-goals")
    public ResponseEntity<List<EnumResponse>> getSustainableDevelopmentGoals() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getSustainableDevelopmentGoals());
    }

    @GetMapping("/teaching-strategies")
    public ResponseEntity<List<EnumResponse>> getTeachingStrategies() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getTeachingStrategies());
    }

    @GetMapping("/learning-modalities")
    public ResponseEntity<List<EnumResponse>> getLearningModalities() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getLearningModalities());
    }

    @GetMapping("/learning-resources")
    public ResponseEntity<List<EnumResponse>> getLearningResources() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getLearningResources());
    }

    @GetMapping("/universal-design-learning-principles")
    public ResponseEntity<List<EnumResponse>> getUniversalDesignLearningPrinciples() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)))
                .body(enumService.getUniversalDesignLearningPrinciples());
    }
}
