package edu.utec.planificador.datatype;

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
    @Size(max = Constants.MAX_IDENTITY_DOCUMENT_LENGTH)
    private String identityDocument;

    @Column(length = Constants.MAX_USER_NAME_LENGTH)
    @Size(max = Constants.MAX_USER_NAME_LENGTH)
    private String name;

    @Column(length = Constants.MAX_USER_LASTNAMES_LENGTH)
    @Size(max = Constants.MAX_USER_LASTNAMES_LENGTH)
    private String lastName;

    @Column(length = Constants.MAX_PHONE_NUMBER_LENGTH)
    @Pattern(regexp = Constants.PHONE_NUMBER_REGEX, message = "{validation.phone.uruguay.format}")
    private String phoneNumber;

    @Column(length = Constants.MAX_COUNTRY_LENGTH)
    @Size(max = Constants.MAX_COUNTRY_LENGTH)
    private String country;

    @Column(length = Constants.MAX_CITY_LENGTH)
    @Size(max = Constants.MAX_CITY_LENGTH)
    private String city;

    @Column(length = Constants.MAX_EMAIL_LENGTH)
    @Email
    @Size(max = Constants.MAX_EMAIL_LENGTH)
    private String personalEmail;
}
