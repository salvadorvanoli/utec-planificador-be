package edu.utec.planificador.enumeration;

public enum LearningResource implements DisplayableEnum {
    
    EXHIBITION("Exhibición"),
    BOOK_DOCUMENT("Libro/documento"),
    DEMONSTRATION("Demostración"),
    WHITEBOARD("Pizarrón"),
    ONLINE_COLLABORATION_TOOL("Herramienta de colaboración en línea"),
    ONLINE_LECTURE("Charla en línea"),
    ONLINE_FORUM("Foro en línea"),
    ONLINE_EVALUATION("Evaluación en línea"),
    GAME("Juego"),
    SURVEY("Encuesta"),
    VIDEO("Video"),
    INFOGRAPHIC("Infografía"),
    WEBPAGE("Página web"),
    OTHER("Otros"),
    NOT_DETERMINED("Sin determinar");

    private final String displayValue;

    LearningResource(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
