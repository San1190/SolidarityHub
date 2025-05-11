package SolidarityHub.views;

import SolidarityHub.models.Tarea;
import SolidarityHub.services.UsuarioServicio;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import software.xdev.vaadin.maps.leaflet.MapContainer;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.controls.LControlLayers;
import software.xdev.vaadin.maps.leaflet.controls.draw.LDrawControl;
import software.xdev.vaadin.maps.leaflet.controls.draw.LDrawControlOptions;
import software.xdev.vaadin.maps.leaflet.controls.draw.LDrawEvent;
import software.xdev.vaadin.maps.leaflet.controls.draw.LDrawEvents;
import software.xdev.vaadin.maps.leaflet.controls.draw.LDrawOptions;
import software.xdev.vaadin.maps.leaflet.layer.LLayer;
import software.xdev.vaadin.maps.leaflet.layer.raster.LTileLayer;
import software.xdev.vaadin.maps.leaflet.layer.vector.LCircle;
import software.xdev.vaadin.maps.leaflet.layer.vector.LCircleMarker;
import software.xdev.vaadin.maps.leaflet.layer.vector.LFeatureGroup;
import software.xdev.vaadin.maps.leaflet.layer.vector.LMarker;
import software.xdev.vaadin.maps.leaflet.layer.vector.LPolygon;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;
import software.xdev.vaadin.maps.leaflet.registry.LDefaultComponentManagementRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "main", layout = MainLayout.class)
@PageTitle("Main | SolidarityHub")
public class MainView extends VerticalLayout {

    private double userLatitude = 39.4699;    // Valor por defecto (Valencia)
    private double userLongitude = -0.3763;   // Valor por defecto (Valencia)
    private static final double RADIO_BUSQUEDA_KM = 30.0;

    private LMap map;
    private LComponentManagementRegistry registry;
    private List<Tarea> tareas;
    private List<LMarker> marcadores = new ArrayList<>();
    private Registration geolocalizacionRegistration;

    public MainView(UsuarioServicio usuarioServicio) {
        setSizeFull();
        setSpacing(false);
        setPadding(true);
        setAlignItems(Alignment.CENTER);

        inicializarTareasEjemplo();

        // Crear y añadir componentes principales
        H3 title = new H3("Mapa de Tareas y Áreas Críticas");
        title.addClassName(LumoUtility.FontSize.XXXLARGE);
        title.addClassName(LumoUtility.TextColor.SUCCESS);
        title.addClassName(LumoUtility.TextAlignment.CENTER);
        title.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        add(title);
        add(descripcion);
        add(crearControlesMapa());
        add(crearMapaContainer());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        obtenerGeolocalizacionUsuario();
    }

    @Override
    protected void onDetach(com.vaadin.flow.component.DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (geolocalizacionRegistration != null) {
            geolocalizacionRegistration.remove();
            geolocalizacionRegistration = null;
        }
    }

    private void inicializarTareasEjemplo() {
        tareas = new ArrayList<>();
        // ... crear y agregar 9 tareas como en tu ejemplo previo ...
        // Para brevedad, se omiten; asume que tareas tiene localización, nombre, descripción y voluntarios necesarios
    }

    private Component crearMapaContainer() {
        registry = new LDefaultComponentManagementRegistry(this);
        MapContainer container = new MapContainer(registry);
        container.setSizeFull();
        container.getElement().getStyle()
            .set("border-radius", "8px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
            .set("margin", "1rem auto")
            .set("max-width", "1200px")
            .set("width", "95%")
            .set("min-height", "600px");

        map = container.getlMap();
        map.addLayer(LTileLayer.createDefaultForOpenStreetMapTileServer(registry));
        map.setView(new LLatLng(registry, userLatitude, userLongitude), 10);
        return container;
    }

    private void obtenerGeolocalizacionUsuario() {
        UI ui = getUI().orElse(null);
        if (ui == null) return;

        geolocalizacionRegistration = ui.addPollListener(event -> {
            if (map != null && registry != null) {
                actualizarMapa();
            }
        });

        ui.getPage().executeJs(
            "if (navigator.geolocation) {" +
            "  navigator.geolocation.getCurrentPosition(" +
            "    function(position) { const lat = position.coords.latitude; const lng = position.coords.longitude; $0.$server.actualizarUbicacionUsuario(lat, lng); }," +
            "    function(error) { console.error(error.message); $0.$server.mostrarErrorGeolocalizacion(error.message); }," +
            "    {enableHighAccuracy: true, timeout: 10000, maximumAge: 0}" +
            "  );" +
            "} else { $0.$server.mostrarErrorGeolocalizacion('Tu navegador no soporta geolocalización'); }",
            getElement());
    }

    // Invocado desde JS
    public void actualizarUbicacionUsuario(double lat, double lng) {
        this.userLatitude = lat;
        this.userLongitude = lng;
        actualizarMapa();
        Notification.show("Ubicación: " + String.format("%.4f, %.4f", lat, lng),
                          3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public void mostrarErrorGeolocalizacion(String msg) {
        Notification.show("No se pudo obtener ubicación: " + msg + ". Usando predeterminada.",
                          5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        actualizarMapa();
    }

    private void actualizarMapa() {
        if (map == null || registry == null) return;

        // Eliminar marcadores antiguos
        for (LMarker m : marcadores) {
            m.remove();
        }
        marcadores.clear();

        // Marcador usuario
        LLatLng userLoc = new LLatLng(registry, userLatitude, userLongitude);
        LMarker userMarker = new LMarker(registry, userLoc);
        userMarker.bindPopup("<b>Tu ubicación actual</b>");
        userMarker.addTo(map);

        map.setView(userLoc, 10);

        // Filtrar y añadir tareas
        List<Tarea> cercanas = tareas.stream()
            .filter(t -> {
                double[] c = obtenerCoordenadasDeTarea(t);
                return c != null && calcularDistanciaHaversine(userLatitude, userLongitude, c[0], c[1]) <= RADIO_BUSQUEDA_KM;
            })
            .collect(Collectors.toList());

        for (Tarea t : cercanas) {
            double[] c = obtenerCoordenadasDeTarea(t);
            LLatLng ll = new LLatLng(registry, c[0], c[1]);
            LMarker mk = new LMarker(registry, ll);
            String popup = "<div style='min-width:200px;'><h3>" + t.getNombre() + "</h3>" +
                           "<p><strong>Desc:</strong> " + t.getDescripcion() + "</p>" +
                           "<p><strong>Voluntarios:</strong> " + t.getNumeroVoluntariosNecesarios() + "</p>" +
                           "</div>";
            mk.bindPopup(popup);
            mk.addTo(map);
            marcadores.add(mk);
        }

        Notification n = new Notification(
            "Mostrando " + cercanas.size() + " tareas dentro de " + RADIO_BUSQUEDA_KM + " km",
            3000, Notification.Position.BOTTOM_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        n.open();
    }

    private double[] obtenerCoordenadasDeTarea(Tarea tarea) {
        try {
            String[] p = tarea.getLocalizacion().split(",");
            return new double[]{ Double.parseDouble(p[0]), Double.parseDouble(p[1]) };
        } catch (Exception e) {
            return null;
        }
    }

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}