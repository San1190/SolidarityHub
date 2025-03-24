package SolidarityHub.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
@Entity
public class Necesidad {

    public enum TipoNecesidad {
        ALIMENTACION, SALUD, REFUGIO, ROPA, SERVICIO_LIMPIEZA, AYUDA_PSICOLOGICA
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
    private EstadoNecesidad estadoNecesidad;
    private Urgencia urgencia;
    private String ubicacion;
    private LocalDateTime fechaCreacion;
    




    public Necesidad() {}

    public Necesidad(TipoNecesidad tipoNecesidad, String descripcion,EstadoNecesidad estadoNecesidad, Urgencia urgencia, String ubicacion, LocalDateTime fechaCreacion) {
        this.tipoNecesidad = tipoNecesidad;
        this.descripcion = descripcion;
        this.estadoNecesidad = estadoNecesidad;
        this.urgencia = urgencia;
        this.ubicacion = ubicacion;
        this.fechaCreacion = fechaCreacion;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoNecesidad geTipoNecesidad() { return tipoNecesidad; }
    public void setTipoNecesidad(TipoNecesidad tipoNecesidad) { this.tipoNecesidad = tipoNecesidad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public EstadoNecesidad getEstadoNecesidad() { return estadoNecesidad; }
    public void setEstadoNecesidad(EstadoNecesidad estadoNecesidad) { this.estadoNecesidad = estadoNecesidad; }
    
    public Urgencia getUrgencia() { return urgencia; }
    public void setUrgencia(Urgencia urgencia) { this.urgencia = urgencia; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }


} 
