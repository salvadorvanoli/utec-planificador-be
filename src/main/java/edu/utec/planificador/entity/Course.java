package edu.utec.planificador.entity;

import edu.utec.planificador.enumeration.DeliveryFormat;
import edu.utec.planificador.enumeration.PartialGradingSystem;
import edu.utec.planificador.enumeration.Shift;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;
import edu.utec.planificador.util.Constants;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "El turno es obligatorio")
    private Shift shift;

    @Column(length = Constants.MAX_COURSE_DESCRIPTION_LENGTH)
    @Size(max = Constants.MAX_COURSE_DESCRIPTION_LENGTH, message = "La descripción no puede exceder " + Constants.MAX_COURSE_DESCRIPTION_LENGTH + " caracteres")
    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @Column(nullable = false)
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    @Column(nullable = false)
    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate endDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "El sistema de calificación parcial es obligatorio")
    private PartialGradingSystem partialGradingSystem;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "course_delivery_format_hours", joinColumns = @JoinColumn(name = "course_id"))
    @MapKeyColumn(name = "delivery_format")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "hours")
    private Map<DeliveryFormat, Integer> formatoHoras = new HashMap<>();

    @Column(nullable = false)
    @NotNull(message = "Debe especificar si está relacionado con investigación")
    private Boolean isRelatedToInvestigation = false;

    @Column(nullable = false)
    @NotNull(message = "Debe especificar si involucra actividades con el sector productivo")
    private Boolean involvesActivitiesWithProductiveSector = false;

    @ElementCollection(targetClass = SustainableDevelopmentGoal.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "course_sustainable_development_goals", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "sustainable_development_goal")
    @Enumerated(EnumType.STRING)
    private Set<SustainableDevelopmentGoal> sustainableDevelopmentGoals = new HashSet<>();

    @ElementCollection(targetClass = UniversalDesignLearningPrinciple.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "course_universal_design_learning_principles", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "universal_design_learning_principle")
    @Enumerated(EnumType.STRING)
    private Set<UniversalDesignLearningPrinciple> universalDesignLearningPrinciples = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curricular_unit_id", nullable = false)
    @NotNull(message = "La unidad curricular es obligatoria")
    private CurricularUnit curricularUnit;

    @ManyToMany
    @JoinTable(
        name = "course_teacher",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private List<Teacher> teachers = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("weekNumber ASC")
    private List<WeeklyPlanning> weeklyPlannings = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Modification> modifications = new ArrayList<>();
}
