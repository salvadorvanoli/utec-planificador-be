package edu.utec.planificador.entity;

import edu.utec.planificador.util.Constants;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString(exclude = {"terms"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "program")
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Setter(AccessLevel.PACKAGE)
    @Column(nullable = false, length = Constants.MAX_PROGRAM_NAME_LENGTH)
    @NotBlank
    @Size(max = Constants.MAX_PROGRAM_NAME_LENGTH)
    private String name;

    @Setter(AccessLevel.PACKAGE)
    @Column(nullable = false)
    @NotNull
    @Min(1)
    private Integer durationInTerms;

    @Setter(AccessLevel.PACKAGE)
    @Column(nullable = false)
    @NotNull
    @Min(1)
    private Integer totalCredits;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("number ASC")
    private List<Term> terms = new ArrayList<>();
}
