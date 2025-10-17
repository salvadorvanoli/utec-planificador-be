package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.response.EnumResponse;
import edu.utec.planificador.enumeration.*;
import edu.utec.planificador.service.EnumService;
import edu.utec.planificador.util.EnumUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnumServiceImpl implements EnumService {

    @Override
    public Map<String, List<EnumResponse>> getAllEnums() {
        Map<String, List<EnumResponse>> enums = new HashMap<>();
        
        enums.put("domainAreas", getDomainAreas());
        enums.put("cognitiveProcesses", getCognitiveProcesses());
        enums.put("shifts", getShifts());
        enums.put("deliveryFormats", getDeliveryFormats());
        enums.put("transversalCompetencies", getTransversalCompetencies());
        enums.put("partialGradingSystems", getPartialGradingSystems());
        enums.put("professionalCompetencies", getProfessionalCompetencies());
        enums.put("sustainableDevelopmentGoals", getSustainableDevelopmentGoals());
        enums.put("teachingStrategies", getTeachingStrategies());
        enums.put("learningModalities", getLearningModalities());
        enums.put("learningResources", getLearningResources());
        enums.put("udlPrinciples", getUniversalDesignLearningPrinciples());
        
        return enums;
    }

    @Override
    public List<EnumResponse> getDomainAreas() {
        return EnumUtils.toEnumResponseList(DomainArea.values());
    }

    @Override
    public List<EnumResponse> getCognitiveProcesses() {
        return EnumUtils.toEnumResponseList(CognitiveProcess.values());
    }

    @Override
    public List<EnumResponse> getShifts() {
        return EnumUtils.toEnumResponseList(Shift.values());
    }

    @Override
    public List<EnumResponse> getDeliveryFormats() {
        return EnumUtils.toEnumResponseList(DeliveryFormat.values());
    }

    @Override
    public List<EnumResponse> getTransversalCompetencies() {
        return EnumUtils.toEnumResponseList(TransversalCompetency.values());
    }

    @Override
    public List<EnumResponse> getPartialGradingSystems() {
        return EnumUtils.toEnumResponseList(PartialGradingSystem.values());
    }

    @Override
    public List<EnumResponse> getProfessionalCompetencies() {
        return EnumUtils.toEnumResponseList(ProfessionalCompetency.values());
    }

    @Override
    public List<EnumResponse> getSustainableDevelopmentGoals() {
        return EnumUtils.toEnumResponseList(SustainableDevelopmentGoal.values());
    }

    @Override
    public List<EnumResponse> getTeachingStrategies() {
        return EnumUtils.toEnumResponseList(TeachingStrategy.values());
    }

    @Override
    public List<EnumResponse> getLearningModalities() {
        return EnumUtils.toEnumResponseList(LearningModality.values());
    }

    @Override
    public List<EnumResponse> getLearningResources() {
        return EnumUtils.toEnumResponseList(LearningResource.values());
    }

    @Override
    public List<EnumResponse> getUniversalDesignLearningPrinciples() {
        return EnumUtils.toEnumResponseList(UniversalDesignLearningPrinciple.values());
    }
}
