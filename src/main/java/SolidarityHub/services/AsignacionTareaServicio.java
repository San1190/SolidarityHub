package SolidarityHub.services;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Voluntario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import SolidarityHub.repository.UsuarioRepositorio;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsignacionTareaServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;


    @Autowired
    private NotificacionServicio notificacionServicio;
    
    @Autowired
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private static final double EARTH_RADIUS = 6371; // Radio de la Tierra en kilómetros

    public void asignarVoluntariosAutomaticamente(Tarea tarea, double radioMaximo) {
        // Obtener todos los voluntarios disponibles
        List<Voluntario> voluntarios = usuarioRepositorio.findAll().stream()
        .filter(u -> u instanceof Voluntario)
        .map(u -> (Voluntario) u)
        .collect(Collectors.toList());


        // Filtrar voluntarios por proximidad y disponibilidad
        List<Voluntario> voluntariosCompatibles = voluntarios.stream()
            .filter(voluntario -> {
                // Verificar si el voluntario está dentro del radio permitido
                double distancia = calcularDistancia(
                    extraerLatitud(tarea.getLocalizacion()),
                    extraerLongitud(tarea.getLocalizacion()),
                    extraerLatitud(voluntario.getDireccion()),
                    extraerLongitud(voluntario.getDireccion())
                );

                // Verificar disponibilidad de horario
                boolean horarioCompatible = verificarDisponibilidadHorario(voluntario, tarea);

                return distancia <= radioMaximo && horarioCompatible;
            })
            .collect(Collectors.toList());

        // Inicializar la lista de voluntarios asignados si es nula
        if (tarea.getVoluntariosAsignados() == null) {
            tarea.setVoluntariosAsignados(new ArrayList<>());
        }
        
        // Asignar los voluntarios compatibles a la tarea
        // Limitar el número de voluntarios asignados al número necesario
        int voluntariosNecesarios = tarea.getNumeroVoluntariosNecesarios();
        int voluntariosAAsignar = Math.min(voluntariosCompatibles.size(), voluntariosNecesarios);
        
        for (int i = 0; i < voluntariosAAsignar; i++) {
            Voluntario voluntario = voluntariosCompatibles.get(i);
            if (!tarea.getVoluntariosAsignados().contains(voluntario)) {
                tarea.getVoluntariosAsignados().add(voluntario);
                // Publicar evento usando el patrón Observer
                eventPublisher.publishEvent(new SolidarityHub.events.NuevaTareaAsignadaEvent(this, tarea, voluntario));
            }
        }
    }

    private boolean verificarDisponibilidadHorario(Voluntario voluntario, Tarea tarea) {
        // Obtener el día de la semana de la tarea
        String diaTarea = tarea.getFechaInicio().getDayOfWeek().toString();
        
        // Verificar si el voluntario está disponible ese día
        return voluntario.getDiasDisponibles().contains(diaTarea);
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return EARTH_RADIUS * c;
    }

    private double extraerLatitud(String ubicacion) {
        // Formato esperado: "latitud,longitud"
        String[] coordenadas = ubicacion.split(",");
        return Double.parseDouble(coordenadas[0].trim());
    }

    private double extraerLongitud(String ubicacion) {
        // Formato esperado: "latitud,longitud"
        String[] coordenadas = ubicacion.split(",");
        return Double.parseDouble(coordenadas[1].trim());
    }
}