package edu.utec.planificador.enumeration;

public enum DeliveryFormat implements DisplayableEnum {
    
    IN_PERSON("Presencial"),
    VIRTUAL("Virtual"),
    HYBRID("Híbrido");

    private final String displayValue;

    DeliveryFormat(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
