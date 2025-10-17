package edu.utec.planificador.enumeration;

public enum ProfessionalCompetency implements DisplayableEnum {
    
    // AREA 1 - Installation Design
    TECHNICAL_ASSISTANCE(DomainArea.INSTALLATION_DESIGN, 1, "Asistencia técnica en el diseño y realización, bajo normativas, de instalaciones, equipos, instrumentos y sistemas de instrumentos, destinados a uso médico, en instituciones para prestación de servicios de salud, o empresas externas (incluye hardware y software)."),

    // AREA 2 - Installation Management
    EFFICIENT_MANAGEMENT(DomainArea.INSTALLATION_MANAGEMENT, 1, "Realizar actividades de gestión eficiente y bajo normativas de instalaciones de equipos, instrumentos y sistemas de instrumentos, destinados a uso médico, en instituciones de prestación de servicios de salud o en empresas externas."),
    MAINTENANCE_PLANNING(DomainArea.INSTALLATION_MANAGEMENT, 2, "Planificar, dirigir y realizar programas y tareas de mantenimiento preventivo y preventivo, bajo normativas de instalaciones, equipos, instrumentos y sistemas de instrumentos, destinados al uso médico, en instituciones de prestación de servicios de salud o en empresas externas."),
    INSTITUTIONAL_ADVISORY(DomainArea.INSTALLATION_MANAGEMENT, 3, "Asesorar a los mandos institucionales en la toma de decisiones sobre actualización, adquisición, instalación, reubicación y bajas de instalaciones, equipos, instrumentos y sistemas de instrumentos, destinados al uso médico en instituciones de prestación de servicios de salud o en empresas externas."),
    PERSONNEL_TRAINING(DomainArea.INSTALLATION_MANAGEMENT, 4, "Planificar y ejecutar programas y acciones para capacitar y promover buenas prácticas y uso seguro, por parte del personal de instalaciones, equipos, instrumentos y sistemas de instrumentos destinados a uso médico en instituciones de prestación de servicios de salud o en empresas externas."),
    COMPLIANCE_VERIFICATION(DomainArea.INSTALLATION_MANAGEMENT, 5, "Planificar, dirigir y realizar la generación y la verificación de cumplimiento de normativas, incluyendo peritajes técnicos, relacionadas con instalaciones, equipos, instrumentos, destinados a uso médico, en Organismos del Estado, en organismos internacionales, en ONGs ad hoc, en instituciones de prestación de servicios para la salud, o en empresas externas."),

    // AREA 3 - RDI Projects
    PROJECT_DESIGN_MANAGEMENT(DomainArea.RDI_PROJECTS, 1, "Diseñar, formular y gestionar proyectos de I+D+I en el campo de la Ingeniería Biomédica."),
    DIRECTOR_ACTIVITIES(DomainArea.RDI_PROJECTS, 2, "Desarrollar actividades específicas indicadas por el Director, en Proyectos I+D en el campo de la Medicina."),

    // AREA 4 - Service Management
    ESTABLISHMENT_MANAGEMENT(DomainArea.SERVICE_MANAGEMENT, 1, "Gestionar y dirigir establecimiento de salud.");

    private final DomainArea domainArea;
    private final Integer code;
    private final String displayValue;

    ProfessionalCompetency(DomainArea domainArea, Integer code, String displayValue) {
        this.domainArea = domainArea;
        this.code = code;
        this.displayValue = displayValue;
    }

    public DomainArea getDomainArea() { return domainArea; }
    public Integer getCode() { return code; }
    public String getDisplayValue() { return displayValue; }

    public static ProfessionalCompetency fromCode(Integer code) {
        for (ProfessionalCompetency competency : values()) {
            if (competency.code.equals(code)) return competency;
        }
        throw new IllegalArgumentException("Código de competencia no válido: " + code);
    }
}
