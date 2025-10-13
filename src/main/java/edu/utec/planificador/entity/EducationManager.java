package edu.utec.planificador.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "EducationManager")
public class EducationManager extends Position {

    // Campos específicos para ResponsableEducacion se agregarán más adelante
}