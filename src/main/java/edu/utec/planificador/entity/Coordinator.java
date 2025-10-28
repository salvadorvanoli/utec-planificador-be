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
@Table(name = "coordinator")
public class Coordinator extends Position {

    public Coordinator(User user) {
        super(Role.COORDINATOR);
        setUser(user);
    }
}
