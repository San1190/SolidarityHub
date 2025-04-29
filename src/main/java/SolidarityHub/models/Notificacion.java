package SolidarityHub.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public enum EstadoNotificacion {
        PENDIENTE, ACEPTADA, RECHAZADA
    }

    private String titulo;
    private String mensaje;
    private LocalDateTime fechaCreacion;
    private EstadoNotificacion estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id")
    private Tarea tarea;

    // Constructor vacío requerido por JPA
    public Notificacion() {
        this.fechaCreacion = LocalDateTime.now();
        estado = EstadoNotificacion.PENDIENTE;
    }

    // Constructor con parámetros
    public Notificacion(String titulo, String mensaje, Usuario usuario, Tarea tarea, EstadoNotificacion estado) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.usuario = usuario;
        this.tarea = tarea;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Tarea getTarea() {
        return tarea;
    }

    public void setTarea(Tarea tarea) {
        this.tarea = tarea;
    }

    public EstadoNotificacion getEstado() {return estado;}

    public void setEstado(EstadoNotificacion estado) {this.estado = estado;}
}
