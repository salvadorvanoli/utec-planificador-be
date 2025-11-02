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
@Table(name = "administrator")
public class Administrator extends Position {

    public Administrator(User user) {
        super(Role.ADMINISTRATOR);
        setUser(user);
    }
}
