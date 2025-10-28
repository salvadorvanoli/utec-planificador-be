package edu.utec.planificador.entity;

import edu.utec.planificador.enumeration.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "education_manager")
public class EducationManager extends Position {

    public EducationManager(User user) {
        super(Role.EDUCATION_MANAGER);
        setUser(user);
    }
}
