package SolidarityHub.views;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import software.xdev.vaadin.maps.leaflet.MapContainer;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.ui.LMarker;
import software.xdev.vaadin.maps.leaflet.layer.vector.LCircle;
import software.xdev.vaadin.maps.leaflet.layer.vector.LCircleMarker;
import software.xdev.vaadin.maps.leaflet.layer.raster.LTileLayer;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;
import software.xdev.vaadin.maps.leaflet.registry.LDefaultComponentManagementRegistry;

import java.util.ArrayList;
import java.util.List;

@Route(value = "main", layout = MainLayout.class)
@PageTitle("Main | SolidarityHub")
public class MainView extends VerticalLayout {
    private LMap map;
    private LComponentManagementRegistry registry;
    private final List<LMarker> markers = new ArrayList<>();
    private final List<LCircle> circles = new ArrayList<>();
    private final List<LCircleMarker> stores = new ArrayList<>();

    private static final double UPV_LAT = 39.4815;
    private static final double UPV_LNG = -0.3419;
    private static final int RADIUS_KM = 30;

    public MainView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        addHeader();
        initMap();
        addControls();
    }

    private void addHeader() {
        H2 title = new H2("Mapa de Necesidades y Recursos");
        title.getStyle().set("margin", "1em 0 0.5em 1em");
        Paragraph desc = new Paragraph("Visualiza necesidades, zonas y almacenes en un radio de "
                + RADIUS_KM + " km desde UPV Campus Vera.");
        desc.getStyle().set("margin", "0 0 1em 1em");
        add(title, desc);
    }

    private void initMap() {
        registry = new LDefaultComponentManagementRegistry(this);
        MapContainer container = new MapContainer(registry);
        container.setSizeFull();
        container.setHeight("600px");
    
        // Obtén el LMap del contenedor
        map = container.getlMap();
    
        // Capa base y vista inicial
        map.addLayer(LTileLayer.createDefaultForOpenStreetMapTileServer(registry));
        map.setView(new LLatLng(registry, UPV_LAT, UPV_LNG), 12);
    
        // Marcador fijo UPV
        LMarker upv = new LMarker(registry, new LLatLng(registry, UPV_LAT, UPV_LNG));
        upv.bindTooltip("UPV Campus Vera");
        upv.addTo(map);
        markers.add(upv);
    
        // Añade el contenedor (que es un Component) al layout
        add(container);
    
        
        container.getElement().executeJs(
    "const host = this;" +
    // imprimimos el contenedor
    "console.log('Container element:', this);" +
    // buscamos el <vaadin-map> interno
    "const mapEl = this.querySelector('vaadin-map');" +
    "console.log('vaadin-map element:', mapEl);" +
    // imprimimos si tiene la propiedad .map
    "console.log('mapEl.map is', mapEl && mapEl.map);" +
    "if (mapEl && mapEl.map) {" +
    // instrumentamos también la llegada de un clic:
    "  mapEl.map.on('click', function(e) {" +
    "    console.log('Leaflet click!', e.latlng);" +
    "    host.$server.mapClicked(e.latlng.lat, e.latlng.lng);" +
    "  });" +
    "}"
);

    }
    
    

    private void addControls() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidthFull(); hl.setPadding(true); hl.setSpacing(true);

        Button btnPoint = new Button("Crear Punto", e -> {
            // Solo muestra instrucción, el clic se maneja por JS
            Notification.show("Ahora, haz clic en el mapa para crear un punto de necesidad");
        });

        Button btnStore = new Button("Marcar Almacén", e -> {
            Notification.show("Ahora, haz clic en el mapa para marcar un almacén");
        });

        Button btnVol = new Button("Crear Punto de Encuentro", e -> createHexagon());
        Button btnClear = new Button("Limpiar Mapa", e -> clearMap());

        hl.add(btnPoint, btnStore, btnVol, btnClear);
        add(hl);
    }

    @ClientCallable
    public void mapClicked(double lat, double lng) {
        // Dependiendo de la última acción, creamos punto o almacén.
        // Aquí siempre creamos una 'necesidad', pero puedes gestionar estado
        LLatLng pos = new LLatLng(registry, lat, lng);
        LMarker m = new LMarker(registry, pos);
        m.bindTooltip("Necesidad en " + lat + ", " + lng);
        m.addTo(map);
        markers.add(m);
        Notification.show("Punto creado en " + String.format("%.4f, %.4f", lat, lng));
    }

    private void createHexagon() {
        LCircle circle = new LCircle(
            registry,
            new LLatLng(registry, UPV_LAT, UPV_LNG),
            RADIUS_KM * 1000
        );
        circle.addTo(map);
        circles.add(circle);
        Notification.show("Hexágono creado");
    }

    private void clearMap() {
        markers.forEach(m -> map.removeLayer(m)); markers.clear();
        circles.forEach(c -> map.removeLayer(c)); circles.clear();
        stores.forEach(s -> map.removeLayer(s)); stores.clear();
        Notification.show("Mapa limpiado");
    }
}
