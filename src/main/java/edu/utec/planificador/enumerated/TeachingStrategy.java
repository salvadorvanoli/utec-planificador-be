package edu.utec.planificador.enumerated;

public enum TeachingStrategy implements DisplayableEnum {
    
    LECTURE("Clase magistral"),
    DEBATE("Debate"),
    TEAMWORK("Trabajo en equipo"),
    FIELD_ACTIVITY("Actividad de campo"),
    PRACTICAL_ACTIVITY("Actividad práctica"),
    LABORATORY_PRACTICES("Prácticas de laboratorio"),
    TESTS("Pruebas"),
    RESEARCH_ACTIVITIES("Actividades de investigación"),
    FLIPPED_CLASSROOM("Aula invertida"),
    DISCUSSION("Discusión"),
    SMALL_GROUP_TUTORIALS("Tutorías en grupos pequeños"),
    PROJECTS("Proyectos"),
    CASE_STUDY("Caso de estudio"),
    OTHER("Otros"),
    NOT_DETERMINED("Sin determinar");

    private final String displayValue;

    TeachingStrategy(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
