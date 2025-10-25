package edu.utec.planificador.entity;

import edu.utec.planificador.datatype.PersonalData;
import edu.utec.planificador.util.Constants;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = Constants.MAX_EMAIL_LENGTH)
    @Size(max = Constants.MAX_EMAIL_LENGTH)
    @Pattern(regexp = Constants.EMAIL_REGEX, message = "{validation.email.utec.format}")
    private String utecEmail;

    @Column(nullable = false, length = Constants.MAX_PASSWORD_LENGTH)
    @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH)
    private String password;

    @Embedded
    @Valid
    private PersonalData personalData;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Position> positions = new ArrayList<>();
}
