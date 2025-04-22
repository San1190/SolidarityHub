package SolidarityHub.events;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Voluntario;
import SolidarityHub.services.NotificacionServicio;
import SolidarityHub.services.TareaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Listener para eventos relacionados con tareas.
 * Implementa el patrón Observer para reaccionar a eventos del sistema.
 */
@Component
public class TareaEventListener {

    @Autowired
    private NotificacionServicio notificacionServicio;
    
    @Autowired
    private TareaServicio tareaServicio;
    
    /**
     * Escucha eventos de asignación de nuevas tareas y procesa las notificaciones
     * @param event Evento de nueva tarea asignada
     */
    @EventListener
    public void handleNuevaTareaAsignadaEvent(NuevaTareaAsignadaEvent event) {
        Tarea tarea = event.getTarea();
        Voluntario voluntario = event.getVoluntario();
        
        // La notificación ya se crea en NotificacionServicio.notificarAsignacionTarea
        // Este método puede extenderse para realizar acciones adicionales
        System.out.println("Evento recibido: Nueva tarea '" + tarea.getNombre() + "' asignada al voluntario " + voluntario.getNombre());
    }
    
    /**
     * Tarea programada que se ejecuta diariamente para enviar recordatorios
     * de tareas que comienzan hoy
     */
    @Scheduled(cron = "0 0 8 * * ?") // Ejecutar todos los días a las 8:00 AM
    public void enviarRecordatoriosTareasHoy() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();
        
        System.out.println("Enviando recordatorios para tareas que comienzan hoy: " + hoy);
        
        // Obtener tareas que comienzan hoy
        List<Tarea> tareasHoy = tareaServicio.obtenerTareasQueComienzan(ahora);
        
        // Enviar recordatorios a los voluntarios asignados
        for (Tarea tarea : tareasHoy) {
            if (tarea.getVoluntariosAsignados() != null && !tarea.getVoluntariosAsignados().isEmpty()) {
                for (Voluntario voluntario : tarea.getVoluntariosAsignados()) {
                    // Crear notificación de recordatorio
                    String titulo = "Recordatorio: Tarea comienza hoy";
                    String mensaje = String.format("La tarea '%s' comienza hoy a las %s. Localización: %s", 
                            tarea.getNombre(), 
                            tarea.getFechaInicio().format(DateTimeFormatter.ofPattern("HH:mm")),
                            tarea.getLocalizacion());
                    
                    notificacionServicio.crearNotificacion(titulo, mensaje, voluntario, tarea);
                }
            }
        }
    }
}