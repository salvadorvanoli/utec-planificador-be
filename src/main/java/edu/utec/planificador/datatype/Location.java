package edu.utec.planificador.datatype;

import edu.utec.planificador.util.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Embeddable
public class Location {

    @Column(length = Constants.MAX_STATE_LENGTH)
    @Size(max = Constants.MAX_STATE_LENGTH, message = "El departamento no puede exceder " + Constants.MAX_STATE_LENGTH + " caracteres")
    private String state;

    @Column(length = Constants.MAX_CITY_LENGTH)
    @Size(max = Constants.MAX_CITY_LENGTH, message = "La ciudad no puede exceder " + Constants.MAX_CITY_LENGTH + " caracteres")
    private String city;

    @Column(length = Constants.MAX_STREET_LENGTH)
    @Size(max = Constants.MAX_STREET_LENGTH, message = "La calle no puede exceder " + Constants.MAX_STREET_LENGTH + " caracteres")
    private String street;

    @Column(length = Constants.MAX_STREET_NUMBER_LENGTH)
    @Size(max = Constants.MAX_STREET_NUMBER_LENGTH, message = "El número no puede exceder " + Constants.MAX_STREET_NUMBER_LENGTH + " caracteres")
    private String number;
}
