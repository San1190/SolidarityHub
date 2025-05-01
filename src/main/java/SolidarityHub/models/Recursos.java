package SolidarityHub.models;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Entity
public class Recursos {

    public enum TipoRecurso {
        PRIMEROS_AUXILIOS, MEDICAMENTOS, ALIMENTACION, ALIMENTACION_BEBE, REFUGIO, ROPA, SERVICIO_LIMPIEZA,
        AYUDA_PSICOLOGICA, AYUDA_CARPINTERIA, AYUDA_ELECTRICIDAD, AYUDA_FONTANERIA, MATERIAL_HIGENE
    }

    public enum EstadoRecurso {
        DISPONIBLE, ASIGNADO, NO_DISPONIBLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private TipoRecurso tipoRecurso;
    private String descripcion;
    private int cantidad;

    @Enumerated(EnumType.STRING)
    private EstadoRecurso estado = EstadoRecurso.DISPONIBLE;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "afectados", "voluntariosAsignados", "creador", "recursosAsignados"})
    private Tarea tareaAsignada;
    public Recursos() {}

    public Recursos(TipoRecurso tipoRecurso, String descripcion) {
        this.tipoRecurso = tipoRecurso;
        this.descripcion = descripcion;
    }

    public Recursos(TipoRecurso tipoRecurso, String descripcion, int cantidad, EstadoRecurso estado) {
        this.tipoRecurso = tipoRecurso;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    public Long getId()  { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoRecurso getTipoRecurso() { return tipoRecurso; }
    public void setTipoRecurso(TipoRecurso tipoRecurso) { this.tipoRecurso = tipoRecurso; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    
    public EstadoRecurso getEstado() { return estado; }
    public void setEstado(EstadoRecurso estado) { this.estado = estado; }
    
    public Tarea getTareaAsignada() { return tareaAsignada; }
    public void setTareaAsignada(Tarea tareaAsignada) { this.tareaAsignada = tareaAsignada; }
}
