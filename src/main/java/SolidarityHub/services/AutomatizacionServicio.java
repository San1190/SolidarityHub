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
     * @param necesidad La necesidad que se ha creado o actualizado
     */
    @EventListener(Necesidad.class)
    @Transactional
    public void handleNecesidadEvent(Necesidad necesidad) {
        try {
            if (necesidad == null) {
                System.err.println("Error: Se recibió un evento de Necesidad con valor nulo");
                return;
            }
            
            if (necesidad.getDescripcion() == null) {
                System.err.println("Error: La necesidad recibida no tiene descripción");
                return;
            }
            
            System.out.println("Evento de Necesidad recibido: " + necesidad.getDescripcion());
            crearTareaDesdeNecesidad(necesidad);
        } catch (Exception e) {
            System.err.println("Error al procesar evento de Necesidad: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Este método escucha eventos de tipo Usuario y crea tareas automáticamente si es necesario.
     * Si el usuario es un Afectado, se crearán tareas basadas en sus necesidades.
     * @param usuario El usuario que se ha creado o actualizado
     */
    @EventListener(Usuario.class)
    @Transactional
    public void handleUsuarioEvent(Usuario usuario) {
        try {
            if (usuario == null) {
                System.err.println("Error: Se recibió un evento de Usuario con valor nulo");
                return;
            }
            
            System.out.println("Evento de Usuario recibido: " + usuario.getNombre());
            
            // Solo procesamos usuarios de tipo Afectado
            if (usuario instanceof Afectado) {
                Afectado afectado = (Afectado) usuario;
                
                // Si el afectado tiene necesidades asociadas, creamos tareas para cada una
                if (afectado.getNecesidades() != null && !afectado.getNecesidades().isEmpty()) {
                    System.out.println("Procesando " + afectado.getNecesidades().size() + " necesidades para el afectado: " + afectado.getNombre());
                    for (Necesidad necesidad : afectado.getNecesidades()) {
                        if (necesidad != null) {
                            crearTareaDesdeNecesidad(necesidad);
                        } else {
                            System.err.println("Error: Se encontró una necesidad nula en la lista de necesidades del afectado");
                        }
                    }
                } else {
                    // Si el afectado no tiene necesidades específicas, podríamos crear una tarea genérica
                    // basada en el registro de un nuevo afectado, si es necesario
                    System.out.println("Nuevo afectado registrado: " + afectado.getNombre() + ". No tiene necesidades específicas.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al procesar evento de Usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Este método se llama cuando se guarda una nueva necesidad.
     * Crea automáticamente una tarea basada en la necesidad.
     * @param necesidad La necesidad para la que se creará una tarea
     */
    @Transactional
    public void crearTareaDesdeNecesidad(Necesidad necesidad) {
        try {
            // Validar que la necesidad tenga los datos necesarios
            if (necesidad == null || necesidad.getDescripcion() == null || necesidad.getTipoNecesidad() == null) {
                System.out.println("Error: La necesidad no tiene los datos requeridos para crear una tarea");
                return;
            }
            
            System.out.println("Creando tarea para necesidad: " + necesidad.getDescripcion());
            
            // Verificar si la necesidad ya tiene una tarea asociada
            List<Tarea> tareasExistentes = tareaServicio.filtrarPorTipo(necesidad.getTipoNecesidad());
            System.out.println("Tareas existentes del mismo tipo: " + tareasExistentes.size());
            
            boolean tareaExiste = false;
            for (Tarea tarea : tareasExistentes) {
                // Verificar si ya existe una tarea con descripción similar
                if (tarea.getDescripcion() != null && 
                    tarea.getDescripcion().contains(necesidad.getDescripcion())) {
                    tareaExiste = true;
                    System.out.println("Ya existe una tarea similar: " + tarea.getDescripcion());
                    break;
                }
            }
            
            if (!tareaExiste) {
                System.out.println("Creando nueva tarea para la necesidad...");
                // Crear una nueva tarea basada en la necesidad
                Tarea nuevaTarea = new Tarea();
                nuevaTarea.setNombre("Tarea para " + necesidad.getTipoNecesidad().name());
                nuevaTarea.setDescripcion("Atender necesidad: " + necesidad.getDescripcion());
                nuevaTarea.setTipo(necesidad.getTipoNecesidad());
                nuevaTarea.setLocalizacion(necesidad.getUbicacion() != null ? necesidad.getUbicacion() : "Sin ubicación especificada");
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
                Tarea tareaGuardada = tareaServicio.guardarTarea(nuevaTarea);
                System.out.println("Tarea creada con éxito. ID: " + tareaGuardada.getId());
            } else {
                System.out.println("No se creó tarea porque ya existe una similar.");
            }
        } catch (Exception e) {
            System.err.println("Error al crear tarea desde necesidad: " + e.getMessage());
            e.printStackTrace();
        }
    }
}