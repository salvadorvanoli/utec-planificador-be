package edu.utec.planificador.enumeration;

public enum Shift implements DisplayableEnum {
    
    MORNING("Matutino"),
    EVENING("Vespertino");

    private final String displayValue;

    Shift(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
