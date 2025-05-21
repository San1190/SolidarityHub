package SolidarityHub.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
@Entity
public class Necesidad {

    public enum TipoNecesidad {
        PRIMEROS_AUXILIOS, MEDICAMENTOS, ALIMENTACION, ALIMENTACION_BEBE, REFUGIO, ROPA, SERVICIO_LIMPIEZA,
         AYUDA_PSICOLOGICA, AYUDA_CARPINTERIA, AYUDA_ELECTRICIDAD, AYUDA_FONTANERIA, MATERIAL_HIGENE 
    }

    public enum Urgencia {
        BAJA, MEDIA, ALTA
    }

    public enum EstadoNecesidad {
        REGISTRADA, EN_PROCESO, FINALIZADA
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private TipoNecesidad tipoNecesidad;
    private String descripcion;
    
    @ManyToOne
    @JoinColumn(name = "afectado_id")
    private Afectado creador;


    
    private EstadoNecesidad estadoNecesidad;
    private Urgencia urgencia;
    private String ubicacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaInicio;

    public Necesidad() {}

    public Necesidad(TipoNecesidad tipoNecesidad, String descripcion,Afectado creador, EstadoNecesidad estadoNecesidad, Urgencia urgencia, String ubicacion, LocalDateTime fechaCreacion, LocalDateTime fechaInicio) {
        this.tipoNecesidad = tipoNecesidad;
        this.descripcion = descripcion;
        this.creador = creador;
        this.estadoNecesidad = estadoNecesidad;
        this.urgencia = urgencia;
        this.ubicacion = ubicacion;
        this.fechaCreacion = fechaCreacion;
        this.fechaInicio = fechaInicio;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoNecesidad getTipoNecesidad() { return tipoNecesidad; }
    public void setTipoNecesidad(TipoNecesidad tipoNecesidad) { this.tipoNecesidad = tipoNecesidad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Afectado getCreador() { return creador; }
    public void setCreador(Afectado creador) { this.creador = creador; }

    public EstadoNecesidad getEstadoNecesidad() { return estadoNecesidad; }
    public void setEstadoNecesidad(EstadoNecesidad estadoNecesidad) { this.estadoNecesidad = estadoNecesidad; }
    
    public Urgencia getUrgencia() { return urgencia; }
    public void setUrgencia(Urgencia urgencia) { this.urgencia = urgencia; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }


}
