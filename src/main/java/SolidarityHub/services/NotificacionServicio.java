package SolidarityHub.services;

import SolidarityHub.events.TareaCreadaEvent;
import SolidarityHub.models.Notificacion;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import SolidarityHub.repository.NotificacionRepositorio;
import SolidarityHub.repository.TareaRepositorio;
import SolidarityHub.services.TareaServicio;
import SolidarityHub.services.UsuarioServicio;
import java.util.List;
import java.util.Optional;

@Service
public class NotificacionServicio {

    @Autowired
    private NotificacionRepositorio notificacionRepositorio;
    
    
    @Autowired
    private TareaServicio tareaServicio;
    
    @Autowired
    private UsuarioServicio usuarioServicio;

    public void crearNotificacion(Notificacion notificacion) {
        notificacionRepositorio.save(notificacion);
    }

    public List<Notificacion> findByVoluntarioAndEstado(Usuario usuario, Notificacion.EstadoNotificacion estado) {
        return notificacionRepositorio.findByVoluntarioAndEstado(usuario, estado);
    }
    
    /**
     * Método para responder a una asignación de tarea (aceptar o rechazar)
     * @param tareaId ID de la tarea
     * @param voluntarioId ID del voluntario
     * @param estado Estado de la respuesta (ACEPTADA o RECHAZADA)
     * @return true si se procesó correctamente, false en caso contrario
     */
    public boolean responderAsignacionTarea(Long tareaId, Long voluntarioId, Notificacion.EstadoNotificacion estado) {
        // Verificar que el estado sea válido (ACEPTADA o RECHAZADA)
        if (estado != Notificacion.EstadoNotificacion.ACEPTADA && estado != Notificacion.EstadoNotificacion.RECHAZADA) {
            return false;
        }
        
        // Obtener el usuario/voluntario
        Usuario usuario = usuarioServicio.obtenerUsuarioPorId(voluntarioId);
        if (usuario == null) {
            return false;
        }
        
        // Obtener la tarea
        Optional<Tarea> tareaOpt = tareaServicio.obtenerTareaPorId(tareaId);
        if (!tareaOpt.isPresent()) {
            return false;
        }
        Tarea tarea = tareaOpt.get();
        
        // Buscar la notificación pendiente para este usuario y esta tarea
        List<Notificacion> notificaciones = notificacionRepositorio.findByVoluntarioAndEstado(
                usuario, Notificacion.EstadoNotificacion.PENDIENTE);
        
        Notificacion notificacion = notificaciones.stream()
                .filter(n -> n.getTarea() != null && n.getTarea().getId().equals(tareaId))
                .findFirst()
                .orElse(null);
        
        if (notificacion == null) {
            return false;
        }
        
        // Actualizar el estado de la notificación
        notificacion.setEstado(estado);
        notificacionRepositorio.save(notificacion);
        
        // Si la notificación fue aceptada, asignar el voluntario a la tarea
        if (estado == Notificacion.EstadoNotificacion.ACEPTADA && usuario instanceof Voluntario) {
            Voluntario voluntario = (Voluntario) usuario;
            
            // Inicializar la lista de voluntarios si es necesario
            if (tarea.getVoluntariosAsignados() == null) {
                tarea.setVoluntariosAsignados(new java.util.ArrayList<>());
            }
            
            // Añadir el voluntario si no está ya asignado
            if (!tarea.getVoluntariosAsignados().contains(voluntario)) {
                tarea.getVoluntariosAsignados().add(voluntario);
                tareaServicio.actualizarTarea(tarea);
            }
        }
        
        return true;
    }
    
    /**
     * Método para eliminar una notificación
     * @param notificacionId ID de la notificación a eliminar
     */
    public void eliminarNotificacion(Long notificacionId) {
        notificacionRepositorio.deleteById(notificacionId);
    }
}