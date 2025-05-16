package SolidarityHub.models;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

/**
 * Modelo para representar una zona de encuentro para tareas.
 * Una zona de encuentro es un polígono cerrado definido por una serie de coordenadas.
 */
@Entity
public class ZonaEncuentro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    
    // Coordenadas de la zona en formato "lat1,lng1;lat2,lng2;..."
    @Column(length = 5000)
    private String coordenadas;
    
    // Fecha de creación
    private LocalDateTime fechaCreacion;
    
    // Tarea asociada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id")
    private Tarea tarea;
    
    // Color del borde
    private String colorBorde;
    
    // Color del relleno
    private String colorRelleno;
    
    // Constructor vacío requerido por JPA
    public ZonaEncuentro() {
        this.fechaCreacion = LocalDateTime.now();
    }
    
    // Constructor completo
    public ZonaEncuentro(String nombre, String descripcion, String coordenadas, Tarea tarea,
                         String colorBorde, String colorRelleno) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.coordenadas = coordenadas;
        this.tarea = tarea;
        this.colorBorde = colorBorde;
        this.colorRelleno = colorRelleno;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(String coordenadas) {
        this.coordenadas = coordenadas;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Tarea getTarea() {
        return tarea;
    }

    public void setTarea(Tarea tarea) {
        this.tarea = tarea;
    }

    public String getColorBorde() {
        return colorBorde;
    }

    public void setColorBorde(String colorBorde) {
        this.colorBorde = colorBorde;
    }

    public String getColorRelleno() {
        return colorRelleno;
    }

    public void setColorRelleno(String colorRelleno) {
        this.colorRelleno = colorRelleno;
    }
    
    /**
     * Convierte las coordenadas de String a una lista de pares latitud/longitud
     * @return Lista de arrays de doubles [latitud, longitud]
     */
    @Transient
    public List<double[]> getCoordenadaComoLista() {
        List<double[]> resultado = new ArrayList<>();
        if (coordenadas == null || coordenadas.isEmpty()) {
            return resultado;
        }
        
        String[] puntos = coordenadas.split(";");
        for (String punto : puntos) {
            String[] coords = punto.split(",");
            if (coords.length == 2) {
                try {
                    double lat = Double.parseDouble(coords[0]);
                    double lng = Double.parseDouble(coords[1]);
                    resultado.add(new double[]{lat, lng});
                } catch (NumberFormatException e) {
                    // Ignorar punto inválido
                }
            }
        }
        
        return resultado;
    }

    /**
     * Obtiene de forma segura el nombre de la tarea asociada
     * @return Nombre de la tarea o un texto por defecto si no hay tarea
     */
    @Transient
    public String getNombreTareaSeguro() {
        if (tarea == null) {
            return "(Sin tarea asociada)";
        }
        
        try {
            String nombre = tarea.getNombre();
            return nombre != null ? nombre : "(Sin nombre)";
        } catch (Exception e) {
            // Error de inicialización de proxy - no session
            return "(Tarea no disponible)";
        }
    }
    
    /**
     * Obtiene de forma segura el ID de la tarea asociada
     * @return ID de la tarea o null si no hay tarea
     */
    @Transient
    public Long getIdTareaSeguro() {
        if (tarea == null) {
            return null;
        }
        
        try {
            return tarea.getId();
        } catch (Exception e) {
            // Error de inicialización de proxy - no session
            return null;
        }
    }
} 