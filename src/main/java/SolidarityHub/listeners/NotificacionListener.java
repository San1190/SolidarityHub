package SolidarityHub.listeners;

import SolidarityHub.events.NuevaTareaAsignadaEvent;
import SolidarityHub.services.NotificacionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacionListener {

    @Autowired
    private NotificacionServicio notificacionServicio;

    @EventListener
    public void manejarNuevaTarea(NuevaTareaAsignadaEvent event) {
        // Enviar notificación al voluntario sobre la tarea asignada
        notificacionServicio.notificarAsignacionTarea(event.getTarea(), event.getVoluntario());
    }
}