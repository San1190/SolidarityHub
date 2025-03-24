package SolidarityHub.models;

public enum Habilidades {
    LIMPIEZA("Limpieza"),
    COCINA("Cocina"),
    COMPRA_ALIMENTOS("Compra de Alimentos"),
    DISTRIBUCION_ALIMENTOS("Distribución de Alimentos"),
    TRANSPORTE_ALIMENTOS("Transporte de Alimentos"),
    LAVANDERIA("Lavandería"),
    PSICOLOGIA("Psicología"),
    TERAPIA("Terapia"),
    CONSEJERIA("Consejería"),
    TRANSPORTE("Transporte");

    private final String nombre;

    Habilidades(String nombre) {
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

