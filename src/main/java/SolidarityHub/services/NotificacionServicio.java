package SolidarityHub.services;

import SolidarityHub.events.NuevaTareaAsignadaEvent;
import SolidarityHub.models.Notificacion;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import SolidarityHub.repository.NotificacionRepositorio;
import SolidarityHub.repository.TareaRepositorio;
import java.util.List;
import java.util.Optional;
import SolidarityHub.services.NotificacionBroadcaster;

@Service
public class NotificacionServicio {

    @Autowired
    private NotificacionRepositorio notificacionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private TareaRepositorio tareaRepositorio;

    public Notificacion crearNotificacion(String titulo, String mensaje, Usuario usuario, Tarea tarea) {
        Notificacion notificacion = new Notificacion(titulo, mensaje, usuario, tarea);
        notificacion = notificacionRepository.save(notificacion);

        // Enviar notificación en tiempo real al usuario usando el broadcaster
        NotificacionBroadcaster.broadcast(usuario.getId(), "nueva_notificacion");

        return notificacion;
    }

    public List<Notificacion> obtenerNotificacionesNoLeidas(Usuario usuario) {
        return notificacionRepository.findAll().stream()
                .filter(n -> n.getUsuario().equals(usuario) && !n.isLeida())
                .sorted((n1, n2) -> n2.getFechaCreacion().compareTo(n1.getFechaCreacion()))
                .collect(java.util.stream.Collectors.toList());
    }

    public void marcarComoLeida(Long notificacionId) {
        Optional<Notificacion> notificacionOpt = notificacionRepository.findById(notificacionId);
        if (notificacionOpt.isPresent()) {
            Notificacion notificacion = notificacionOpt.get();
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
        }
    }

    /**
     * Notifica a un voluntario que se le ha asignado una nueva tarea
     * @param tarea La tarea asignada
     * @param voluntario El voluntario al que se le asigna la tarea
     * @return La notificación creada
     */
    public Notificacion notificarAsignacionTarea(Tarea tarea, Voluntario voluntario) {
        String titulo = "Nueva tarea disponible";
        String mensaje = String.format("Se te ha asignado la tarea '%s' que coincide con tu perfil. ¿Deseas aceptarla?", tarea.getNombre());
        
        // Publicar evento usando el patrón Observer
        eventPublisher.publishEvent(new NuevaTareaAsignadaEvent(this, tarea, voluntario));
        
        // Crear notificación en la base de datos
        Notificacion notificacion = crearNotificacion(titulo, mensaje, voluntario, tarea);
        
        // Enviar notificación en tiempo real usando el broadcaster
        NotificacionBroadcaster.broadcast(voluntario.getId(), "asignacion_tarea");
        
        return notificacion;
    }

    public void eliminarNotificacion(Long notificacionId) {
        notificacionRepository.deleteById(notificacionId);
    }
    
    /**
     * Método para confirmar o rechazar una tarea asignada a un voluntario
     * @param tareaId ID de la tarea
     * @param voluntarioId ID del voluntario
     * @param aceptada true si el voluntario acepta la tarea, false si la rechaza
     * @return true si la operación fue exitosa, false en caso contrario
     */
    public boolean responderAsignacionTarea(Long tareaId, Long voluntarioId, boolean aceptada) {
        Optional<Tarea> tareaOpt = tareaRepositorio.findById(tareaId);
        if (!tareaOpt.isPresent()) {
            return false;
        }
        
        Tarea tarea = tareaOpt.get();
        List<Voluntario> voluntariosAsignados = tarea.getVoluntariosAsignados();
        
        // Buscar el voluntario en la lista de asignados
        boolean voluntarioEncontrado = voluntariosAsignados.stream()
            .anyMatch(v -> v.getId().equals(voluntarioId));
            
        if (!voluntarioEncontrado) {
            return false;
        }
        
        if (!aceptada) {
            // Si rechaza, eliminar de la lista de voluntarios asignados
            voluntariosAsignados.removeIf(v -> v.getId().equals(voluntarioId));
            tarea.setVoluntariosAsignados(voluntariosAsignados);
            tareaRepositorio.save(tarea);
            
            // Notificar al creador de la tarea que un voluntario ha rechazado
            String titulo = "Voluntario rechazó tarea";
            String mensaje = String.format("Un voluntario ha rechazado la tarea '%s'", tarea.getNombre());
            crearNotificacion(titulo, mensaje, tarea.getCreador(), tarea);
        } else {
            // Si acepta, enviar confirmación al creador de la tarea
            String titulo = "Voluntario aceptó tarea";
            String mensaje = String.format("Un voluntario ha aceptado la tarea '%s'", tarea.getNombre());
            crearNotificacion(titulo, mensaje, tarea.getCreador(), tarea);
        }
        
        return true;
    }
}