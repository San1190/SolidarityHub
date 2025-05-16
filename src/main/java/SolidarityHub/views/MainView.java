package SolidarityHub.views;

import SolidarityHub.commands.CreateHexagonCommand;
import SolidarityHub.commands.CreatePointCommand;
import SolidarityHub.commands.MapCommand;
import SolidarityHub.commands.MarkStoreCommand;
import SolidarityHub.commands.CreateAreaCriticaCommand;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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

import java.util.ArrayList;
import java.util.List;

@Route(value = "main", layout = MainLayout.class)
@PageTitle("Mapa de Solidaridad | SolidarityHub")
public class MainView extends VerticalLayout {
    private LMap map;
    private MapContainer mapContainer;
    private LComponentManagementRegistry registry;
    private final List<LMarker> markers = new ArrayList<>();
    private final List<LCircle> circles = new ArrayList<>();
    private final List<LCircleMarker> stores = new ArrayList<>();
    private final List<LPolygon> areasCriticas = new ArrayList<>();
    
    // Panel lateral para mostrar información detallada
    private VerticalLayout sidePanel;

    private static final double UPV_LAT = 39.4815;
    private static final double UPV_LNG = -0.3419;
    private static final int RADIUS_KM = 30;
    
    // Comando activo que se ejecutará al hacer clic en el mapa
    private MapCommand activeCommand;

    public MainView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("main-view");
        
        // Estructura principal: encabezado + contenido
        addHeader();
        
        // Contenedor principal: mapa + panel lateral
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setSizeFull();
        mainContent.setSpacing(false);
        mainContent.setPadding(false);
        
        // Inicializar y añadir el mapa (lado izquierdo)
        Div mapWrapper = new Div();
        mapWrapper.setWidthFull();
        mapWrapper.setHeight("700px");
        mapWrapper.getStyle().set("position", "relative");
        
        initMap(mapWrapper);
        
        // Inicializar panel lateral (lado derecho)
        initSidePanel();
        
        // Añadir componentes al contenedor principal
        mainContent.add(mapWrapper);
        mainContent.add(sidePanel);
        mainContent.setFlexGrow(3, mapWrapper);
        mainContent.setFlexGrow(1, sidePanel);
        
        add(mainContent);
        
