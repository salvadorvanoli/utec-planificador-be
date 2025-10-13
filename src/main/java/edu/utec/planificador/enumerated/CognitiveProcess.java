package edu.utec.planificador.enumerated;

public enum CognitiveProcess implements DisplayableEnum {
    
    REMEMBER("Recordar"),
    UNDERSTAND("Comprender"),
    APPLY("Aplicar"),
    ANALYZE("Analizar"),
    EVALUATE("Evaluar"),
    CREATE("Crear"),
    NOT_DETERMINED("Sin determinar");

    private final String displayValue;

    CognitiveProcess(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
