package SolidarityHub.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Recursos {

    public enum TipoRecurso {
        PRIMEROS_AUXILIOS, MEDICAMENTOS, ALIMENTACION, ALIMENTACION_BEBE, REFUGIO, ROPA, SERVICIO_LIMPIEZA,
        AYUDA_PSICOLOGICA, AYUDA_CARPINTERIA, AYUDA_ELECTRICIDAD, AYUDA_FONTANERIA, MATERIAL_HIGENE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private TipoRecurso tipoRecurso;
    private String descripcion;

    public Recursos() {}

    public Recursos(TipoRecurso tipoRecurso, String descripcion) {
        this.tipoRecurso = tipoRecurso;
        this.descripcion = descripcion;
    }

    public Long getId()  { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoRecurso getTipoRecurso() { return tipoRecurso; }
    public void setTipoRecurso(TipoRecurso tipoRecurso) { this.tipoRecurso = tipoRecurso; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
}
