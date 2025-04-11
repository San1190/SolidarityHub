package SolidarityHub.models;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Tarea {

    public enum EstadoTarea {
        PREPARADA, EN_CURSO, FINALIZADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    private Necesidad.TipoNecesidad tipo;
    
    private String localizacion;
    private int numeroVoluntariosNecesarios;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    
    @Enumerated(EnumType.STRING)
    private EstadoTarea estado;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tarea_afectados",
        joinColumns = @JoinColumn(name = "tarea_id"),
        inverseJoinColumns = @JoinColumn(name = "afectado_id")
    )
    private List<Afectado> afectados;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tarea_voluntarios",
        joinColumns = @JoinColumn(name = "tarea_id"),
        inverseJoinColumns = @JoinColumn(name = "voluntario_id")
    )
    private List<Voluntario> voluntariosAsignados;
    
    // Constructor vacío requerido por JPA
    public Tarea() {}
    
    // Constructor con todos los campos
    public Tarea(String nombre, String descripcion, Necesidad.TipoNecesidad tipo, 
                String localizacion, int numeroVoluntariosNecesarios, 
                LocalDateTime fechaInicio, LocalDateTime fechaFin, 
                EstadoTarea estado, List<Afectado> afectados, 
                List<Voluntario> voluntariosAsignados) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.localizacion = localizacion;
        this.numeroVoluntariosNecesarios = numeroVoluntariosNecesarios;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.afectados = afectados;
        this.voluntariosAsignados = voluntariosAsignados;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public Necesidad.TipoNecesidad getTipo() { return tipo; }
    public void setTipo(Necesidad.TipoNecesidad tipo) { this.tipo = tipo; }
    
    public String getLocalizacion() { return localizacion; }
    public void setLocalizacion(String localizacion) { this.localizacion = localizacion; }
    
    public int getNumeroVoluntariosNecesarios() { return numeroVoluntariosNecesarios; }
    public void setNumeroVoluntariosNecesarios(int numeroVoluntariosNecesarios) { 
        this.numeroVoluntariosNecesarios = numeroVoluntariosNecesarios; 
    }
    
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    
    public EstadoTarea getEstado() { return estado; }
    public void setEstado(EstadoTarea estado) { this.estado = estado; }
    
    public List<Afectado> getAfectados() { return afectados; }
    public void setAfectados(List<Afectado> afectados) { this.afectados = afectados; }
    
    public List<Voluntario> getVoluntariosAsignados() { return voluntariosAsignados; }
    public void setVoluntariosAsignados(List<Voluntario> voluntariosAsignados) { 
        this.voluntariosAsignados = voluntariosAsignados; 
    }
}