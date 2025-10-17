package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.EnumResponse;
import java.util.List;
import java.util.Map;

public interface EnumService {
    
    Map<String, List<EnumResponse>> getAllEnums();
    
    List<EnumResponse> getDomainAreas();
    List<EnumResponse> getCognitiveProcesses();
    List<EnumResponse> getShifts();
    List<EnumResponse> getDeliveryFormats();
    List<EnumResponse> getTransversalCompetencies();
    List<EnumResponse> getPartialGradingSystems();
    List<EnumResponse> getProfessionalCompetencies();
    List<EnumResponse> getSustainableDevelopmentGoals();
    List<EnumResponse> getTeachingStrategies();
    List<EnumResponse> getLearningModalities();
    List<EnumResponse> getLearningResources();
    List<EnumResponse> getUniversalDesignLearningPrinciples();
}
