package SolidarityHub.services;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Voluntario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    /**
     * Asigna automáticamente voluntarios a una tarea basándose en proximidad, disponibilidad y habilidades
     * @param tarea La tarea a la que se asignarán voluntarios
     * @param radioMaximo El radio máximo de distancia para considerar voluntarios (en km)
     */
    public void asignarVoluntariosAutomaticamente(Tarea tarea, double radioMaximo) {
        // Obtener todos los voluntarios disponibles
        List<Voluntario> voluntarios = usuarioRepositorio.findAll().stream()
        .filter(u -> u instanceof Voluntario)
        .map(u -> (Voluntario) u)
        .collect(Collectors.toList());

        // Filtrar voluntarios por proximidad, disponibilidad y habilidades compatibles
        List<Voluntario> voluntariosCompatibles = voluntarios.stream()
            .filter(voluntario -> {
                // Verificar nulidad de localizaciones antes de calcular distancia
                if (tarea.getLocalizacion() == null || voluntario.getDireccion() == null) {
                    return false; // No se puede calcular distancia si falta alguna ubicación
                }

                // Verificar si el voluntario está dentro del radio permitido
                double distancia = calcularDistancia(
                    extraerLatitud(tarea.getLocalizacion()),
                    extraerLongitud(tarea.getLocalizacion()),
                    extraerLatitud(voluntario.getDireccion()),
                    extraerLongitud(voluntario.getDireccion())
                );
                
                // Si la distancia es inválida (p.ej., por error en coordenadas), descartar voluntario
                if (distancia < 0) { 
                    return false;
                }

                // Verificar disponibilidad de horario
                boolean horarioCompatible = verificarDisponibilidadHorario(voluntario, tarea);
                
                // Verificar si el voluntario tiene las habilidades necesarias para la tarea
                boolean habilidadesCompatibles = verificarHabilidadesCompatibles(voluntario, tarea);

                return distancia <= radioMaximo && horarioCompatible && habilidadesCompatibles;
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
        
        // Asignar voluntarios y crear notificaciones
        for (int i = 0; i < voluntariosAAsignar; i++) {
            Voluntario voluntario = voluntariosCompatibles.get(i);
            
            // Verificar que el voluntario no esté ya asignado a la tarea
            if (!tarea.getVoluntariosAsignados().contains(voluntario)) {
                // Añadir el voluntario a la tarea
                tarea.getVoluntariosAsignados().add(voluntario);
                
                // Crear una notificación para el voluntario
                SolidarityHub.models.Notificacion notificacion = new SolidarityHub.models.Notificacion(
                    "Nueva tarea asignada",
                    "Se te ha asignado la tarea: " + tarea.getNombre() + ". Por favor, revisa los detalles y confirma tu participación.",
                    voluntario,
                    tarea,
                    SolidarityHub.models.Notificacion.EstadoNotificacion.PENDIENTE
                );
                
                // Guardar la notificación
                notificacionServicio.crearNotificacion(notificacion);
            }
        }
        
    }

    /**
     * Verifica si un voluntario está disponible en el horario de la tarea
     * @param voluntario El voluntario a verificar
     * @param tarea La tarea a verificar
     * @return true si el voluntario está disponible, false en caso contrario
     */
    public boolean verificarDisponibilidadHorario(Voluntario voluntario, Tarea tarea) {
        if (tarea.getFechaInicio() == null) {
            return true; // Asumimos disponibilidad por defecto
        }
        String diaTarea = tarea.getFechaInicio().getDayOfWeek().toString();
        boolean diaDisponible = voluntario.getDiasDisponibles() != null && 
                               voluntario.getDiasDisponibles().contains(diaTarea);
        boolean turnoCompatible = true;
        if (voluntario.getTurnoDisponibilidad() != null && !voluntario.getTurnoDisponibilidad().isEmpty()) {
            int horaInicio = tarea.getFechaInicio().getHour();
            
            switch (voluntario.getTurnoDisponibilidad().toUpperCase()) {
                case "MAÑANA":
                    turnoCompatible = horaInicio >= 8 && horaInicio < 14;
                    break;
                case "TARDE":
                    turnoCompatible = horaInicio >= 14 && horaInicio < 20;
                    break;
                case "NOCHE":
                    turnoCompatible = horaInicio >= 20 || horaInicio < 8;
                    break;
                default:
                    turnoCompatible = true; // Si no tiene un turno específico, asumimos disponibilidad
            }
        }
        return diaDisponible && turnoCompatible;
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
        if (ubicacion == null || ubicacion.trim().isEmpty()) {
            return -1; // Valor inválido para indicar error
        }
        try {
            String[] coordenadas = ubicacion.split(",");
            if (coordenadas.length < 1) return -1;
            return Double.parseDouble(coordenadas[0].trim());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Error al extraer latitud de: " + ubicacion + " - " + e.getMessage());
            return -1; // Valor inválido para indicar error
        }
    }

    private double extraerLongitud(String ubicacion) {
        // Formato esperado: "latitud,longitud"
         if (ubicacion == null || ubicacion.trim().isEmpty()) {
            return -1; // Valor inválido para indicar error
        }
        try {
            String[] coordenadas = ubicacion.split(",");
            if (coordenadas.length < 2) return -1;
            return Double.parseDouble(coordenadas[1].trim());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Error al extraer longitud de: " + ubicacion + " - " + e.getMessage());
            return -1; // Valor inválido para indicar error
        }
    }
    
    /**
     * Verifica si un voluntario tiene las habilidades necesarias para una tarea
     * @param voluntario El voluntario a verificar
     * @param tarea La tarea a verificar
     * @return true si el voluntario tiene las habilidades necesarias, false en caso contrario
     */
    public boolean verificarHabilidadesCompatibles(Voluntario voluntario, Tarea tarea) {
        if (tarea.getHabilidadesRequeridas() == null || tarea.getHabilidadesRequeridas().isEmpty()) {
            return true;
        }
        if (voluntario.getHabilidades() == null || voluntario.getHabilidades().isEmpty()) {
            return false;
        }

        return voluntario.getHabilidades().stream()
                .filter(habilidadVoluntario -> habilidadVoluntario != null && habilidadVoluntario.getNombre() != null)
                .anyMatch(habilidadVoluntario -> 
                    tarea.getHabilidadesRequeridas().stream()
                        .filter(habilidadRequerida -> habilidadRequerida != null && habilidadRequerida.getNombre() != null)
                        .anyMatch(habilidadRequerida -> 
                            habilidadVoluntario.getNombre().equalsIgnoreCase(habilidadRequerida.getNombre())
                        )
                );
    }
}