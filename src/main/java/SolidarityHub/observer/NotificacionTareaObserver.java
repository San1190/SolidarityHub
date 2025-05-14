package SolidarityHub.observer;

import SolidarityHub.models.Notificacion;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import SolidarityHub.services.NotificacionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementación concreta del observador que crea notificaciones cuando se generan tareas
 * desde necesidades.
 */
@Component
public class NotificacionTareaObserver implements TareaObserver {

    @Autowired
    private NotificacionServicio notificacionServicio;
    
    /**
     * Este método se ejecuta cuando una tarea es creada o actualizada.
     * Crea notificaciones para los usuarios relevantes.
     * 
     * @param tarea La tarea que ha sido creada o actualizada
     */
    @Override
    public void onTareaCreated(Tarea tarea) {
        // Verificar si la tarea tiene un tipo de necesidad asignado
        if (tarea.getTipo() != null) {
            // Crear notificación para todos los voluntarios disponibles
            crearNotificacionesParaVoluntarios(tarea);
        }
    }
    
    /**
     * Crea notificaciones para los voluntarios que podrían estar interesados en la tarea
     * 
     * @param tarea La tarea para la que se crearán notificaciones
     */
    private void crearNotificacionesParaVoluntarios(Tarea tarea) {
        // Aquí se implementaría la lógica para obtener los voluntarios relevantes
        // y crear notificaciones para ellos
        
        // Notificar a los suscriptores de la tarea
        List<Voluntario> suscriptores = tarea.getSuscriptores();
        if (suscriptores != null && !suscriptores.isEmpty()) {
            for (Voluntario voluntario : suscriptores) {
                Notificacion notificacion = new Notificacion();
                notificacion.setUsuario(voluntario);
                notificacion.setTarea(tarea);
                notificacion.setTitulo("Nueva tarea disponible: " + tarea.getNombre());
                notificacion.setMensaje("Se ha creado una nueva tarea para atender la necesidad de tipo: " + 
                                      tarea.getTipo().name() + ". Descripción: " + tarea.getDescripcion());
                notificacion.setEstado(Notificacion.EstadoNotificacion.PENDIENTE);
                
                notificacionServicio.crearNotificacion(notificacion);
            }
        }
    }
}