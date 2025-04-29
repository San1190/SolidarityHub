package SolidarityHub.listeners;

import SolidarityHub.events.TareaCreadaEvent;
import SolidarityHub.models.Habilidad;
import SolidarityHub.models.Notificacion;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Voluntario;
import SolidarityHub.repository.UsuarioRepositorio;
import SolidarityHub.services.NotificacionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificacionListener {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private NotificacionServicio notificacionServicio;

    @EventListener
    public void handleTareaCreada(TareaCreadaEvent event) {
        Tarea tarea = event.getTarea();
        List<Habilidad> habilidadesRequeridas = tarea.getHabilidadesRequeridas();

        List<Voluntario> voluntarios = usuarioRepositorio.findByHabilidadesIn(habilidadesRequeridas);

        voluntarios.forEach(voluntario -> {
            String mensaje = "Se ha creado una nueva tarea que coincide con tus habilidades: " + 
                           tarea.getNombre() + ". " +
                           "Puedes aceptar o rechazar esta tarea.";
                
            notificacionServicio.crearNotificacion(
                    new Notificacion("Nueva tarea disponible", mensaje, voluntario, tarea, Notificacion.EstadoNotificacion.PENDIENTE)
            );
        });
    }
}