        // Añadir controles encima del mapa
        addControls(mapWrapper);
    }

    private void addHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName("header");
        header.getStyle()
                .set("background", "linear-gradient(90deg, var(--lumo-primary-color) 0%, var(--lumo-primary-color-50pct) 100%)")
                .set("color", "white")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.15)");
                
        Icon mapIcon = VaadinIcon.MAP_MARKER.create();
        mapIcon.setSize("24px");
        mapIcon.setColor("white");
        
        H2 title = new H2("Mapa de Necesidades y Recursos");
        title.getStyle().set("margin", "0");
        title.getStyle().set("color", "white");
        
        Paragraph desc = new Paragraph("Visualiza y gestiona necesidades, zonas y almacenes en un radio de "
                + RADIUS_KM + " km desde UPV Campus Vera.");
        desc.getStyle().set("margin", "0").set("opacity", "0.9");
        
        VerticalLayout titleBlock = new VerticalLayout(title, desc);
        titleBlock.setPadding(false);
        titleBlock.setSpacing(false);
        
        header.add(mapIcon, titleBlock);
        add(header);
    }

    private void initMap(Div container) {
        registry = new LDefaultComponentManagementRegistry(this);
        mapContainer = new MapContainer(registry);
        mapContainer.setSizeFull();
        
        // Obtén el LMap del contenedor
        map = mapContainer.getlMap();
    
        // Capa base y vista inicial
        map.addLayer(LTileLayer.createDefaultForOpenStreetMapTileServer(registry));
        map.setView(new LLatLng(registry, UPV_LAT, UPV_LNG), 12);
    
        // Marcador fijo UPV con estilo mejorado
        LMarker upv = new LMarker(registry, new LLatLng(registry, UPV_LAT, UPV_LNG));
        upv.bindTooltip("<strong>UPV Campus Vera</strong><br/>Centro de coordinación principal");
        upv.addTo(map);
        markers.add(upv);
        
        // Añadimos el círculo de 30km alrededor de la UPV
        LCircle radiusCircle = new LCircle(
            registry,
            new LLatLng(registry, UPV_LAT, UPV_LNG),
            RADIUS_KM * 1000
        );
        radiusCircle.bindTooltip("Radio de operación: " + RADIUS_KM + "km");
        radiusCircle.addTo(map);
    
        // Añade el contenedor (que es un Component) al layout
        container.add(mapContainer);
        
        // Notificación con estilo
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setText("Mapa cargado. Selecciona una acción y haz clic en el mapa para ejecutarla.");
        notification.setDuration(3000);
        notification.open();
    
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
        
        // Comentario: No podemos usar executeJs directamente en los objetos Leaflet
        // por limitaciones de la API. El círculo tendrá el estilo predeterminado.
    }
    
    private void initSidePanel() {
        sidePanel = new VerticalLayout();
        sidePanel.setWidth("300px");
        sidePanel.setHeight("100%");
        sidePanel.setPadding(true);
        sidePanel.setSpacing(true);
        sidePanel.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-left", "1px solid var(--lumo-contrast-10pct)")
                .set("box-shadow", "-2px 0 5px rgba(0,0,0,0.05)");
        
        H4 panelTitle = new H4("Información del mapa");
        panelTitle.getStyle().set("margin-top", "0");
        
        Paragraph legendTitle = new Paragraph("Leyenda:");
        legendTitle.getStyle().set("font-weight", "bold").set("margin-bottom", "8px");
        
        VerticalLayout legend = new VerticalLayout();
        legend.setPadding(false);
        legend.setSpacing(false);
        
        legend.add(createLegendItem(VaadinIcon.MAP_MARKER, "primary", "Necesidades"));
        legend.add(createLegendItem(VaadinIcon.STORAGE, "success", "Almacén"));
        legend.add(createLegendItem(VaadinIcon.USERS, "tertiary", "Punto de encuentro"));
        legend.add(createLegendItem(VaadinIcon.EXCLAMATION_CIRCLE, "error", "Área crítica"));
        
        // Información de estadísticas
        H4 statsTitle = new H4("Estadísticas");
        
        Div stats = new Div();
        stats.add(new Paragraph("Necesidades registradas: " + markers.size()));
        stats.add(new Paragraph("Almacenes activos: " + stores.size()));
        stats.add(new Paragraph("Puntos de encuentro: " + circles.size()));
        stats.add(new Paragraph("Áreas críticas: " + areasCriticas.size()));
        
        sidePanel.add(panelTitle, legendTitle, legend, statsTitle, stats);
    }
    
    private HorizontalLayout createLegendItem(VaadinIcon icon, String color, String text) {
        HorizontalLayout item = new HorizontalLayout();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setSpacing(true);
        
        Icon iconElement = icon.create();
        iconElement.setSize("16px");
        iconElement.setColor("var(--lumo-" + color + "-color)");
        
        Paragraph textElement = new Paragraph(text);
        textElement.getStyle().set("margin", "0");
        
        item.add(iconElement, textElement);
        return item;
    }
    
    private void updateSidePanelStats() {
        // Actualizamos el panel lateral con las estadísticas actuales
        sidePanel.removeAll();
        initSidePanel();
    }

    private void addControls(Div mapContainer) {
        // Contenedor flotante para los controles
        HorizontalLayout controls = new HorizontalLayout();
        controls.getStyle()
                .set("position", "absolute")
                .set("top", "10px")
                .set("left", "50%")
                .set("transform", "translateX(-50%)")
                .set("z-index", "1000")
                .set("background-color", "rgba(255, 255, 255, 0.9)")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.1)")
                .set("padding", "8px");
        controls.setSpacing(true);

        // Inicializar comandos
        CreatePointCommand pointCommand = new CreatePointCommand(markers);
        MarkStoreCommand storeCommand = new MarkStoreCommand(stores);
        CreateHexagonCommand hexagonCommand = new CreateHexagonCommand(circles, UPV_LAT, UPV_LNG, RADIUS_KM);
        
        // Inicializar el comando para áreas críticas (usando el atributo de la clase)
        CreateAreaCriticaCommand areaCriticaCommand = new CreateAreaCriticaCommand(areasCriticas);

        // Botón de Crear Punto (estilizado)
        Button btnPoint = new Button("Crear Punto", new Icon(VaadinIcon.MAP_MARKER));
        btnPoint.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        btnPoint.addClickListener(e -> {
            activeCommand = pointCommand;
            showNotification(pointCommand.getDescription(), NotificationVariant.LUMO_PRIMARY);
        });

        // Botón de Marcar Almacén (estilizado)
        Button btnStore = new Button("Marcar Almacén", new Icon(VaadinIcon.STORAGE));
        btnStore.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        btnStore.addClickListener(e -> {
            activeCommand = storeCommand;
            showNotification(storeCommand.getDescription(), NotificationVariant.LUMO_SUCCESS);
        });

        // Botón de Punto de Encuentro (estilizado)
        Button btnVol = new Button("Punto de Encuentro", new Icon(VaadinIcon.USERS));
        btnVol.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnVol.addClickListener(e -> {
            // Ejecutamos directamente sin esperar clic en el mapa
            hexagonCommand.execute(map, registry, UPV_LAT, UPV_LNG);
            updateSidePanelStats();
        });
        
        // Botón de Área Crítica (estilizado)
        Button btnAreaCritica = new Button("Área Crítica", new Icon(VaadinIcon.EXCLAMATION_CIRCLE));
        btnAreaCritica.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        btnAreaCritica.addClickListener(e -> {
            activeCommand = areaCriticaCommand;
            showNotification(areaCriticaCommand.getDescription(), NotificationVariant.LUMO_ERROR);
        });
        
        // Botón de Limpiar Mapa (estilizado)
        Button btnClear = new Button("Limpiar Mapa", new Icon(VaadinIcon.TRASH));
        btnClear.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
        btnClear.addClickListener(e -> clearMap());

        controls.add(btnPoint, btnStore, btnVol, btnAreaCritica, btnClear);
        mapContainer.add(controls);
        
        // Establecer el comando por defecto
        activeCommand = pointCommand;
    }
    
    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000);
        notification.addThemeVariants(variant);
        notification.open();
    }

    @ClientCallable
    public void mapClicked(double lat, double lng) {
        // Si hay un comando activo, lo ejecutamos
        if (activeCommand != null) {
            showNotification("Ejecutando acción en: " + String.format("%.4f, %.4f", lat, lng), NotificationVariant.LUMO_PRIMARY);
            activeCommand.execute(map, registry, lat, lng);
            updateSidePanelStats();
        } else {
            showNotification("Selecciona primero una acción antes de hacer clic en el mapa", NotificationVariant.LUMO_ERROR);
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
        showNotification("Mapa limpiado", NotificationVariant.LUMO_SUCCESS);
        updateSidePanelStats();
    }
}
