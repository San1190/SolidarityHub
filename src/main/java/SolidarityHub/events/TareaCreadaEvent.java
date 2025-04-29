package SolidarityHub.events;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Voluntario;
import org.springframework.context.ApplicationEvent;

public class TareaCreadaEvent extends ApplicationEvent {
    private final Tarea tarea;

    public TareaCreadaEvent(Object source, Tarea tarea) {
        super(source);
        this.tarea = tarea;
    }

    public Tarea getTarea() {
        return tarea;
    }
}