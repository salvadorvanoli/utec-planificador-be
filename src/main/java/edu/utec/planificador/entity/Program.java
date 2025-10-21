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
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "program")
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = Constants.MAX_PROGRAM_NAME_LENGTH)
    @NotBlank(message = "El nombre de la carrera es obligatorio")
    @Size(max = Constants.MAX_PROGRAM_NAME_LENGTH, message = "El nombre de la carrera no puede exceder " + Constants.MAX_PROGRAM_NAME_LENGTH + " caracteres")
    private String name;

    @Column(nullable = false)
    @NotNull(message = "La duración en semestres es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos 1 semestre")
    private Integer durationInTerms;

    @Column(nullable = false)
    @NotNull(message = "El total de créditos es obligatorio")
    @Min(value = 1, message = "El total de créditos debe ser al menos 1")
    private Integer totalCredits;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("number ASC")
    private List<Term> terms = new ArrayList<>();
}
