package SolidarityHub.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AreaCritica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    
    // Coordenadas en formato GeoJSON para representar polígonos
    @Column(columnDefinition = "TEXT")
    private String coordenadas;
    
    // Nivel de criticidad del área
    public enum NivelCriticidad {
        BAJA, MEDIA, ALTA, EXTREMA
    }
    
    @Enumerated(EnumType.STRING)
    private NivelCriticidad nivelCriticidad;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id")
    private Usuario creador;
    
    // Constructor vacío requerido por JPA
    public AreaCritica() {}
    
    // Constructor con todos los campos
    public AreaCritica(String nombre, String descripcion, String coordenadas, 
                      NivelCriticidad nivelCriticidad, LocalDateTime fechaCreacion, 
                      LocalDateTime fechaActualizacion, Usuario creador) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.coordenadas = coordenadas;
        this.nivelCriticidad = nivelCriticidad;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.creador = creador;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCoordenadas() { return coordenadas; }
    public void setCoordenadas(String coordenadas) { this.coordenadas = coordenadas; }

    public NivelCriticidad getNivelCriticidad() { return nivelCriticidad; }
    public void setNivelCriticidad(NivelCriticidad nivelCriticidad) { this.nivelCriticidad = nivelCriticidad; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public Usuario getCreador() { return creador; }
    public void setCreador(Usuario creador) { this.creador = creador; }
}