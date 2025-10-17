package edu.utec.planificador.enumeration;

public enum SustainableDevelopmentGoal implements DisplayableEnum {
    
    SDG_1("Poner fin a la pobreza en todas sus formas en todo el mundo."),
    SDG_2("Poner fin al hambre, lograr la seguridad."),
    SDG_3("Garantizar una vida sana y promover el bienestar para todos en todas las edades."),
    SDG_4("Garantizar una educación inclusiva, equitativa y de calidad y promover oportunidades de aprendizaje durante toda la vida para todos."),
    SDG_5("Lograr la igualdad entre los géneros y empoderar a todas las mujeres y las niñas."),
    SDG_6("Garantizar la disponibilidad de agua y su gestión sostenible y el saneamiento para todos."),
    SDG_7("Garantizar el acceso a una energía asequible, segura, sostenible y moderna para todos."),
    SDG_8("Promover el crecimiento económico sostenido, inclusivo y sostenible, el empleo pleno y productivo y el trabajo decente para todos."),
    SDG_9("Construir infraestructuras resilientes, promover la industrialización inclusiva y sostenible y fomentar la innovación."),
    SDG_10("Reducir la desigualdad en y entre los países."),
    SDG_11("Lograr que las ciudades y los asentamientos humanos sean inclusivos, seguros, resilientes y sostenibles."),
    SDG_12("Garantizar modalidades de consumo y producción sostenibles."),
    SDG_13("Adoptar medidas urgentes para combatir el cambio climático y sus efectos."),
    SDG_14("Conservar y utilizar en forma sostenible los océanos, los mares y los recursos marinos para el desarrollo sostenible."),
    SDG_15("Proteger, restablecer y promover el uso sostenible de los ecosistemas terrestres, gestionar los bosques de forma sostenible, luchar contra la desertificación, detener e invertir la degradación de las tierras y poner freno a la pérdida de la diversidad biológica."),
    SDG_16("Promover sociedades pacíficas e inclusivas para el desarrollo sostenible, facilitar el acceso a la justicia para todos y crear instituciones eficaces, responsables e inclusivas a todos los niveles."),
    SDG_17("Fortalecer los medios de ejecución y revitalizar la Alianza Mundial para el Desarrollo Sostenible.");

    private final String displayValue;

    SustainableDevelopmentGoal(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
