package SolidarityHub.events;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Voluntario;
import org.springframework.context.ApplicationEvent;

public class NuevaTareaAsignadaEvent extends ApplicationEvent {
    private final Tarea tarea;
    private final Voluntario voluntario;

    public NuevaTareaAsignadaEvent(Object source, Tarea tarea, Voluntario voluntario) {
        super(source);
        this.tarea = tarea;
        this.voluntario = voluntario;
    }

    public Tarea getTarea() {
        return tarea;
    }

    public Voluntario getVoluntario() {
        return voluntario;
    }
}