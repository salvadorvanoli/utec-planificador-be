package edu.utec.planificador.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString(callSuper = true, exclude = {"courses"})
@Entity
@Table(name = "teacher")
public class Teacher extends Position {

    @ManyToMany(mappedBy = "teachers")
    private List<Course> courses = new ArrayList<>();
}
