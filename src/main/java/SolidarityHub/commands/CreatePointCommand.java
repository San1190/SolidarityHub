package SolidarityHub.commands;

import com.vaadin.flow.component.notification.Notification;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.ui.LMarker;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;

import java.util.List;

/**
 * Comando para crear un punto de necesidad en el mapa.
 */
public class CreatePointCommand implements MapCommand {
    
    private final List<LMarker> markers;
    
    /**
     * Constructor que recibe la lista de marcadores para poder añadir el nuevo punto.
     * 
     * @param markers Lista donde se almacenarán los marcadores creados
     */
    public CreatePointCommand(List<LMarker> markers) {
        this.markers = markers;
    }
    
    @Override
    public void execute(LMap map, LComponentManagementRegistry registry, double lat, double lng) {
        LLatLng pos = new LLatLng(registry, lat, lng);
        LMarker marker = new LMarker(registry, pos);
        marker.bindTooltip("Necesidad en " + lat + ", " + lng);
        marker.addTo(map);
        markers.add(marker);
        Notification.show("Punto de necesidad creado en " + String.format("%.4f, %.4f", lat, lng));
    }
    
    @Override
    public String getDescription() {
        return "Haz clic en el mapa para crear un punto de necesidad";
    }
}