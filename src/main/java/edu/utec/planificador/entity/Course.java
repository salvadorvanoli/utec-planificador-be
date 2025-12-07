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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@ToString(exclude = {"curricularUnit", "campus", "teachers", "weeklyPlannings", "modifications", "officeHours"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Constructor con campos obligatorios: shift, startDate, endDate, curricularUnit, campus
    public Course(Shift shift, LocalDate startDate, LocalDate endDate, CurricularUnit curricularUnit, Campus campus) {
        this.shift = shift;
        this.startDate = startDate;
        this.endDate = endDate;
        this.curricularUnit = curricularUnit;
        this.campus = campus;
        // Initialize collections to avoid null
        this.hoursPerDeliveryFormat = new HashMap<>();
        this.sustainableDevelopmentGoals = new HashSet<>();
        this.universalDesignLearningPrinciples = new HashSet<>();
        this.teachers = new ArrayList<>();
        this.weeklyPlannings = new ArrayList<>();
        this.modifications = new ArrayList<>();
        // Initialize booleans to false
        this.isRelatedToInvestigation = false;
        this.involvesActivitiesWithProductiveSector = false;
    }

    // Constructor completo (mantener para compatibilidad con DataSeeder)
    public Course(Shift shift, String description, LocalDate startDate, LocalDate endDate, 
                  PartialGradingSystem partialGradingSystem, CurricularUnit curricularUnit, Campus campus) {
        this.shift = shift;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.partialGradingSystem = partialGradingSystem;
        this.curricularUnit = curricularUnit;
        this.campus = campus;
        // Initialize collections to avoid null
        this.hoursPerDeliveryFormat = new HashMap<>();
        this.sustainableDevelopmentGoals = new HashSet<>();
        this.universalDesignLearningPrinciples = new HashSet<>();
        this.teachers = new ArrayList<>();
        this.weeklyPlannings = new ArrayList<>();
        this.modifications = new ArrayList<>();
        // Initialize booleans to false
        this.isRelatedToInvestigation = false;
        this.involvesActivitiesWithProductiveSector = false;
    }

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Shift shift;

    @Setter
    @Column(length = Constants.MAX_COURSE_DESCRIPTION_LENGTH, nullable = true)
    @Size(max = Constants.MAX_COURSE_DESCRIPTION_LENGTH)
    private String description;

    @Setter
    @Column(nullable = false)
    @NotNull
    private LocalDate startDate;

    @Setter
    @Column(nullable = false)
    @NotNull
    private LocalDate endDate;

    @Setter
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private PartialGradingSystem partialGradingSystem;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "course_delivery_format_hours", joinColumns = @JoinColumn(name = "course_id"))
    @MapKeyColumn(name = "delivery_format")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "hours")
    private Map<DeliveryFormat, Integer> hoursPerDeliveryFormat = new HashMap<>();

    @Setter
    @Column(nullable = false)
    @NotNull
    private Boolean isRelatedToInvestigation = false;

    @Setter
    @Column(nullable = false)
    @NotNull
    private Boolean involvesActivitiesWithProductiveSector = false;

    @ElementCollection(targetClass = SustainableDevelopmentGoal.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "course_sustainable_development_goals", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "sustainable_development_goal")
    @Enumerated(EnumType.STRING)
    private Set<SustainableDevelopmentGoal> sustainableDevelopmentGoals = new HashSet<>();

    @ElementCollection(targetClass = UniversalDesignLearningPrinciple.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "course_universal_design_learning_principles", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "universal_design_learning_principle")
    @Enumerated(EnumType.STRING)
    private Set<UniversalDesignLearningPrinciple> universalDesignLearningPrinciples = new HashSet<>();

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curricular_unit_id", nullable = false)
    @NotNull
    private CurricularUnit curricularUnit;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = false)
    @NotNull
    private Campus campus;

    @ManyToMany
    @JoinTable(
        name = "course_teacher",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private List<Teacher> teachers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "course_id")
    @OrderBy("weekNumber ASC")
    private List<WeeklyPlanning> weeklyPlannings = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "course_id")
    @OrderBy("id DESC")
    private List<Modification> modifications = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "course_id")
    @OrderBy("date ASC, startTime ASC")
    private List<OfficeHours> officeHours = new ArrayList<>();

    public String getPeriod() {
        if (startDate == null) {
            return null;
        }
        
        int year = startDate.getYear();
        int month = startDate.getMonthValue();
        
        int semester = (month <= 7) ? 1 : 2;
        
        return String.format("%d-%dS", year, semester);
    }
}
