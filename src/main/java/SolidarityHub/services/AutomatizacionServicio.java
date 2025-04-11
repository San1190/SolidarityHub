package SolidarityHub.services;

import SolidarityHub.models.Afectado;
import SolidarityHub.models.Necesidad;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Tarea.EstadoTarea;
import SolidarityHub.models.Usuario;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@Service
public class AutomatizacionServicio implements ApplicationListener<ContextRefreshedEvent> {

    private final TareaServicio tareaServicio;

    public AutomatizacionServicio(TareaServicio tareaServicio) {
        this.tareaServicio = tareaServicio;
    }

    /**
     * Este método se ejecuta cuando se inicia la aplicación.
     * Ya no necesitamos procesar necesidades aquí, ya que lo haremos a través de eventos.
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // La inicialización ahora se maneja en InicializacionConfig
    }
    
    /**
     * Este método escucha eventos de tipo Necesidad y crea tareas automáticamente.
     */
    @EventListener
    @Transactional
    public void handleNecesidadEvent(Necesidad necesidad) {
        crearTareaDesdeNecesidad(necesidad);
    }
    
    /**
     * Este método escucha eventos de tipo Usuario y crea tareas automáticamente si es necesario.
     * Si el usuario es un Afectado, se crearán tareas basadas en sus necesidades.
     */
    @EventListener
    @Transactional
    public void handleUsuarioEvent(Usuario usuario) {
        // Solo procesamos usuarios de tipo Afectado
        if (usuario instanceof Afectado) {
            Afectado afectado = (Afectado) usuario;
            
            // Si el afectado tiene necesidades asociadas, creamos tareas para cada una
            if (afectado.getNecesidades() != null && !afectado.getNecesidades().isEmpty()) {
                for (Necesidad necesidad : afectado.getNecesidades()) {
                    crearTareaDesdeNecesidad(necesidad);
                }
            } else {
                // Si el afectado no tiene necesidades específicas, podríamos crear una tarea genérica
                // basada en el registro de un nuevo afectado, si es necesario
                System.out.println("Nuevo afectado registrado: " + afectado.getNombre() + ". No tiene necesidades específicas.");
            }
        }
    }

    /**
     * Este método se llama cuando se guarda una nueva necesidad.
     * Crea automáticamente una tarea basada en la necesidad.
     */
    @Transactional
    public void crearTareaDesdeNecesidad(Necesidad necesidad) {
        // Verificar si la necesidad ya tiene una tarea asociada
        List<Tarea> tareasExistentes = tareaServicio.filtrarPorTipo(necesidad.getTipoNecesidad());
        
        boolean tareaExiste = false;
        for (Tarea tarea : tareasExistentes) {
            // Verificar si ya existe una tarea con descripción similar
            if (tarea.getDescripcion() != null && 
                tarea.getDescripcion().contains(necesidad.getDescripcion())) {
                tareaExiste = true;
                break;
            }
        }
        
        if (!tareaExiste) {
            // Crear una nueva tarea basada en la necesidad
            Tarea nuevaTarea = new Tarea();
            nuevaTarea.setNombre("Tarea para " + necesidad.getTipoNecesidad().name());
            nuevaTarea.setDescripcion("Atender necesidad: " + necesidad.getDescripcion());
            nuevaTarea.setTipo(necesidad.getTipoNecesidad());
            nuevaTarea.setLocalizacion(necesidad.getUbicacion());
            nuevaTarea.setNumeroVoluntariosNecesarios(1); // Por defecto, se necesita al menos un voluntario
            
            // Establecer fechas por defecto (desde ahora hasta 7 días después)
            LocalDateTime ahora = LocalDateTime.now();
            nuevaTarea.setFechaInicio(ahora);
            nuevaTarea.setFechaFin(ahora.plusDays(7));
            
            // Establecer estado inicial
            nuevaTarea.setEstado(EstadoTarea.PREPARADA);
            
            // Inicializar listas vacías
            nuevaTarea.setAfectados(new ArrayList<>());
            nuevaTarea.setVoluntariosAsignados(new ArrayList<>());
            
            // Guardar la tarea
            tareaServicio.guardarTarea(nuevaTarea);
        }
    }
}