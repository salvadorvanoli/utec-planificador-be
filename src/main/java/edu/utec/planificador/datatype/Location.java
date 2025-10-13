package edu.utec.planificador.valueobject;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Location {

    private String state;
    private String city;
    private String street;
    private String number;
}
