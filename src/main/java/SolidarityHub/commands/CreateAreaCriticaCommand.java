package SolidarityHub.commands;

import com.vaadin.flow.component.notification.Notification;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.vector.LPolygon;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando para crear un área crítica en el mapa (representada como un polígono).
 * Este comando permite seleccionar un punto central y crear una zona de recursos
 * alrededor de ese punto.
 */
public class CreateAreaCriticaCommand implements MapCommand {
    
    private final List<LPolygon> areas;
    private double firstLat;
    private double firstLng;
    private boolean esperandoSegundoPunto;
    
    /**
     * Constructor que recibe la lista de áreas críticas para poder añadir la nueva.
     * 
     * @param areas Lista donde se almacenarán los polígonos creados
     */
    public CreateAreaCriticaCommand(List<LPolygon> areas) {
        this.areas = areas;
        this.esperandoSegundoPunto = false;
    }
    
    @Override
    public void execute(LMap map, LComponentManagementRegistry registry, double lat, double lng) {
        if (!esperandoSegundoPunto) {
            // Primer clic: guardamos el primer punto (esquina superior izquierda)
            firstLat = lat;
            firstLng = lng;
            esperandoSegundoPunto = true;
            Notification.show("Selecciona el segundo punto para completar el área crítica");
        } else {
            // Segundo clic: creamos el polígono con los dos puntos
            crearPoligono(map, registry, lat, lng);
            esperandoSegundoPunto = false; // Reiniciamos para la próxima área
        }
    }
    
    /**
     * Crea un polígono rectangular usando dos puntos opuestos.
     */
    private void crearPoligono(LMap map, LComponentManagementRegistry registry, double lat2, double lng2) {
        // Calculamos las coordenadas de los cuatro vértices del rectángulo
        double lat1 = firstLat;
        double lng1 = firstLng;
        
        // Creamos un array de LLatLng con los vértices del polígono (rectángulo)
        List<LLatLng> vertices = new ArrayList<>();
        
        // Añadimos los vértices del rectángulo
        vertices.add(new LLatLng(registry, lat1, lng1)); // esquina superior izquierda
        vertices.add(new LLatLng(registry, lat1, lng2)); // esquina superior derecha
        vertices.add(new LLatLng(registry, lat2, lng2)); // esquina inferior derecha
        vertices.add(new LLatLng(registry, lat2, lng1)); // esquina inferior izquierda
        vertices.add(new LLatLng(registry, lat1, lng1)); // cerramos el polígono volviendo al inicio
        
        // Creamos el polígono
        LPolygon polygon = new LPolygon(registry, vertices);
        polygon.bindTooltip("Área Crítica");
        
        // Añadimos el polígono al mapa y a la lista
        polygon.addTo(map);
        areas.add(polygon);
        
        // Notificamos al usuario
        Notification.show("Área crítica creada");
    }
    
    @Override
    public String getDescription() {
        return "Haz clic en dos puntos del mapa para crear un área crítica rectangular";
    }
} 