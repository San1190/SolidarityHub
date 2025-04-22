package SolidarityHub.services;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea.EstadoTarea;
import SolidarityHub.models.Voluntario;
import SolidarityHub.repository.TareaRepositorio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class TareaServicio {
    
    private final TareaRepositorio tareaRepositorio;

    public TareaServicio(TareaRepositorio tareaRepositorio) {
        this.tareaRepositorio = tareaRepositorio;
    }

    // Método para listar todas las tareas
    public List<Tarea> listarTareas() {
        return tareaRepositorio.findAll();
    }

    // Método para obtener una tarea por su ID
    public Optional<Tarea> obtenerTareaPorId(Long id) {
        return tareaRepositorio.findById(id);
    }

    // Método para guardar una tarea
    public Tarea guardarTarea(Tarea tarea) {
        return tareaRepositorio.save(tarea);
    }

    // Método para actualizar una tarea
    public Tarea actualizarTarea(Tarea tarea) {
        return tareaRepositorio.save(tarea);
    }

    // Método para eliminar una tarea
    public void eliminarTarea(Long id) {
        tareaRepositorio.deleteById(id);
    }
    
    // Método para filtrar tareas por estado
    public List<Tarea> filtrarPorEstado(EstadoTarea estado) {
        return tareaRepositorio.findByEstado(estado);
    }
    
    // Método para filtrar tareas por tipo
    public List<Tarea> filtrarPorTipo(TipoNecesidad tipo) {
        return tareaRepositorio.findByTipo(tipo);
    }
    
    // Método para filtrar tareas por estado y tipo
    public List<Tarea> filtrarPorEstadoYTipo(EstadoTarea estado, TipoNecesidad tipo) {
        return tareaRepositorio.findByEstadoAndTipo(estado, tipo);
    }
    
    /**
     * Obtiene las tareas pendientes de confirmación para un voluntario.
     * Estas son tareas donde el voluntario está asignado pero aún no ha confirmado su participación.
     * 
     * @param voluntario El voluntario para el que se buscan las tareas pendientes
     * @return Lista de tareas pendientes de confirmación
     */
    public List<Tarea> obtenerTareasPendientesConfirmacion(Voluntario voluntario) {
        // Usar una consulta personalizada en el repositorio que cargue explícitamente los voluntarios asignados
        List<Tarea> todasLasTareas = tareaRepositorio.findAllWithVoluntariosAsignados();
        
        return todasLasTareas.stream()
                .filter(tarea -> tarea.getVoluntariosAsignados() != null && 
                                 tarea.getVoluntariosAsignados().contains(voluntario) &&
                                 tarea.getEstado() == EstadoTarea.PREPARADA)
                .collect(Collectors.toList());
    }
    
    /**
     * Cambia el estado de una tarea a EN_CURSO cuando todos los voluntarios necesarios
     * han aceptado participar en ella.
     * 
     * @param tareaId ID de la tarea a actualizar
     * @return true si la tarea se actualizó correctamente, false en caso contrario
     */
    public boolean iniciarTareaSiCompleta(Long tareaId) {
        Optional<Tarea> tareaOpt = tareaRepositorio.findById(tareaId);
        if (!tareaOpt.isPresent()) {
            return false;
        }
        
        Tarea tarea = tareaOpt.get();
        
        // Verificar si la tarea ya tiene todos los voluntarios necesarios
        if (tarea.getEstado() == EstadoTarea.PREPARADA && 
            tarea.getVoluntariosAsignados() != null &&
            tarea.getVoluntariosAsignados().size() >= tarea.getNumeroVoluntariosNecesarios()) {
            
            // Cambiar estado a EN_CURSO
            tarea.setEstado(EstadoTarea.EN_CURSO);
            tareaRepositorio.save(tarea);
            return true;
        }
        
        return false;
    }
    
    /**
     * Obtiene las tareas aceptadas por un voluntario.
     * 
     * @param voluntario El voluntario para el que se buscan las tareas aceptadas
     * @return Lista de tareas aceptadas
     */
    public List<Tarea> obtenerTareasAceptadas(Voluntario voluntario) {
        // Usar una consulta personalizada en el repositorio que cargue explícitamente los voluntarios asignados
        List<Tarea> todasLasTareas = tareaRepositorio.findAllWithVoluntariosAsignados();
        
        return todasLasTareas.stream()
                .filter(tarea -> tarea.getVoluntariosAsignados() != null && 
                                 tarea.getVoluntariosAsignados().contains(voluntario) &&
                                 (tarea.getEstado() == EstadoTarea.EN_CURSO || 
                                  tarea.getEstado() == EstadoTarea.FINALIZADA))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene las tareas que comienzan en la fecha especificada.
     * Útil para enviar recordatorios a los voluntarios.
     * 
     * @param fecha La fecha para la que se buscan las tareas
     * @return Lista de tareas que comienzan en la fecha especificada
     */
    public List<Tarea> obtenerTareasQueComienzan(LocalDateTime fecha) {
        LocalDateTime inicioDia = fecha.toLocalDate().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);
        
        return tareaRepositorio.findAll().stream()
                .filter(tarea -> tarea.getFechaInicio().isAfter(inicioDia) && 
                                 tarea.getFechaInicio().isBefore(finDia))
                .collect(Collectors.toList());
    }
}