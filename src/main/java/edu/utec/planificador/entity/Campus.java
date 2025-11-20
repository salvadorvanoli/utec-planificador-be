package edu.utec.planificador.entity;

import edu.utec.planificador.datatype.Location;
import edu.utec.planificador.util.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString(exclude = {"programs"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Entity
@Table(name = "campus")
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Setter
    @Column(unique = true, nullable = false, length = Constants.MAX_CAMPUS_NAME_LENGTH)
    @NotBlank
    @Size(max = Constants.MAX_CAMPUS_NAME_LENGTH)
    private String name;

    @Setter
    @Embedded
    @Valid
    private Location location;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regional_technological_institute_id", nullable = false)
    @NotNull
    private RegionalTechnologicalInstitute regionalTechnologicalInstitute;

    @ManyToMany
    @JoinTable(
        name = "campus_program",
        joinColumns = @JoinColumn(name = "campus_id"),
        inverseJoinColumns = @JoinColumn(name = "program_id")
    )
    @OrderBy("name ASC")
    private List<Program> programs = new ArrayList<>();
}
