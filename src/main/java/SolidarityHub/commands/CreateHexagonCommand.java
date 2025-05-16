package SolidarityHub.commands;

import com.vaadin.flow.component.notification.Notification;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.vector.LCircle;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;

import java.util.List;

/**
 * Comando para crear un punto de encuentro (representado como un hexágono/círculo) en el mapa.
 */
public class CreateHexagonCommand implements MapCommand {
    
    private final List<LCircle> circles;
    private final double centerLat;
    private final double centerLng;
    private final int radiusKm;
    
    /**
     * Constructor que recibe la lista de círculos y las coordenadas del centro.
     * 
     * @param circles Lista donde se almacenarán los círculos creados
     * @param centerLat Latitud del centro del círculo
     * @param centerLng Longitud del centro del círculo
     * @param radiusKm Radio del círculo en kilómetros
     */
    public CreateHexagonCommand(List<LCircle> circles, double centerLat, double centerLng, int radiusKm) {
        this.circles = circles;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.radiusKm = radiusKm;
    }
    
    @Override
    public void execute(LMap map, LComponentManagementRegistry registry, double lat, double lng) {
        // En este caso ignoramos lat y lng porque siempre creamos el círculo en el centro definido
        LCircle circle = new LCircle(
            registry,
            new LLatLng(registry, centerLat, centerLng),
            radiusKm * 1000
        );
        circle.addTo(map);
        circles.add(circle);
        Notification.show("Punto de encuentro creado");
    }
    
    @Override
    public String getDescription() {
        return "Crear un punto de encuentro en el centro del mapa";
    }
}