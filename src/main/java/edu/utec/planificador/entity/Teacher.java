package edu.utec.planificador.entity;

import edu.utec.planificador.enumeration.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString(callSuper = true, exclude = {"courses"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "teacher")
public class Teacher extends Position {

    @ManyToMany(mappedBy = "teachers")
    private List<Course> courses = new ArrayList<>();

    public Teacher(User user) {
        super(Role.TEACHER);
        setUser(user);
    }
}
