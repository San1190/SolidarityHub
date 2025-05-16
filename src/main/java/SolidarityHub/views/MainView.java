package SolidarityHub.views;

import SolidarityHub.commands.CreateHexagonCommand;
import SolidarityHub.commands.CreatePointCommand;
import SolidarityHub.commands.MapCommand;
import SolidarityHub.commands.MarkStoreCommand;
import SolidarityHub.commands.CreateAreaCriticaCommand;
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
import software.xdev.vaadin.maps.leaflet.layer.vector.LPolygon;
import software.xdev.vaadin.maps.leaflet.layer.raster.LTileLayer;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;
import software.xdev.vaadin.maps.leaflet.registry.LDefaultComponentManagementRegistry;
import com.vaadin.flow.component.UI;

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
    private final List<LPolygon> areasCriticas = new ArrayList<>();

    private static final double UPV_LAT = 39.4815;
    private static final double UPV_LNG = -0.3419;
    private static final int RADIUS_KM = 30;
    
    // Comando activo que se ejecutará al hacer clic en el mapa
    private MapCommand activeCommand;

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
        
        // Notificación para indicar al usuario que puede interactuar con el mapa
        Notification.show("Mapa cargado. Selecciona una acción y haz clic en el mapa para ejecutarla.");
    
        // ID para identificar este componente desde JavaScript
        setId("main-view");
        
        // Registrar evento de clic directamente en el mapa usando la API de Leaflet
        map.on("click", "e => document.getElementById('" + getId().orElse("main-view") + "').$server.mapClicked(e.latlng.lat, e.latlng.lng)");
        
        // Método adicional para capturar clics usando el ID del contenedor del mapa
        String js = String.format(
            "setTimeout(() => {" +
                "const mapContainer = document.querySelector('.leaflet-container');" +
                "if (mapContainer && mapContainer._leaflet_id) {" +
                    "const mapId = mapContainer._leaflet_id;" +
                    "if (L && L.map && L.map._instances && L.map._instances[mapId]) {" +
                        "console.log('Método directo: Mapa Leaflet encontrado');" +
                        "L.map._instances[mapId].on('click', function(e) {" +
                            "console.log('Método directo: Click en mapa', e.latlng);" +
                            "document.getElementById('%s').$server.mapClicked(e.latlng.lat, e.latlng.lng);" +
                        "});" +
                    "}" +
                "}" +
            "}, 1000);", getId().orElse("main-view"));
        
        UI.getCurrent().getPage().executeJs(js);
    }
    
    

    private void addControls() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidthFull(); hl.setPadding(true); hl.setSpacing(true);

        // Inicializar comandos
        CreatePointCommand pointCommand = new CreatePointCommand(markers);
        MarkStoreCommand storeCommand = new MarkStoreCommand(stores);
        CreateHexagonCommand hexagonCommand = new CreateHexagonCommand(circles, UPV_LAT, UPV_LNG, RADIUS_KM);
        
        // Inicializar el comando para áreas críticas (usando el atributo de la clase)
        CreateAreaCriticaCommand areaCriticaCommand = new CreateAreaCriticaCommand(areasCriticas);

        Button btnPoint = new Button("Crear Punto", e -> {
            activeCommand = pointCommand;
            Notification.show(pointCommand.getDescription());
        });

        Button btnStore = new Button("Marcar Almacén", e -> {
            activeCommand = storeCommand;
            Notification.show(storeCommand.getDescription());
        });

        Button btnVol = new Button("Crear Punto de Encuentro", e -> {
            // Ejecutamos directamente sin esperar clic en el mapa
            hexagonCommand.execute(map, registry, UPV_LAT, UPV_LNG);
        });
        
        // Botón para crear áreas críticas
        Button btnAreaCritica = new Button("Crear Área Crítica", e -> {
            activeCommand = areaCriticaCommand;
            Notification.show(areaCriticaCommand.getDescription());
        });
        
        Button btnClear = new Button("Limpiar Mapa", e -> clearMap());

        hl.add(btnPoint, btnStore, btnVol, btnAreaCritica, btnClear);
        add(hl);
        
        // Establecer el comando por defecto
        activeCommand = pointCommand;
    }

    @ClientCallable
    public void mapClicked(double lat, double lng) {
        // Si hay un comando activo, lo ejecutamos
        if (activeCommand != null) {
            Notification.show("Ejecutando comando en: " + String.format("%.4f, %.4f", lat, lng));
            activeCommand.execute(map, registry, lat, lng);
        } else {
            Notification.show("Selecciona primero una acción antes de hacer clic en el mapa");
        }
    }

    private void clearMap() {
        markers.forEach(m -> map.removeLayer(m)); markers.clear();
        circles.forEach(c -> map.removeLayer(c)); circles.clear();
        stores.forEach(s -> map.removeLayer(s)); stores.clear();
        // Necesitamos limpiar también las áreas críticas
        if (areasCriticas != null) {
            areasCriticas.forEach(a -> map.removeLayer(a)); 
            areasCriticas.clear();
        }
        Notification.show("Mapa limpiado");
    }
}
