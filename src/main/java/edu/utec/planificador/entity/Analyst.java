package edu.utec.planificador.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Analyst")
public class Analyst extends Position {

    // Campos específicos para Analista se agregarán más adelante
}