package edu.utec.planificador.entity;

import edu.utec.planificador.enumeration.CognitiveProcess;
import edu.utec.planificador.enumeration.LearningModality;
import edu.utec.planificador.enumeration.LearningResource;
import edu.utec.planificador.enumeration.TeachingStrategy;
import edu.utec.planificador.enumeration.TransversalCompetency;
import edu.utec.planificador.util.Constants;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Setter
    @Column(nullable = false, length = Constants.MAX_ACTIVITY_DESCRIPTION_LENGTH)
    @NotBlank
    @Size(max = Constants.MAX_ACTIVITY_DESCRIPTION_LENGTH)
    private String description;

    @Setter
    @Column(nullable = false)
    @NotNull
    @Min(Constants.MIN_ACTIVITY_DURATION)
    private Integer durationInMinutes;

    @ElementCollection(targetClass = CognitiveProcess.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "activity_cognitive_processes", joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "cognitive_process")
    @Enumerated(EnumType.STRING)
    private Set<CognitiveProcess> cognitiveProcesses = new HashSet<>();

    @ElementCollection(targetClass = TransversalCompetency.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "activity_transversal_competencies", joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "transversal_competency")
    @Enumerated(EnumType.STRING)
    private Set<TransversalCompetency> transversalCompetencies = new HashSet<>();

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private LearningModality learningModality;

    @ElementCollection(targetClass = TeachingStrategy.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "activity_teaching_strategies", joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "teaching_strategy")
    @Enumerated(EnumType.STRING)
    private Set<TeachingStrategy> teachingStrategies = new HashSet<>();

    @ElementCollection(targetClass = LearningResource.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "activity_learning_resources", joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "learning_resource")
    @Enumerated(EnumType.STRING)
    private Set<LearningResource> learningResources = new HashSet<>();
}
