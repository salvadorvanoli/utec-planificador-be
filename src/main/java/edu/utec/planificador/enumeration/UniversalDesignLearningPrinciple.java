package edu.utec.planificador.enumeration;

public enum UniversalDesignLearningPrinciple implements DisplayableEnum {
    
    MEANS_OF_ENGAGEMENT("Medios de compromiso"),
    MEANS_OF_REPRESENTATION("Medios de representación"),
    MEANS_OF_ACTION_EXPRESSION("Medios de acción y expresión"),
    NONE("Ninguno");

    private final String displayValue;

    UniversalDesignLearningPrinciple(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
