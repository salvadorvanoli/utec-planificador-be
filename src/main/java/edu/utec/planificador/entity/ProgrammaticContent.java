package edu.utec.planificador.entity;

import edu.utec.planificador.util.Constants;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "programmatic_content")
public class ProgrammaticContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = Constants.MAX_PROGRAMMATIC_CONTENT_LENGTH)
    @NotBlank(message = "El contenido programático es obligatorio")
    @Size(max = Constants.MAX_PROGRAMMATIC_CONTENT_LENGTH, message = "El contenido programático no puede exceder " + Constants.MAX_PROGRAMMATIC_CONTENT_LENGTH + " caracteres")
    private String content;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();
}
