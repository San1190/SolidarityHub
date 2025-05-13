package SolidarityHub.views;

import SolidarityHub.models.Tarea;
import SolidarityHub.services.UsuarioServicio;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
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
    
    // Capas para dibujo y almacenamiento de elementos
    private LFeatureGroup drawnItems;
    private LFeatureGroup resourceAreas;
    private LFeatureGroup criticalAreas;

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
        add(crearDescripcion());
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
        
        // Crear tareas de ejemplo con ubicaciones reales
        Tarea tarea1 = new Tarea();
        tarea1.setNombre("Distribución de alimentos");
        tarea1.setDescripcion("Entrega de alimentos básicos a familias afectadas");
        tarea1.setLocalizacion("39.4699,-0.3763"); // Valencia
        tarea1.setNumeroVoluntariosNecesarios(5);
        
        Tarea tarea2 = new Tarea();
        tarea2.setNombre("Limpieza de escombros");
        tarea2.setDescripcion("Ayuda para limpiar calles después de inundaciones");
        tarea2.setLocalizacion("39.4821,-0.3843"); // Cerca de Valencia
        tarea2.setNumeroVoluntariosNecesarios(10);
        
        Tarea tarea3 = new Tarea();
        tarea3.setNombre("Asistencia médica");
        tarea3.setDescripcion("Atención médica básica para personas afectadas");
        tarea3.setLocalizacion("39.4569,-0.3525"); // Otra ubicación en Valencia
        tarea3.setNumeroVoluntariosNecesarios(3);
        
        tareas.add(tarea1);
        tareas.add(tarea2);
        tareas.add(tarea3);
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
        
        // Exponer el mapa a JavaScript para poder acceder desde los botones
        UI.getCurrent().getPage().executeJs(
            "window.map = document.querySelector('vaadin-map').map;");
        
        // Añadir controles de dibujo al mapa
        configurarControlesDibujo();
        
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
    
    private Component crearDescripcion() {
        VerticalLayout descripcionLayout = new VerticalLayout();
        descripcionLayout.setSpacing(false);
        descripcionLayout.setPadding(false);
        descripcionLayout.setAlignItems(Alignment.CENTER);
        
        Paragraph descripcion = new Paragraph(
            "Este mapa muestra tareas cercanas a tu ubicación y te permite crear áreas críticas y zonas de recursos. "
            + "Utiliza las herramientas de dibujo para marcar áreas importantes en el mapa.");
        descripcion.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("max-width", "800px")
            .set("text-align", "center");
        
        descripcionLayout.add(descripcion);
        return descripcionLayout;
    }
    
    private Component crearControlesMapa() {
        HorizontalLayout controlesLayout = new HorizontalLayout();
        controlesLayout.setWidthFull();
        controlesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        controlesLayout.setSpacing(true);
        controlesLayout.setPadding(true);
        
        Button btnAreaCritica = new Button("Crear Área Crítica", new Icon(VaadinIcon.EXCLAMATION_CIRCLE));
        btnAreaCritica.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        btnAreaCritica.addClickListener(e -> activarDibujoPoligono("critica"));
        
        Button btnAreaRecursos = new Button("Crear Zona de Recursos", new Icon(VaadinIcon.PACKAGE));
        btnAreaRecursos.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnAreaRecursos.addClickListener(e -> activarDibujoPoligono("recursos"));
        
        Button btnLimpiar = new Button("Limpiar Dibujos", new Icon(VaadinIcon.ERASER));
        btnLimpiar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnLimpiar.addClickListener(e -> limpiarDibujos());
        
        controlesLayout.add(btnAreaCritica, btnAreaRecursos, btnLimpiar);
        return controlesLayout;
    }
    
    private void configurarControlesDibujo() {
        // Crear capas para almacenar elementos dibujados
        drawnItems = new LFeatureGroup(registry);
        resourceAreas = new LFeatureGroup(registry);
        criticalAreas = new LFeatureGroup(registry);
        
        // Añadir capas al mapa
        drawnItems.addTo(map);
        resourceAreas.addTo(map);
        criticalAreas.addTo(map);
        
        // Configurar opciones de dibujo
        LDrawOptions drawOptions = new LDrawOptions(registry);
        drawOptions.setPolyline(false);
        drawOptions.setCircle(true);
        drawOptions.setCircleMarker(false);
        drawOptions.setRectangle(true);
        drawOptions.setPolygon(true);
        
        // Configurar control de dibujo
        LDrawControlOptions drawControlOptions = new LDrawControlOptions(registry);
        drawControlOptions.setDraw(drawOptions);
        drawControlOptions.setEdit(new LDrawOptions(registry));
        
        LDrawControl drawControl = new LDrawControl(registry, drawControlOptions);
        drawControl.addTo(map);
        
        // Configurar eventos de dibujo
        map.on(LDrawEvents.CREATED, event -> {
            LDrawEvent drawEvent = (LDrawEvent) event;
            LLayer layer = drawEvent.getLayer();
            
            // Añadir la capa dibujada al grupo de elementos dibujados
            drawnItems.addLayer(layer);
            
            // Mostrar diálogo para clasificar el área
            mostrarDialogoClasificacionArea(layer);
        });
    }
    
    private void activarDibujoPoligono(String tipo) {
        // Activar herramienta de dibujo de polígono y guardar el tipo para usarlo después
        UI.getCurrent().getPage().executeJs(
            "if (window.drawingType) { window.drawingType = $0; }"
            + "else { window.drawingType = $0; }"
            + "if (window.map) { new L.Draw.Polygon(window.map).enable(); }",
            tipo);
        
        Notification.show("Dibuja un polígono en el mapa para marcar el área", 
                          3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }
    
    private void limpiarDibujos() {
        if (drawnItems != null) {
            drawnItems.clearLayers();
        }
        if (resourceAreas != null) {
            resourceAreas.clearLayers();
        }
        if (criticalAreas != null) {
            criticalAreas.clearLayers();
        }
        
        Notification.show("Se han eliminado todas las áreas dibujadas", 
                          3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void mostrarDialogoClasificacionArea(LLayer layer) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Clasificar Área");
        
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(Alignment.STRETCH);
        
        TextField nombreField = new TextField("Nombre del área");
        nombreField.setRequired(true);
        nombreField.setWidthFull();
        
        TextArea descripcionField = new TextArea("Descripción");
        descripcionField.setWidthFull();
        descripcionField.setHeight("100px");
        
        RadioButtonGroup<String> tipoGroup = new RadioButtonGroup<>();
        tipoGroup.setLabel("Tipo de área");
        tipoGroup.setItems("Área Crítica", "Zona de Recursos");
        tipoGroup.setValue("Área Crítica");
        
        dialogLayout.add(nombreField, descripcionField, tipoGroup);
        
        Button guardarBtn = new Button("Guardar", e -> {
            if (nombreField.getValue().isEmpty()) {
                Notification.show("El nombre es obligatorio", 
                                  3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            // Añadir el área a la capa correspondiente según el tipo
            if ("Zona de Recursos".equals(tipoGroup.getValue())) {
                layer.bindPopup("<div><h3>" + nombreField.getValue() + "</h3>"
                               + "<p>" + descripcionField.getValue() + "</p>"
                               + "<p><strong>Tipo:</strong> Zona de Recursos</p></div>");
                resourceAreas.addLayer(layer);
                layer.setStyle("{color: '#28a745', fillColor: '#28a745', fillOpacity: 0.3}");
            } else {
                layer.bindPopup("<div><h3>" + nombreField.getValue() + "</h3>"
                               + "<p>" + descripcionField.getValue() + "</p>"
                               + "<p><strong>Tipo:</strong> Área Crítica</p></div>");
                criticalAreas.addLayer(layer);
                layer.setStyle("{color: '#dc3545', fillColor: '#dc3545', fillOpacity: 0.3}");
            }
            
            dialog.close();
            Notification.show("Área guardada correctamente", 
                              3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        
        Button cancelarBtn = new Button("Cancelar", e -> {
            drawnItems.removeLayer(layer);
            dialog.close();
        });
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        dialog.getFooter().add(cancelarBtn, guardarBtn);
        dialog.add(dialogLayout);
        dialog.open();
    }
}