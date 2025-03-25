package SolidarityHub.models;

public enum Habilidad {
    LIMPIEZA("Limpieza"),
    PRIMEROS_AUXILIOS("Primeros Auxilios"),
    COCINA("Cocina"),
    ELECTICISTA("Electricista"),
    FONTANERIA("Fontanería"),
    CARPINTERIA("Carpintería"),
    LAVANDERIA("Lavandería"),
    TRANSPORTE_ALIMENTOS("Transporte de Alimentos"),
    AYUDA_PSICOLOGICA("Ayuda Psicológica"),
    TRANSPORTE_PERSONAS("Transporte de Personas"),;

    private final String nombre;

    Habilidad(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

