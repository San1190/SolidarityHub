package SolidarityHub.models;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import SolidarityHub.models.Necesidad.TipoNecesidad;

@Entity
public class Tarea implements Observado {

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
    private double latitud;
    private double longitud;
    private int numeroVoluntariosNecesarios;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    
    // Punto de encuentro para la tarea
    private String puntoEncuentro;
    
    // Información de turnos en lugar de horas
    private String turno; // Ejemplo: "Mañana", "Tarde", "Noche"
    
    @Enumerated(EnumType.STRING)
    private EstadoTarea estado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario creador;
    
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
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tareasAsignadas"})
    private List<Voluntario> voluntariosAsignados;
    
    @OneToMany(mappedBy = "tareaAsignada", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"tareaAsignada", "hibernateLazyInitializer", "handler"})
    private List<Recursos> recursosAsignados;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "tarea_habilidades_requeridas",
        joinColumns = @JoinColumn(name = "tarea_id")
    )
    @Enumerated(EnumType.STRING) // Guarda el nombre del enum, como "COCINA", "MEDICINA", etc.
    @Column(name = "habilidad")
    private List<Habilidad> habilidadesRequeridas;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tarea_suscriptores",
            joinColumns = @JoinColumn(name = "tarea_id"),
            inverseJoinColumns = @JoinColumn(name = "voluntario_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tareasAsignadas"})
    private List<Voluntario> suscriptores;

    // Constructor vacío requerido por JPA
    public Tarea() {}

    // Constructor con todos los campos
    public Tarea(String nombre, String descripcion, Necesidad.TipoNecesidad tipo, 
                String localizacion, int numeroVoluntariosNecesarios, 
                LocalDateTime fechaInicio, LocalDateTime fechaFin, String puntoEncuentro, String turno,
                EstadoTarea estado, Usuario creador, List<Afectado> afectados, 
                List<Voluntario> voluntariosAsignados, List<Habilidad> habilidadesRequeridas, List<Voluntario> suscriptores, double latitud, double longitud) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.localizacion = localizacion;
        this.numeroVoluntariosNecesarios = numeroVoluntariosNecesarios;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.puntoEncuentro = puntoEncuentro;
        this.turno = turno;
        this.estado = estado;
        this.creador = creador;
        this.afectados = afectados;
        this.voluntariosAsignados = voluntariosAsignados;
        this.habilidadesRequeridas = habilidadesRequeridas;
        this.suscriptores = suscriptores;
        this.latitud = latitud;
        this.longitud = longitud;
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
    
    public String getPuntoEncuentro() { return puntoEncuentro; }
    public void setPuntoEncuentro(String puntoEncuentro) { this.puntoEncuentro = puntoEncuentro; }
    
    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
    
    public EstadoTarea getEstado() { return estado; }
    public void setEstado(EstadoTarea estado) { this.estado = estado; }
    
    public Usuario getCreador() { return creador; }
    public void setCreador(Usuario creador) { this.creador = creador; }
    
    public List<Afectado> getAfectados() { return afectados; }
    public void setAfectados(List<Afectado> afectados) { this.afectados = afectados; }
    
    public List<Voluntario> getVoluntariosAsignados() { return voluntariosAsignados; }
    public void setVoluntariosAsignados(List<Voluntario> voluntariosAsignados) { 
        this.voluntariosAsignados = voluntariosAsignados; 
    }
    
    public List<Recursos> getRecursosAsignados() { return recursosAsignados; }
    public void setRecursosAsignados(List<Recursos> recursosAsignados) { 
        this.recursosAsignados = recursosAsignados; 
    }
    
    // Getter y Setter para habilidadesRequeridas
    public List<Habilidad> getHabilidadesRequeridas() { return habilidadesRequeridas; }
    public void setHabilidadesRequeridas(List<Habilidad> habilidadesRequeridas) {
        this.habilidadesRequeridas = habilidadesRequeridas;
    }

    public List<Voluntario> getSuscriptores() { return suscriptores; }
    public void setSuscriptores(List<Voluntario> suscriptores) {
        this.suscriptores = suscriptores;
    }

    public TipoNecesidad getTipoNecesidad() {
        return tipo;
    }

    public void setTipoNecesidad(TipoNecesidad tipo) {
        this.tipo = tipo;
    }

    public EstadoTarea getEstadoTarea() {
        return estado;
    }

    public void setEstadoTarea(EstadoTarea estado) {
        this.estado = estado;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public void suscribirVoluntario(Voluntario voluntario) {
        suscriptores.add(voluntario);
    }
    public void dessuscribirVoluntario(Voluntario voluntario) {
        suscriptores.remove(voluntario);
    }
    
    
}
