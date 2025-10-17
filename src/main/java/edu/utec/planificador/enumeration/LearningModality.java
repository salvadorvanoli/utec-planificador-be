package edu.utec.planificador.enumeration;

public enum LearningModality implements DisplayableEnum {
    
    VIRTUAL("Virtual"),
    IN_PERSON("Presencial"),
    SIMULTANEOUS_IN_PERSON_VIRTUAL("Simultáneamente presencial-virtual"),
    AUTONOMOUS("Autónomo"),
    NOT_DETERMINED("Sin determinar");

    private final String displayValue;

    LearningModality(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
