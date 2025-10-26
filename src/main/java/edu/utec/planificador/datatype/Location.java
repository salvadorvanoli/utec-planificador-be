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
    @Size(max = Constants.MAX_STATE_LENGTH)
    private String state;

    @Column(length = Constants.MAX_CITY_LENGTH)
    @Size(max = Constants.MAX_CITY_LENGTH)
    private String city;

    @Column(length = Constants.MAX_STREET_LENGTH)
    @Size(max = Constants.MAX_STREET_LENGTH)
    private String street;

    @Column(length = Constants.MAX_STREET_NUMBER_LENGTH)
    @Size(max = Constants.MAX_STREET_NUMBER_LENGTH)
    private String number;
}
