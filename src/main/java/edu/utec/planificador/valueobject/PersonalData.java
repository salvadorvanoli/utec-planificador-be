package edu.utec.planificador.valueobject;

import edu.utec.planificador.util.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Embeddable
public class PersonalData {

    @Column(unique = true, length = Constants.MAX_IDENTITY_DOCUMENT_LENGTH)
    @Size(max = Constants.MAX_IDENTITY_DOCUMENT_LENGTH, message = "El documento de identidad no puede exceder " + Constants.MAX_IDENTITY_DOCUMENT_LENGTH + " caracteres")
    private String identityDocument;

    @Column(length = Constants.MAX_USER_NAME_LENGTH)
    @Size(max = Constants.MAX_USER_NAME_LENGTH, message = "El nombre no puede exceder " + Constants.MAX_USER_NAME_LENGTH + " caracteres")
    private String name;

    @Column(length = Constants.MAX_USER_LASTNAMES_LENGTH)
    @Size(max = Constants.MAX_USER_LASTNAMES_LENGTH, message = "El apellido no puede exceder " + Constants.MAX_USER_LASTNAMES_LENGTH + " caracteres")
    private String lastName;

    @Column(length = Constants.MAX_PHONE_NUMBER_LENGTH)
    @Pattern(regexp = Constants.PHONE_NUMBER_REGEX, message = "El número de teléfono debe tener el formato 09XXXXXXX o 9XXXXXXX")
    private String phoneNumber;

    @Column(length = Constants.MAX_COUNTRY_LENGTH)
    @Size(max = Constants.MAX_COUNTRY_LENGTH, message = "El país no puede exceder " + Constants.MAX_COUNTRY_LENGTH + " caracteres")
    private String country;

    @Column(length = Constants.MAX_CITY_LENGTH)
    @Size(max = Constants.MAX_CITY_LENGTH, message = "La ciudad no puede exceder " + Constants.MAX_CITY_LENGTH + " caracteres")
    private String city;

    @Column(length = Constants.MAX_EMAIL_LENGTH)
    @Email(message = "El formato del email personal no es válido")
    @Size(max = Constants.MAX_EMAIL_LENGTH, message = "El email personal no puede exceder " + Constants.MAX_EMAIL_LENGTH + " caracteres")
    private String personalEmail;
}
