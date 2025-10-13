package edu.utec.planificador.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Coordinator")
public class Coordinator extends Position {

    // Campos específicos para Coordinador se agregarán más adelante
}