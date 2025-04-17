package SolidarityHub.services;

import SolidarityHub.models.Notificacion;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import org.springframework.beans.factory.annotation.Autowired;
// Add dependency in pom.xml:

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import SolidarityHub.repository.NotificacionRepositorio;
import java.util.List;
import java.util.Optional;

@Service
public class NotificacionServicio {

    @Autowired
    private NotificacionRepositorio  notificacionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Notificacion crearNotificacion(String titulo, String mensaje, Usuario usuario, Tarea tarea) {
        Notificacion notificacion = new Notificacion(titulo, mensaje, usuario, tarea);
        notificacion = notificacionRepository.save(notificacion);

        // Enviar notificación en tiempo real al usuario
        messagingTemplate.convertAndSendToUser(
            usuario.getId().toString(),
            "/queue/notifications",
            notificacion
        );

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

    public void notificarAsignacionTarea(Tarea tarea, Voluntario voluntario) {
        String titulo = "Nueva tarea disponible";
        String mensaje = String.format("Se te ha asignado la tarea '%s' que coincide con tu perfil. ¿Deseas aceptarla?", tarea.getNombre());
        crearNotificacion(titulo, mensaje, voluntario, tarea);
    }

    public void eliminarNotificacion(Long notificacionId) {
        notificacionRepository.deleteById(notificacionId);
    }
}