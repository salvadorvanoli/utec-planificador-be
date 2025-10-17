package edu.utec.planificador.enumeration;

public enum DomainArea implements DisplayableEnum {
    
    INSTALLATION_DESIGN(1, "Diseño y realización de instalaciones, dispositivos, instrumentos y sistemas de instrumentos destinados a uso médico utilizando recursos de Ingeniería biomédica."),
    INSTALLATION_MANAGEMENT(2, "Gestión de instalaciones, dispositivos, instrumentos y sistemas de instrumentos, destinados a uso médico, utilizando recursos de Ingeniería en Biomédica."),
    RDI_PROJECTS(3, "Diseño y ejecución de Proyectos de Investigación, Desarrollo e Innovación (I+D+I) en Ingeniería Biomédica y en Medicina."),
    SERVICE_MANAGEMENT(4, "Dirección /Dirección Técnica en unidades de prestación de servicios para la salud.");

    private final int code;
    private final String displayValue;

    DomainArea(int code, String displayValue) {
        this.code = code;
        this.displayValue = displayValue;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static DomainArea fromCode(int code) {
        for (DomainArea area : values()) {
            if (area.getCode() == code) return area;
        }
        throw new IllegalArgumentException("Código de área no válido: " + code);
    }
}
