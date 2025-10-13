package edu.utec.planificador.enumerated;

public enum PartialGradingSystem implements DisplayableEnum {
    
    PGS_1(1, "PE 25% + SE 35% + EC 40%"),
    PGS_2(2, "PE 30% + SE 30% + EC 40%"),
    PGS_3(3, "PE 25% + SE 35% + LAB 20% + EC 20%"),
    PGS_4(4, "PT 70% + TE 30%"),
    PGS_5(5, "PE 30% + SE 50% + TE 20%"),
    PGS_6(6, "Actividades 60% + Participación 40%"),
    PGS_7(7, "Tecnólogos UTU/UTEC/UDELAR (PE 50% + SE 50%)"),
    PGS_8(8, "Tecnólogos UTU/UTEC/UDELAR (PE 40% + SE 60%)"),
    PGS_9(9, "ACT. TEÓRICO-PRÁC. 70% + EC 30%"),
    PGS_10(10, "Inglés (PT 70% + TE 30%)"),
    PGS_11(11, "Sin examen (LAB 30% + EC 10% + Proyecto 20% + PE 10% + SE 10% + TE 10% + CE 10%)"),
    PGS_12(12, "Sin examen, para talleres (LAB 30% + EC 30% + Proyecto 40%)");

    private final int code;
    private final String displayValue;

    PartialGradingSystem(int code, String displayValue) {
        this.code = code;
        this.displayValue = displayValue;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static PartialGradingSystem fromCode(int code) {
        for (PartialGradingSystem system : values()) {
            if (system.code == code) return system;
        }
        throw new IllegalArgumentException("Código de SCP no válido: " + code);
    }
}
