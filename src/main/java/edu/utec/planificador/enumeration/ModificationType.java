package edu.utec.planificador.enumeration;

public enum ModificationType {
    DELETE(0, "Eliminación"),
    CREATE(1, "Creación"),
    UPDATE(2, "Modificación");

    private final int value;
    private final String displayValue;

    ModificationType(int value, String displayValue) {
        this.value = value;
        this.displayValue = displayValue;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static ModificationType fromValue(int value) {
        for (ModificationType type : ModificationType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException(
            "Valor de tipo de modificación inválido: " + value
        );
    }
}
