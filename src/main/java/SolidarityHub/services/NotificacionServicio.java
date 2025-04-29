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
import java.util.List;
import java.util.Optional;

@Service
public class NotificacionServicio {

    @Autowired
    private NotificacionRepositorio notificacionRepositorio;

    public void crearNotificacion(Notificacion notificacion) {
        notificacionRepositorio.save(notificacion);
    }

    public void actualizarEstado(Notificacion notificacion, Notificacion.EstadoNotificacion estado) {
        notificacion.setEstado(estado);
        notificacionRepositorio.save(notificacion);
    }

    public List<Notificacion> findByVoluntarioAndEstado(Usuario usuario, Notificacion.EstadoNotificacion estado) {
        return notificacionRepositorio.findByVoluntarioAndEstado(usuario, estado);
    }
}