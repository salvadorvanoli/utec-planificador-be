package edu.utec.planificador.entity;

import edu.utec.planificador.util.Constants;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "weekly_planning")
public class WeeklyPlanning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "El número de semana es obligatorio")
    @Min(value = Constants.MIN_WEEK_NUMBER, message = "El número de semana debe ser al menos " + Constants.MIN_WEEK_NUMBER)
    @Max(value = Constants.MAX_WEEK_NUMBER, message = "El número de semana no puede exceder " + Constants.MAX_WEEK_NUMBER)
    private Integer weekNumber;

    @Column(nullable = false)
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "weekly_planning_bibliographic_references", joinColumns = @JoinColumn(name = "weekly_planning_id"))
    @Column(name = "reference", length = 500)
    private List<String> bibliographicReferences = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "weekly_planning_programmatic_content",
        joinColumns = @JoinColumn(name = "weekly_planning_id"),
        inverseJoinColumns = @JoinColumn(name = "programmatic_content_id")
    )
    @OrderBy("id ASC")
    private List<ProgrammaticContent> programmaticContents = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Activity> activities = new ArrayList<>();
}
