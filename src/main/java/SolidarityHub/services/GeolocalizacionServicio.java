package SolidarityHub.services;

import SolidarityHub.models.Necesidad;
import SolidarityHub.models.Tarea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeolocalizacionServicio {

    private final TareaServicio tareaServicio;
    private final NecesidadServicio necesidadServicio;

    @Autowired
    public GeolocalizacionServicio(TareaServicio tareaServicio, NecesidadServicio necesidadServicio) {
        this.tareaServicio = tareaServicio;
        this.necesidadServicio = necesidadServicio;
    }

    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     * @param lat1 Latitud del punto 1
     * @param lon1 Longitud del punto 1
     * @param lat2 Latitud del punto 2
     * @param lon2 Longitud del punto 2
     * @return Distancia en kilómetros
     */
    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Convierte una cadena de ubicación en coordenadas [latitud, longitud]
     * @param ubicacion Cadena de ubicación (formato: "latitud,longitud")
     * @return Array con [latitud, longitud]
     */
    public double[] extraerCoordenadas(String ubicacion) {
        try {
            String[] partes = ubicacion.split(",");
            if (partes.length == 2) {
                double latitud = Double.parseDouble(partes[0].trim());
                double longitud = Double.parseDouble(partes[1].trim());
                return new double[]{latitud, longitud};
            }
        } catch (Exception e) {
            // Si hay error en el formato, devolvemos coordenadas por defecto (Valencia)
            return new double[]{39.4699, -0.3763};
        }
        // Coordenadas por defecto (Valencia)
        return new double[]{39.4699, -0.3763};
    }

    /**
     * Obtiene todas las tareas dentro de un radio específico desde una ubicación
     * @param latitudUsuario Latitud del usuario
     * @param longitudUsuario Longitud del usuario
     * @param radioKm Radio en kilómetros
     * @return Lista de tareas dentro del radio
     */
    public List<Tarea> obtenerTareasCercanas(double latitudUsuario, double longitudUsuario, double radioKm) {
        List<Tarea> todasLasTareas = tareaServicio.listarTareas();
        
        return todasLasTareas.stream()
                .filter(tarea -> {
                    if (tarea.getLocalizacion() == null || tarea.getLocalizacion().isEmpty()) {
                        return false;
                    }
                    
                    double[] coordenadasTarea = extraerCoordenadas(tarea.getLocalizacion());
                    double distancia = calcularDistancia(
                            latitudUsuario, longitudUsuario,
                            coordenadasTarea[0], coordenadasTarea[1]);
                    
                    return distancia <= radioKm;
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las necesidades dentro de un radio específico desde una ubicación
     * @param latitudUsuario Latitud del usuario
     * @param longitudUsuario Longitud del usuario
     * @param radioKm Radio en kilómetros
     * @return Lista de necesidades dentro del radio
     */
    public List<Necesidad> obtenerNecesidadesCercanas(double latitudUsuario, double longitudUsuario, double radioKm) {
        List<Necesidad> todasLasNecesidades = necesidadServicio.listarNecesidades();
        
        return todasLasNecesidades.stream()
                .filter(necesidad -> {
                    if (necesidad.getUbicacion() == null || necesidad.getUbicacion().isEmpty()) {
                        return false;
                    }
                    
                    double[] coordenadasNecesidad = extraerCoordenadas(necesidad.getUbicacion());
                    double distancia = calcularDistancia(
                            latitudUsuario, longitudUsuario,
                            coordenadasNecesidad[0], coordenadasNecesidad[1]);
                    
                    return distancia <= radioKm;
                })
                .collect(Collectors.toList());
    }
}