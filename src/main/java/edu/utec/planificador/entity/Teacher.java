package edu.utec.planificador.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "teacher")
public class Teacher extends Position {

    @ManyToMany(mappedBy = "teachers")
    private List<Course> courses = new ArrayList<>();
}
