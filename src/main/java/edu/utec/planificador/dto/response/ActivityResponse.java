package edu.utec.planificador.dto.response;

import edu.utec.planificador.enumeration.LearningModality;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class ActivityResponse {
    private Long id;
    private String title;
    private String description;
    private String color;
    private Integer durationInMinutes;
    private LearningModality learningModality;
    private Long programmaticContentId;
    private Set<String> cognitiveProcesses;
    private Set<String> transversalCompetencies;
    private Set<String> teachingStrategies;
    private Set<String> learningResources;
}
