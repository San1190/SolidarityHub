package SolidarityHub.commands;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.vector.LCircleMarker;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;

import java.util.List;

/**
 * Comando para marcar un almacén en el mapa.
 */
public class MarkStoreCommand implements MapCommand {
    
    private final List<LCircleMarker> stores;
    
    /**
     * Constructor que recibe la lista de almacenes para poder añadir el nuevo.
     * 
     * @param stores Lista donde se almacenarán los marcadores de almacenes creados
     */
    public MarkStoreCommand(List<LCircleMarker> stores) {
        this.stores = stores;
    }
    
    @Override
    public void execute(LMap map, LComponentManagementRegistry registry, double lat, double lng) {
        LLatLng pos = new LLatLng(registry, lat, lng);
        LCircleMarker store = new LCircleMarker(registry, pos);
        store.setRadius(8);
        
        // Configurar el almacén con color verde
        store.bindTooltip("Almacén en " + lat + ", " + lng);
        store.addTo(map);
        stores.add(store);
        
        // Aplicar estilo verde al marcador usando JavaScript
        UI.getCurrent().getPage().executeJs(
            "setTimeout(() => {" +
                "const leafletLayers = document.querySelectorAll('.leaflet-interactive');" +
                "const circles = Array.from(leafletLayers).filter(el => el.tagName === 'circle' || el.tagName === 'path');" +
                "if (circles.length > 0) {" +
                    "const lastCircle = circles[circles.length - 1];" +
                    "lastCircle.setAttribute('stroke', '#2ecc71');" + // Verde
                    "lastCircle.setAttribute('fill', '#2ecc71');" +  // Verde
                    "lastCircle.setAttribute('fill-opacity', '0.7');" +
                    "lastCircle.setAttribute('stroke-width', '2');" +
                "}" +
            "}, 100);"
        );
        
        Notification.show("Almacén marcado en " + String.format("%.4f, %.4f", lat, lng));
    }
    
    @Override
    public String getDescription() {
        return "Haz clic en el mapa para marcar un almacén";
    }
}