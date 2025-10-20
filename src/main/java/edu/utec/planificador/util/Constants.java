package edu.utec.planificador.util;

public final class Constants {

    // User constants
    public static final String EMAIL_REGEX = "^[a-zA-Z]+\\.[a-zA-Z]+@utec\\.edu\\.uy$";
    public static final String PHONE_NUMBER_REGEX = "^(09[0-9]{7}|9[0-9]{7})$";
    public static final int MAX_EMAIL_LENGTH = 150;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final int MAX_IDENTITY_DOCUMENT_LENGTH = 8;
    public static final int MAX_USER_NAME_LENGTH = 40;
    public static final int MAX_USER_LASTNAMES_LENGTH = 80;
    public static final int MAX_PHONE_NUMBER_LENGTH = 9;
    public static final int MAX_COUNTRY_LENGTH = 50;
    public static final int MAX_CITY_LENGTH = 50;

    // Regional Technical Institute constants
    public static final int MAX_RTI_NAME_LENGTH = 150;

    // Campus constants
    public static final int MAX_CAMPUS_NAME_LENGTH = 100;

    // Program constants
    public static final int MAX_PROGRAM_NAME_LENGTH = 200;

    // Curricular Unit constants
    public static final int MAX_CURRICULAR_UNIT_NAME_LENGTH = 200;

    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
