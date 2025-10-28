package edu.utec.planificador.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private int minLength;
    private int maxLength;
    private boolean requireDigit;
    private boolean requireLowercase;
    private boolean requireUppercase;
    private boolean requireSpecialChar;

    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        if (password.length() < minLength || password.length() > maxLength) {
            addViolation(context, String.format("La contraseña debe tener entre %d y %d caracteres", minLength, maxLength));
            return false;
        }

        if (requireDigit && !DIGIT_PATTERN.matcher(password).matches()) {
            addViolation(context, "La contraseña debe contener al menos un número");
            return false;
        }

        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).matches()) {
            addViolation(context, "La contraseña debe contener al menos una letra minúscula");
            return false;
        }

        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).matches()) {
            addViolation(context, "La contraseña debe contener al menos una letra mayúscula");
            return false;
        }

        if (requireSpecialChar && !SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            addViolation(context, "La contraseña debe contener al menos un carácter especial (!@#$%^&*()_+-=[]{};':\"\\|,.<>/?)");
            return false;
        }

        if (isCommonPassword(password)) {
            addViolation(context, "La contraseña es demasiado común. Por favor, elija una más segura");
            return false;
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private boolean isCommonPassword(String password) {
        String lowerPassword = password.toLowerCase();
        
        String[] commonPasswords = {
            "password", "123456", "12345678", "qwerty", "abc123",
            "monkey", "1234567", "letmein", "trustno1", "dragon",
            "baseball", "iloveyou", "master", "sunshine", "ashley",
            "bailey", "passw0rd", "shadow", "123123", "654321",
            "superman", "qazwsx", "michael", "football"
        };

        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }

        if (lowerPassword.matches("^(\\w)\\1+$")) {
            return true;
        }

        if (lowerPassword.matches("^\\d+$")) {
            return true;
        }

        if (lowerPassword.matches("^[a-z]+$") || lowerPassword.matches("^[A-Z]+$")) {
            return true;
        }

        return false;
    }
}
