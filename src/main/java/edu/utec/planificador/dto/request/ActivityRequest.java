package edu.utec.planificador.dto.request;

import edu.utec.planificador.enumeration.LearningModality;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import edu.utec.planificador.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
public class ActivityRequest {

    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = Constants.MAX_ACTIVITY_DESCRIPTION_LENGTH)
    private String description;

    @Size(max = 7)
    private String color;

    @NotNull
    @Min(1)
    private Integer durationInMinutes;

    @NotNull
    private LearningModality learningModality;

    @NotNull
    private Long programmaticContentId;

    private Set<String> cognitiveProcesses;
    private Set<String> transversalCompetencies;
    private Set<String> teachingStrategies;
    private Set<String> learningResources;
}
