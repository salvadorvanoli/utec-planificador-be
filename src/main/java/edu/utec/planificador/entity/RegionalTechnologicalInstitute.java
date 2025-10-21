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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "regional_technological_institute")
public class RegionalTechnologicalInstitute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = Constants.MAX_RTI_NAME_LENGTH)
    @NotBlank(message = "El nombre del instituto tecnológico regional es obligatorio")
    @Size(max = Constants.MAX_RTI_NAME_LENGTH, message = "El nombre del instituto tecnológico regional no puede exceder " + Constants.MAX_RTI_NAME_LENGTH + " caracteres")
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("name ASC")
    private List<Campus> campuses = new ArrayList<>();
}
