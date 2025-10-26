package edu.utec.planificador.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.ToString;

@ToString(callSuper = true)
@Entity
@Table(name = "analyst")
public class Analyst extends Position {

}
