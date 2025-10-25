package edu.utec.planificador.entity;

import edu.utec.planificador.enumeration.DomainArea;
import edu.utec.planificador.enumeration.ProfessionalCompetency;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "curricular_unit")
public class CurricularUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = Constants.MAX_CURRICULAR_UNIT_NAME_LENGTH)
    @NotBlank
    @Size(max = Constants.MAX_CURRICULAR_UNIT_NAME_LENGTH)
    private String name;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    private Integer credits;

    @ElementCollection(targetClass = DomainArea.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "curricular_unit_domain_areas", joinColumns = @JoinColumn(name = "curricular_unit_id"))
    @Column(name = "domain_area")
    @Enumerated(EnumType.STRING)
    private Set<DomainArea> domainAreas = new HashSet<>();

    @ElementCollection(targetClass = ProfessionalCompetency.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "curricular_unit_professional_competencies", joinColumns = @JoinColumn(name = "curricular_unit_id"))
    @Column(name = "professional_competency")
    @Enumerated(EnumType.STRING)
    private Set<ProfessionalCompetency> professionalCompetencies = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    @NotNull
    private Term term;

    @OneToMany(mappedBy = "curricularUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses = new ArrayList<>();
}
