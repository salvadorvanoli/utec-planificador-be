package edu.utec.planificador.enumeration;

public enum TransversalCompetency implements DisplayableEnum {
    
    COMMUNICATION("Comunicación"),
    TEAMWORK("Trabajo en equipo"),
    LEARNING_SELF_REGULATION("Autorregulación del aprendizaje"),
    CRITICAL_THINKING("Pensamiento crítico"),
    NOT_DETERMINED("Sin determinar");

    private final String displayValue;

    TransversalCompetency(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
