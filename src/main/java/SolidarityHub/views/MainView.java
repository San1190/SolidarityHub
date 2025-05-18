package SolidarityHub.views;

import SolidarityHub.commands.CreateHexagonCommand;
import SolidarityHub.commands.CreatePointCommand;
import SolidarityHub.commands.MapCommand;
import SolidarityHub.commands.MarkStoreCommand;
import SolidarityHub.commands.CreateAreaCriticaCommand;
import SolidarityHub.commands.AsignarPuntoEncuentroCommand;
import SolidarityHub.commands.EditarZonaEncuentroCommand;
import SolidarityHub.models.Gestor;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.ZonaEncuentro;
import SolidarityHub.services.GeolocalizacionServicio;
import SolidarityHub.services.TareaServicio;
import SolidarityHub.services.ZonaEncuentroServicio;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

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

import com.vaadin.flow.component.select.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.vaadin.flow.component.AttachEvent;

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
    private final List<LPolygon> zonasEncuentroPersistentes = new ArrayList<>();
    
    // Usuario actual
    private Usuario usuarioActual;
    private boolean esGestor = false;
    
    // Panel lateral para mostrar información detallada
    private VerticalLayout sidePanel;
    
    // Servicios
    private final TareaServicio tareaServicio;
    private final GeolocalizacionServicio geolocalizacionServicio;
    private final ZonaEncuentroServicio zonaEncuentroServicio;
    
    // Constantes
    private static final double UPV_LAT = 39.4815;
    private static final double UPV_LNG = -0.3419;
    private static final int RADIUS_KM = 30;
    
    // Comando activo que se ejecutará al hacer clic en el mapa
    private MapCommand activeCommand;

    // Comando para asignar puntos de encuentro a tareas
    private AsignarPuntoEncuentroCommand puntoEncuentroCommand;
    
    // Tarea seleccionada para asignar punto de encuentro
    private Tarea tareaSeleccionada;

    private Button btnFinalizarZona;

    // Comandos adicionales
    private EditarZonaEncuentroCommand editarZonaCommand;
    
    // Zona seleccionada para edición
    private ZonaEncuentro zonaSeleccionada;
    
    // Marcador original de color para restaurar tras edición
    private String colorOriginalBorde;
    private String colorOriginalRelleno;

    @Autowired
    public MainView(TareaServicio tareaServicio, GeolocalizacionServicio geolocalizacionServicio,
                   ZonaEncuentroServicio zonaEncuentroServicio) {
        this.tareaServicio = tareaServicio;
        this.geolocalizacionServicio = geolocalizacionServicio;
        this.zonaEncuentroServicio = zonaEncuentroServicio;
        
        // Obtener el usuario actual de la sesión
        usuarioActual = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");
        if (usuarioActual == null) {
            UI.getCurrent().navigate("/");
            return;
        }
        
        // Verificar si el usuario es un gestor
        esGestor = usuarioActual instanceof Gestor || "gestor".equals(usuarioActual.getTipoUsuario());
        
        // Configuración de la vista
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
        
        // Añadir controles encima del mapa (solo si es gestor)
        if (esGestor) {
            addControls(mapWrapper);
        }
        
        // Cargar tareas y mostrarlas en el mapa
        cargarTareasEnMapa();
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
        
        Paragraph userInfo = new Paragraph(
            "Usuario: " + usuarioActual.getNombre() + " " + usuarioActual.getApellidos() + 
            " (" + (esGestor ? "Gestor" : usuarioActual.getTipoUsuario()) + ")"
        );
        userInfo.getStyle().set("margin-left", "auto").set("font-size", "0.9em").set("color", "white");
        
        header.add(mapIcon, titleBlock, userInfo);
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
        
        // Ya no añadimos el círculo de 30km alrededor de la UPV
        // El radio de operación se mantiene conceptualmente pero no se muestra visualmente
        
        // Añade el contenedor (que es un Component) al layout
        container.add(mapContainer);
        
        // Notificación con estilo
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setText("Mapa cargado. " + 
            (esGestor ? "Puedes gestionar el mapa seleccionando acciones y haciendo clic." : 
                      "Aquí puedes visualizar tareas y recursos."));
        notification.setDuration(3000);
        notification.open();
    
        // ID para identificar este componente desde JavaScript
        setId("main-view");
        
        // Solo los gestores pueden interactuar con el mapa
        if (esGestor) {
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
    }
    
    /**
     * Carga las tareas desde el servicio y las muestra en el mapa.
     * Cada tipo de tarea (ALIMENTO, ROPA, etc.) tiene un color distinto.
     */
    private void cargarTareasEnMapa() {
        // Obtener todas las tareas
        List<Tarea> tareas = tareaServicio.listarTareas();
        
        tareas.forEach(tarea -> {
            // Verificar si tiene localización válida
            if (tarea.getLocalizacion() != null && !tarea.getLocalizacion().isEmpty()) {
                try {
                    // Intentar extraer coordenadas del formato texto
                    double[] coords = extraerCoordenadasDeTexto(tarea.getLocalizacion());
                    if (coords != null) {
                        double lat = coords[0];
                        double lng = coords[1];
                        
                        // Crear marcador según el tipo de tarea
                        LCircleMarker marker = new LCircleMarker(registry, new LLatLng(registry, lat, lng));
                        
                        // Personalizar apariencia según tipo de tarea
                        marker.setRadius(8);
                        
                        // Asignar color según el tipo de tarea
                        String color = getColorForTaskType(tarea.getTipo());
                        
                        marker.bindTooltip("<strong>" + tarea.getNombre() + "</strong><br/>" +
                                         "Tipo: " + (tarea.getTipo() != null ? tarea.getTipo().name() : "N/A") + "<br/>" +
                                         "Estado: " + (tarea.getEstado() != null ? tarea.getEstado().name() : "N/A") + "<br/>" +
                                         "Voluntarios: " + tarea.getNumeroVoluntariosNecesarios());
                        
                        // Primero agregamos el marcador al mapa para que sea visible
                        marker.addTo(map);
                        stores.add(marker);
                        
                        // Aplicar estilos al marcador
                        final int markerIndex = stores.size() - 1;
                        UI.getCurrent().getPage().executeJs(
                            "setTimeout(() => {" +
                                "const leafletLayers = document.querySelectorAll('.leaflet-interactive');" +
                                "const circles = Array.from(leafletLayers).filter(el => el.tagName === 'circle' || el.tagName === 'path');" +
                                "if (circles.length > " + markerIndex + ") {" +
                                    "circles[" + markerIndex + "].setAttribute('stroke', '" + color + "');" +
                                    "circles[" + markerIndex + "].setAttribute('fill', '" + color + "');" +
                                    "circles[" + markerIndex + "].setAttribute('fill-opacity', '0.7');" +
                                    "circles[" + markerIndex + "].setAttribute('stroke-width', '2');" +
                                "}" +
                            "}, 1000);");
                    }
                } catch (Exception e) {
                    System.err.println("Error al procesar tarea con ubicación: " + tarea.getLocalizacion());
                    e.printStackTrace();
                }
            }
        });
        
        // Cargar las zonas de encuentro guardadas
        cargarZonasEncuentro();
        
        // Actualizar estadísticas
        updateSidePanelStats();
    }
    
    /**
     * Extrae coordenadas de un texto que puede estar en varios formatos
     */
    private double[] extraerCoordenadasDeTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return null;
        }

        try {
            // Intentar extraer coordenadas del formato "lat, lng"
            String[] partes = texto.split(",");
            if (partes.length == 2) {
                double lat = Double.parseDouble(partes[0].trim());
                double lng = Double.parseDouble(partes[1].trim());
                
                // Verificar que las coordenadas son válidas
                if (coordenadaValida(lat, lng)) {
                    return new double[]{lat, lng};
                }
            }
        } catch (NumberFormatException e) {
            // Si no se pudo parsear como coordenadas, intentar geocodificación
            try {
                // Aquí podrías implementar la geocodificación usando un servicio como Google Maps o Nominatim
                // Por ahora, retornamos null si no se pueden extraer coordenadas
                System.out.println("No se pudieron extraer coordenadas del texto: " + texto);
                return null;
            } catch (Exception ex) {
                System.err.println("Error en geocodificación: " + ex.getMessage());
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Carga las zonas de encuentro guardadas en la base de datos y las muestra en el mapa
     * con sistema mejorado de diagnóstico y reintentos
     */
    private void cargarZonasEncuentro() {
        try {
            // Limpiar las zonas anteriores
            zonasEncuentroPersistentes.forEach(z -> map.removeLayer(z));
            zonasEncuentroPersistentes.clear();
            
            // Mostrar mensaje de carga
            Notification loadingNotification = Notification.show(
                "Cargando zonas de encuentro...", 
                10000, // Aumentamos el tiempo para permitir diagnóstico completo
                Notification.Position.BOTTOM_CENTER);
            loadingNotification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            
            // Obtener todas las zonas de encuentro
            List<ZonaEncuentro> zonas = zonaEncuentroServicio.listarZonasEncuentro();
            
            System.out.println("Número de zonas de encuentro encontradas: " + zonas.size());
            
            // Contador para zonas cargadas correctamente
            int zonasOK = 0;
            List<String> errores = new ArrayList<>();
            
            for (ZonaEncuentro zona : zonas) {
                try {
                    System.out.println("Procesando zona ID: " + zona.getId() + ", coordenadas: " + zona.getCoordenadas());
                    
                    // Verificar que la zona tiene coordenadas
                    if (zona.getCoordenadas() == null || zona.getCoordenadas().isEmpty()) {
                        errores.add("Zona " + zona.getId() + ": No tiene coordenadas");
                        continue;
                    }
                    
                    // Obtener las coordenadas de la zona con manejo de errores mejorado
                    List<double[]> coordsList = new ArrayList<>();
                    try {
                        coordsList = zona.getCoordenadaComoLista();
                        System.out.println("Zona " + zona.getId() + ": Coordenadas parseadas: " + coordsList.size() + " puntos");
                    } catch (Exception e) {
                        System.err.println("Error al parsear coordenadas de zona " + zona.getId() + ": " + e.getMessage());
                        // Intento con formato alternativo como respaldo
                        coordsList = parsearCoordenadas(zona.getCoordenadas());
                    }
                    
                    // Verificar que hay suficientes puntos
                    if (coordsList.size() < 3) {
                        errores.add("Zona " + zona.getId() + ": Insuficientes puntos (" + coordsList.size() + ")");
                        System.out.println("Zona " + zona.getId() + " ignorada: insuficientes puntos (" + coordsList.size() + ")");
                        continue; // Necesitamos al menos 3 puntos para un polígono
                    }
                    
                    // Convertir las coordenadas a objetos LLatLng con validación
                    List<LLatLng> puntos = new ArrayList<>();
                    boolean puntosValidos = true;
                    for (double[] coord : coordsList) {
                        if (coord.length < 2 || !coordenadaValida(coord[0], coord[1])) {
                            puntosValidos = false;
                            errores.add("Zona " + zona.getId() + ": Coordenada inválida [" + 
                                        (coord.length > 0 ? coord[0] : "?") + "," + 
                                        (coord.length > 1 ? coord[1] : "?") + "]");
                            break;
                        }
                        puntos.add(new LLatLng(registry, coord[0], coord[1]));
                    }
                    
                    if (!puntosValidos) continue;
                    
                    // Crear el polígono con manejo de excepciones
                    LPolygon poligono = null;
                    try {
                        poligono = new LPolygon(registry, puntos.toArray(new LLatLng[0]));
                    } catch (Exception e) {
                        System.err.println("Error al crear polígono para zona " + zona.getId() + ": " + e.getMessage());
                        errores.add("Zona " + zona.getId() + ": Error al crear polígono - " + e.getMessage());
                        e.printStackTrace();
                        continue;
                    }
                    
                    // Añadir tooltip con información de la zona
                    String tooltipText = "Zona de encuentro: " + zona.getNombre();
                    tooltipText += "<br/>Tarea: " + zona.getNombreTareaSeguro();
                    poligono.bindTooltip(tooltipText);
                    
                    // Añadir el polígono al mapa
                    try {
                        poligono.addTo(map);
                    } catch (Exception e) {
                        System.err.println("Error al añadir polígono al mapa para zona " + zona.getId() + ": " + e.getMessage());
                        errores.add("Zona " + zona.getId() + ": Error al añadir al mapa - " + e.getMessage());
                        e.printStackTrace();
                        continue;
                    }
                    
                    // Aplicar estilos (colores) al polígono
                    String colorBorde = zona.getColorBorde();
                    String colorRelleno = zona.getColorRelleno();
                    
                    if (colorBorde == null) colorBorde = "#3388ff";
                    if (colorRelleno == null) colorRelleno = "#3388ff";
                    
                    // Crear un identificador único y claro para este polígono
                    final String poligonoId = "zona-" + zona.getId();
                    final Long zonaIdFinal = zona.getId(); // Capturar el ID para el mensaje de log
                    
                    // Usar JavaScript para aplicar estilos e ID al polígono, con un retraso para asegurar que está en el DOM
                    UI.getCurrent().getPage().executeJs(
                        "setTimeout(() => {" +
                            "try {" +
                                "console.log('Buscando nuevo polígono para asignar ID: " + poligonoId + "');" +
                                "const leafletLayers = document.querySelectorAll('.leaflet-overlay-pane .leaflet-interactive');" +
                                "const polygons = Array.from(leafletLayers).filter(el => el.tagName === 'path');" +
                                "if (polygons.length > 0) {" +
                                    "const lastPolygon = polygons[polygons.length - 1];" +
                                    "lastPolygon.setAttribute('stroke', '" + colorBorde + "');" +
                                    "lastPolygon.setAttribute('fill', '" + colorRelleno + "');" +
                                    "lastPolygon.setAttribute('fill-opacity', '0.4');" +
                                    "lastPolygon.setAttribute('stroke-width', '3');" +
                                    "lastPolygon.id = '" + poligonoId + "';" +
                                    "console.log('ID asignado a polígono: " + poligonoId + "');" +
                                    "lastPolygon.setAttribute('data-zona-id', '" + zonaIdFinal + "');" +
                                    
                                    // Añadir un manejador de eventos directo para diagnóstico
                                    "lastPolygon.onclick = function(event) {" +
                                        "console.log('Clic directo en polígono:', this.id, this.getAttribute('data-zona-id'));" +
                                        "if (event.ctrlKey || event.metaKey) {" +
                                            "console.log('Ctrl+Clic detectado');" +
                                            "if (" + esGestor + ") {" +
                                                "document.getElementById('" + getId().orElse("main-view") + "').$server.zonaClicked(" +
                                                    "this.id, event.clientX, event.clientY, true);" +
                                            "}" +
                                        "} else {" +
                                            "console.log('Clic normal detectado');" +
                                            "document.getElementById('" + getId().orElse("main-view") + "').$server.zonaClicked(" +
                                                "this.id, event.clientX, event.clientY, false);" +
                                        "}" +
                                        "event.stopPropagation();" +
                                    "};" +
                                "}" +
                            "} catch(e) {" +
                                "console.error('Error al aplicar estilos:', e);" +
                            "}" +
                        "}, 500);");
                    
                    // Guardar referencia al polígono en la lista de zonas persistentes
                    zonasEncuentroPersistentes.add(poligono);
                    zonasOK++;
                    
                    System.out.println("Zona de encuentro cargada: " + zona.getNombre() + " (ID: " + zona.getId() + 
                                      "), coordenadas: " + zona.getCoordenadas());
                    
                    // Dentro del método cargarZonasEncuentro, después de añadir exitosamente un polígono y antes de continuar con la siguiente zona

                    // Añadir un marcador central para facilitar la selección y visualización de cada zona
                    if (!coordsList.isEmpty()) {
                        try {
                            // Calcular el centro aproximado
                            double sumLat = 0;
                            double sumLng = 0;
                            for (double[] coord : coordsList) {
                                sumLat += coord[0];
                                sumLng += coord[1];
                            }
                            double centerLat = sumLat / coordsList.size();
                            double centerLng = sumLng / coordsList.size();
                            
                            // Determinar el color del marcador según el tipo de tarea
                            String markerColor = "#ffffff"; // Color por defecto
                            if (zona.getTarea() != null && zona.getTarea().getTipo() != null) {
                                markerColor = getColorForTaskType(zona.getTarea().getTipo());
                            }
                            
                            // Código mejorado para referenciar correctamente el mapa
                            UI.getCurrent().getPage().executeJs(
                                "setTimeout(() => {" +
                                    "try {" +
                                        "const centerIcon = L.divIcon({" +
                                            "html: `<div style=\"background-color: " + markerColor + "; " +
                                                  "border-radius: 50%; width: 10px; height: 10px; " +
                                                  "border: 2px solid white; box-shadow: 0 0 4px rgba(0,0,0,0.5); " +
                                                  "animation: pulse 2s infinite;\"></div>`," +
                                            "className: 'zona-center-marker'," +
                                            "iconSize: [14, 14]," +
                                            "iconAnchor: [7, 7]" +
                                        "});" +
                                        
                                        // Obtener la instancia del mapa
                                        "const mapDiv = document.querySelector('.leaflet-container');" +
                                        "if (!mapDiv || !mapDiv._leaflet_id) {" +
                                            "console.error('No se pudo encontrar el mapa Leaflet');" +
                                            "return;" +
                                        "}" +
                                        
                                        "const map = L.map._instances[mapDiv._leaflet_id];" +
                                        "if (!map) {" +
                                            "console.error('No se pudo obtener la instancia del mapa');" +
                                            "return;" +
                                        "}" +
                                        
                                        // Añadir el marcador al mapa y vincularlo a la zona
                                        "const centerMarker = L.marker([" + centerLat + ", " + centerLng + "], {icon: centerIcon});" +
                                        "centerMarker.addTo(map);" +
                                        
                                        // Añadir un estilo de animación único
                                        "if (!document.getElementById('zona-marker-style')) {" +
                                            "const style = document.createElement('style');" +
                                            "style.id = 'zona-marker-style';" +
                                            "style.textContent = `" +
                                                "@keyframes pulse {" +
                                                    "0% { transform: scale(1); opacity: 1; }" +
                                                    "50% { transform: scale(1.3); opacity: 0.7; }" +
                                                    "100% { transform: scale(1); opacity: 1; }" +
                                                "}" +
                                            "`;" +
                                            "document.head.appendChild(style);" +
                                        "}" +
                                        
                                        // Vincular el marcador con la zona para que al hacer clic se muestre la información
                                        "centerMarker.on('click', function(e) {" +
                                            "document.getElementById('" + getId().orElse("main-view") + "').$server.zonaClicked(" +
                                                "'zona-" + zona.getId() + "', e.originalEvent.clientX, e.originalEvent.clientY, false);" +
                                        "});" +
                                    "} catch(err) {" +
                                        "console.error('Error al crear el marcador central:', err);" +
                                    "}" +
                                "}, 800);"
                            );
                        } catch (Exception e) {
                            System.err.println("Error al crear marcador central para zona " + zona.getId() + ": " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar zona de encuentro " + zona.getId() + ": " + e.getMessage());
                    errores.add("Zona " + zona.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            loadingNotification.close();
            
            if (!zonas.isEmpty()) {
                Notification resultNotification = Notification.show(
                    "Zonas de encuentro cargadas: " + zonasOK + " de " + zonas.size(), 
                    5000, 
                    Notification.Position.BOTTOM_END
                );
                
                if (zonasOK == zonas.size()) {
                    resultNotification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else if (zonasOK > 0) {
                    resultNotification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                    if (esGestor) {
                        // Solo mostrar el diagnóstico detallado a gestores
                        mostrarDiagnosticoZonas(errores, zonas.size(), zonasOK);
                    }
                } else {
                    resultNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    if (esGestor) {
                        // Solo mostrar el diagnóstico detallado a gestores
                        mostrarDiagnosticoZonas(errores, zonas.size(), zonasOK);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error general al cargar zonas de encuentro: " + e.getMessage());
            e.printStackTrace();
            Notification.show(
                "Error al cargar zonas de encuentro: " + e.getMessage(), 
                5000, 
                Notification.Position.BOTTOM_START
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    /**
     * Muestra un diálogo con diagnóstico detallado de errores de carga de zonas
     */
    private void mostrarDiagnosticoZonas(List<String> errores, int total, int cargadas) {
        if (errores.isEmpty()) return;
        
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Diagnóstico de carga de zonas");
        dialog.setWidth("600px");
        dialog.setHeight("400px");
        
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        content.setSizeFull();
        
        H4 resumen = new H4("Resumen: " + cargadas + " de " + total + " zonas cargadas correctamente");
        content.add(resumen);
        
        // Añadir cada error en un componente separado
        for (String error : errores) {
            Paragraph p = new Paragraph(error);
            p.getStyle().set("color", "var(--lumo-error-color)");
            content.add(p);
        }
        
        // Botón para reparar si es necesario
        if (cargadas < total && esGestor) {
            Button repararBtn = new Button("Intentar reparación automática", e -> intentarReparacionZonas());
            repararBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            content.add(repararBtn);
        }
        
        Button cerrarBtn = new Button("Cerrar", e -> dialog.close());
        cerrarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        content.add(cerrarBtn);
        
        dialog.add(content);
        dialog.open();
    }
    
    /**
     * Intenta reparar zonas de encuentro con errores
     */
    private void intentarReparacionZonas() {
        Notification.show("Iniciando reparación de zonas...", 
                        3000, Notification.Position.MIDDLE)
                  .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                  
        // Recargar las zonas después de un breve retraso
        UI.getCurrent().access(() -> {
            try {
                Thread.sleep(500);
                cargarZonasEncuentro();
            } catch (Exception e) {
                System.err.println("Error en reparación: " + e.getMessage());
            }
        });
    }
    
    /**
     * Valida que una coordenada geográfica sea razonable
     */
    private boolean coordenadaValida(double lat, double lng) {
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }
    
    /**
     * Método alternativo para parsear coordenadas en caso de error
     */
    private List<double[]> parsearCoordenadas(String coordStr) {
        List<double[]> resultado = new ArrayList<>();
        if (coordStr == null || coordStr.isEmpty()) {
            return resultado;
        }
        
        try {
            // Intentar varios formatos posibles
            String[] separadores = {";", ",", " ", "|"};
            
            for (String separador : separadores) {
                try {
                    String[] partes = coordStr.split(separador);
                    
                    // Si tenemos al menos 6 partes (3 puntos x,y) y es par
                    if (partes.length >= 6 && partes.length % 2 == 0) {
                        for (int i = 0; i < partes.length; i += 2) {
                            double lat = Double.parseDouble(partes[i].trim());
                            double lng = Double.parseDouble(partes[i+1].trim());
                            if (coordenadaValida(lat, lng)) {
                                resultado.add(new double[]{lat, lng});
                            }
                        }
                        
                        if (resultado.size() >= 3) {
                            System.out.println("Coordenadas parseadas con separador: " + separador);
                            return resultado;
                        }
                        resultado.clear();
                    }
                } catch (Exception e) {
                    // Intentar con el siguiente separador
                    resultado.clear();
                }
            }
        } catch (Exception e) {
            System.err.println("Error en parseo alternativo: " + e.getMessage());
        }
        
        return resultado;
    }
    
    /**
     * Devuelve un color según el tipo de tarea.
     * @param tipo El tipo de necesidad de la tarea
     * @return Un color hexadecimal
     */
    private String getColorForTaskType(TipoNecesidad tipo) {
        if (tipo == null) return "#3388ff"; // Color por defecto (azul)
        
        return switch (tipo) {
            case PRIMEROS_AUXILIOS -> "#c0392b"; // Rojo oscuro
            case MEDICAMENTOS -> "#e67e22";     // Naranja oscuro
            case ALIMENTACION -> "#e74c3c";     // Rojo
            case ALIMENTACION_BEBE -> "#d35400"; // Naranja rojizo
            case REFUGIO -> "#8e44ad";          // Morado
            case ROPA -> "#f39c12";             // Naranja
            case SERVICIO_LIMPIEZA -> "#16a085"; // Verde azulado
            case AYUDA_PSICOLOGICA -> "#3498db"; // Azul
            case AYUDA_CARPINTERIA -> "#27ae60"; // Verde
            case AYUDA_ELECTRICIDAD -> "#2980b9"; // Azul oscuro
            case AYUDA_FONTANERIA -> "#2c3e50"; // Azul muy oscuro
            case MATERIAL_HIGENE -> "#1abc9c";  // Verde turquesa
            default -> "#3388ff";               // Azul por defecto
        };
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
        
        // Añadir leyenda para zonas de encuentro con sus colores
        H4 meetingZoneLegendTitle = new H4("Zonas de encuentro por tipo:");
        meetingZoneLegendTitle.getStyle().set("margin-top", "16px").set("margin-bottom", "8px");
        meetingZoneLegendTitle.getStyle().set("font-size", "16px");
        
        // Leyenda para zonas de encuentro por tipo de tarea
        VerticalLayout meetingZoneLegend = new VerticalLayout();
        meetingZoneLegend.setPadding(false);
        meetingZoneLegend.setSpacing(false);
        
        // Tipos de necesidad más comunes para zonas
        meetingZoneLegend.add(createColorLegendItem("#c0392b", "Primeros auxilios"));
        meetingZoneLegend.add(createColorLegendItem("#e67e22", "Medicamentos"));
        meetingZoneLegend.add(createColorLegendItem("#e74c3c", "Alimentación"));
        meetingZoneLegend.add(createColorLegendItem("#8e44ad", "Refugio"));
        meetingZoneLegend.add(createColorLegendItem("#f39c12", "Ropa"));
        meetingZoneLegend.add(createColorLegendItem("#3498db", "Ayuda psicológica"));
        
        // Información de ayuda para gestores
        if (esGestor) {
            Paragraph gestorTip = new Paragraph("Consejo: Ctrl+Clic en una zona para editar o eliminar");
            gestorTip.getStyle()
                .set("font-style", "italic")
                .set("font-size", "0.9em")
                .set("margin-top", "8px")
                .set("color", "var(--lumo-primary-color)");
            meetingZoneLegend.add(gestorTip);
        }
        
        // Botón para mostrar todas las zonas de encuentro
        Button btnMostrarZonas = new Button("Ver todas las zonas de encuentro", 
                                          new Icon(VaadinIcon.LIST));
        btnMostrarZonas.setWidthFull();
        btnMostrarZonas.getStyle().set("margin-top", "16px");
        btnMostrarZonas.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnMostrarZonas.addClickListener(e -> mostrarListadoZonas());
        
        // Información de estadísticas
        H4 statsTitle = new H4("Estadísticas");
        
        Div stats = new Div();
        stats.add(new Paragraph("Necesidades registradas: " + markers.size()));
        stats.add(new Paragraph("Almacenes activos: " + stores.size()));
        stats.add(new Paragraph("Zonas de encuentro: " + zonasEncuentroPersistentes.size()));
        stats.add(new Paragraph("Áreas críticas: " + areasCriticas.size()));
        
        // Información de usuario
        H4 userTitle = new H4("Usuario");
        Div userInfo = new Div();
        userInfo.add(new Paragraph("Nombre: " + usuarioActual.getNombre() + " " + usuarioActual.getApellidos()));
        userInfo.add(new Paragraph("Rol: " + usuarioActual.getTipoUsuario()));
        userInfo.add(new Paragraph("Email: " + usuarioActual.getEmail()));
        
        if (!esGestor) {
            Paragraph notaPermisos = new Paragraph("Nota: Solo los gestores pueden modificar el mapa");
            notaPermisos.getStyle()
                .set("font-style", "italic")
                .set("font-size", "0.9em")
                .set("color", "var(--lumo-contrast-70pct)");
            userInfo.add(notaPermisos);
        }
        
        sidePanel.add(panelTitle, legendTitle, legend, meetingZoneLegendTitle, meetingZoneLegend, 
                     btnMostrarZonas, statsTitle, stats, userTitle, userInfo);
    }
    
    /**
     * Crea un elemento de leyenda con icono
     */
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
    
    /**
     * Crea un elemento de leyenda con cuadrado de color
     */
    private HorizontalLayout createColorLegendItem(String colorHex, String text) {
        HorizontalLayout item = new HorizontalLayout();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setSpacing(true);
        
        // Crear un cuadrado de color
        Div colorSquare = new Div();
        colorSquare.getStyle()
            .set("width", "16px")
            .set("height", "16px")
            .set("background-color", colorHex)
            .set("border-radius", "3px")
            .set("border", "1px solid rgba(0,0,0,0.2)");
        
        Paragraph textElement = new Paragraph(text);
        textElement.getStyle().set("margin", "0");
        
        item.add(colorSquare, textElement);
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
        // Botón para cancelar acción actual (nuevo)
        Button btnCancelar = new Button("Cancelar acción", new Icon(VaadinIcon.CLOSE_CIRCLE));
        btnCancelar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
        btnCancelar.getStyle().set("margin-right", "10px");
        btnCancelar.addClickListener(e -> {
            // Deseleccionar comando activo
            activeCommand = null;
            // Si hay un botón de finalizar, lo eliminamos
            if (btnFinalizarZona != null) {
                remove(btnFinalizarZona);
                btnFinalizarZona = null;
            }
            showNotification("Acción cancelada. Seleccione una nueva acción o interactúe con el mapa.", 
                             NotificationVariant.LUMO_SUCCESS);
        });

        // Botón de Crear Punto (estilizado)
        Button btnPoint = new Button("Crear Punto", new Icon(VaadinIcon.MAP_MARKER));
        btnPoint.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        btnPoint.addClickListener(e -> {
            // Si hay un botón de finalizar, lo eliminamos
            if (btnFinalizarZona != null) {
                remove(btnFinalizarZona);
                btnFinalizarZona = null;
            }
            activeCommand = pointCommand;
            //showNotification(pointCommand.getDescription(), NotificationVariant.LUMO_PRIMARY);
        });

        // Botón de Marcar Almacén (estilizado)
        Button btnStore = new Button("Marcar Almacén", new Icon(VaadinIcon.STORAGE));
        btnStore.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        btnStore.addClickListener(e -> {
            // Si hay un botón de finalizar, lo eliminamos
            if (btnFinalizarZona != null) {
                remove(btnFinalizarZona);
                btnFinalizarZona = null;
            }
            activeCommand = storeCommand;
            //showNotification(storeCommand.getDescription(), NotificationVariant.LUMO_SUCCESS);
        });

        // Botón de Punto de Encuentro (estilizado)
        Button btnVol = new Button("Punto de Encuentro", new Icon(VaadinIcon.USERS));
        btnVol.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnVol.addClickListener(e -> {
            // Si hay un botón de finalizar, lo eliminamos
            if (btnFinalizarZona != null) {
                remove(btnFinalizarZona);
                btnFinalizarZona = null;
            }
            // Ejecutamos directamente sin esperar clic en el mapa
            hexagonCommand.execute(map, registry, UPV_LAT, UPV_LNG);
            updateSidePanelStats();
        });
        
        // Botón de Área Crítica (estilizado)
        Button btnAreaCritica = new Button("Área Crítica", new Icon(VaadinIcon.EXCLAMATION_CIRCLE));
        btnAreaCritica.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        btnAreaCritica.addClickListener(e -> {
            // Si hay un botón de finalizar, lo eliminamos
            if (btnFinalizarZona != null) {
                remove(btnFinalizarZona);
                btnFinalizarZona = null;
            }
            activeCommand = areaCriticaCommand;
           // showNotification(areaCriticaCommand.getDescription(), NotificationVariant.LUMO_ERROR);
        });
        
        // Botón de asignar punto de encuentro a tarea
        Button btnAsignarPuntoEncuentro = new Button("Asignar Punto Encuentro", new Icon(VaadinIcon.CONNECT));
        btnAsignarPuntoEncuentro.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
        btnAsignarPuntoEncuentro.addClickListener(e -> {
            mostrarDialogoSeleccionTarea();
        });
        
        // Botón para ver todas las zonas de encuentro
        Button btnVerZonas = new Button("Ver Zonas Encuentro", new Icon(VaadinIcon.LIST));
        btnVerZonas.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnVerZonas.addClickListener(e -> mostrarListadoZonas());
        
        // Botón de Limpiar Mapa (estilizado)
        Button btnClear = new Button("Limpiar Mapa", new Icon(VaadinIcon.TRASH));
        btnClear.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
        btnClear.addClickListener(e -> clearMap());
        
        // Etiqueta de "Solo Gestor"
        Paragraph gestorLabel = new Paragraph("Modo Gestor");
        gestorLabel.getStyle()
            .set("font-size", "0.8em")
            .set("background-color", "var(--lumo-primary-color)")
            .set("color", "white")
            .set("padding", "4px 8px")
            .set("border-radius", "4px")
            .set("margin", "0");

        controls.add(gestorLabel, btnCancelar, btnPoint, btnStore, btnVol, btnAreaCritica, btnAsignarPuntoEncuentro, btnVerZonas, btnClear);
        mapContainer.add(controls);
    }
    
    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000);
        notification.addThemeVariants(variant);
        notification.open();
    }

    @ClientCallable
    public void mapClicked(double lat, double lng) {
        // Solo permitir acciones si es un gestor
        if (!esGestor) {
            showNotification("No tienes permisos para modificar el mapa. Solo los gestores pueden hacerlo.", 
                             NotificationVariant.LUMO_ERROR);
            return;
        }
        
        // Si hay un comando activo, lo ejecutamos
        if (activeCommand != null) {
            showNotification("Ejecutando acción en: " + String.format("%.4f, %.4f", lat, lng), 
                             NotificationVariant.LUMO_PRIMARY);
            activeCommand.execute(map, registry, lat, lng);
            updateSidePanelStats();
            
            // Si el comando activo es para asignar puntos de encuentro, mostrar el botón de finalizar
            if (activeCommand instanceof AsignarPuntoEncuentroCommand && btnFinalizarZona == null) {
                mostrarBotonFinalizarZona();
            }
        } else {
            // Mensaje mejorado cuando no hay comando activo
            showNotification("No hay acción seleccionada. Seleccione primero una acción usando los botones superiores.",
                             NotificationVariant.LUMO_WARNING);
            
            // También mostramos un tooltip temporal en los botones para guiar al usuario
            UI.getCurrent().getPage().executeJs(
                "setTimeout(() => {" +
                    "const buttons = document.querySelectorAll('vaadin-button');" +
                    "buttons.forEach(btn => {" +
                        "if (btn.textContent.includes('Punto') || btn.textContent.includes('Almacén') || " +
                        "btn.textContent.includes('Área')) {" +
                            "btn.style.boxShadow = '0 0 8px var(--lumo-primary-color)';" +
                            "setTimeout(() => {btn.style.boxShadow = '';}, 2000);" +
                        "}" +
                    "});" +
                "}, 300);");
        }
    }

    private void clearMap() {
        // Cancelar cualquier comando activo
        activeCommand = null;
        
        // Limpiar todos los marcadores y formas
        markers.forEach(m -> map.removeLayer(m)); markers.clear();
        circles.forEach(c -> map.removeLayer(c)); circles.clear();
        stores.forEach(s -> map.removeLayer(s)); stores.clear();
        
        // Limpiar todas las áreas críticas
        if (areasCriticas != null) {
            areasCriticas.forEach(a -> map.removeLayer(a)); 
            areasCriticas.clear();
        }
        
        // Limpiar todas las zonas de encuentro temporales
        // (pero no las persistentes, esas se deben eliminar individualmente)
        for (LPolygon zona : zonasEncuentroPersistentes) {
            try {
                map.removeLayer(zona);
            } catch (Exception e) {
                System.err.println("Error al eliminar zona: " + e.getMessage());
            }
        }
        zonasEncuentroPersistentes.clear();
        
        // Si hay un botón de finalizar, lo eliminamos
        if (btnFinalizarZona != null) {
            remove(btnFinalizarZona);
            btnFinalizarZona = null;
        }
        
        // Limpiar cualquier estado de edición
        limpiarEstadoEdicion();
        
        showNotification("Mapa limpiado completamente", NotificationVariant.LUMO_SUCCESS);
        
        // Volver a crear el marcador de la UPV
        LMarker upv = new LMarker(registry, new LLatLng(registry, UPV_LAT, UPV_LNG));
        upv.bindTooltip("<strong>UPV Campus Vera</strong><br/>Centro de coordinación principal");
        upv.addTo(map);
        markers.add(upv);
        
        // Recargar las tareas y zonas de encuentro
        cargarTareasEnMapa();
        
        updateSidePanelStats();
    }

    /**
     * Muestra un diálogo para seleccionar la tarea a la que se asignará el punto de encuentro
     */
    private void mostrarDialogoSeleccionTarea() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Seleccionar Tarea");
        dialog.setWidth("400px");
        
        // Crear selector de tareas
        Select<Tarea> selectTarea = new Select<>();
        selectTarea.setLabel("Seleccione una tarea");
        selectTarea.setItemLabelGenerator(Tarea::getNombre);
        
        // Cargar tareas
        List<Tarea> tareas = tareaServicio.listarTareas();
        selectTarea.setItems(tareas);
        
        // Layout para los botones
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        Button confirmButton = new Button("Seleccionar", e -> {
            tareaSeleccionada = selectTarea.getValue();
            if (tareaSeleccionada != null) {
                // Verificar si la tarea ya tiene una zona asignada
                List<ZonaEncuentro> zonasExistentes = zonaEncuentroServicio.obtenerZonasPorTarea(tareaSeleccionada.getId());
                if (!zonasExistentes.isEmpty()) {
                    Notification.show(
                        "Esta tarea ya tiene una zona de encuentro asignada. Solo se permite una zona por tarea. " +
                        "Puedes editar la zona existente haciendo Ctrl+clic sobre ella.", 
                        5000, 
                        Notification.Position.MIDDLE
                    ).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                
                // Si ya había un botón de finalizar, lo eliminamos
                if (btnFinalizarZona != null) {
                    remove(btnFinalizarZona);
                    btnFinalizarZona = null;
                }
                
                puntoEncuentroCommand = new AsignarPuntoEncuentroCommand(
                    tareaServicio, zonaEncuentroServicio, tareaSeleccionada, map, registry);
                activeCommand = puntoEncuentroCommand;
                showNotification("Seleccione puntos en el mapa para la zona de encuentro. Puede añadir múltiples puntos.", 
                                NotificationVariant.LUMO_PRIMARY);
                mostrarBotonFinalizarZona();
                dialog.close();
            } else {
                Notification.show("Por favor, seleccione una tarea", 3000, 
                                  Notification.Position.MIDDLE);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        actions.add(cancelButton, confirmButton);
        
        // Añadir componentes al diálogo
        VerticalLayout dialogLayout = new VerticalLayout(selectTarea, actions);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        
        dialog.add(dialogLayout);
        dialog.open();
    }

    /**
     * Muestra un botón flotante para finalizar la creación de la zona de encuentro
     */
    private void mostrarBotonFinalizarZona() {
        // Crear el botón flotante
        btnFinalizarZona = new Button("Finalizar zona de encuentro", new Icon(VaadinIcon.CHECK_CIRCLE));
        btnFinalizarZona.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnFinalizarZona.getStyle()
                .set("position", "fixed")
                .set("bottom", "20px")
                .set("right", "20px")
                .set("z-index", "1000")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.3)");
        
        btnFinalizarZona.addClickListener(e -> finalizarZonaEncuentro());
        
        // Añadir el botón a la UI
        add(btnFinalizarZona);
    }
    
    /**
     * Finaliza la creación de la zona de encuentro y limpia el estado
     */
    private void finalizarZonaEncuentro() {
        if (activeCommand instanceof AsignarPuntoEncuentroCommand) {
            AsignarPuntoEncuentroCommand comando = (AsignarPuntoEncuentroCommand) activeCommand;
            
            // Llamar al método para finalizar la zona (cerrar el polígono)
            comando.finalizarZona();
            
            // Quitar el botón de finalizar
            remove(btnFinalizarZona);
            btnFinalizarZona = null;
            
            // Volver al comando por defecto o dejar sin comando activo
            activeCommand = null;
        }
    }

    /**
     * Inicializa los listeners para detectar clics en zonas de encuentro
     */
    private void initZonaListeners() {
        // Log para diagnóstico
        System.out.println("Inicializando listeners para zonas de encuentro");
        
        // Usar JavaScript para detectar clics en las zonas (polígonos)
        UI.getCurrent().getPage().executeJs(
            "setTimeout(() => {" +
                "console.log('Configurando listeners para polígonos...');" +
                
                "const setupPolygonListeners = () => {" +
                    // Buscar todos los elementos interactivos en la capa de superposición
                    "console.log('Buscando elementos interactivos...');" +
                    "const layers = document.querySelectorAll('.leaflet-overlay-pane .leaflet-interactive');" +
                    "console.log('Encontrados:', layers.length, 'elementos interactivos');" +
                    
                    // Iterar sobre cada capa y añadir listeners a los polígonos
                    "layers.forEach(layer => {" +
                        "if (layer.tagName === 'path' && !layer.hasAttribute('data-has-listener')) {" +
                            "console.log('Configurando listener para:', layer.id || 'polígono sin ID');" +
                            "layer.setAttribute('data-has-listener', 'true');" +
                            
                            // Añadir un evento de clic al polígono
                            "layer.addEventListener('click', (event) => {" +
                                "console.log('Clic en polígono:', layer.id);" +
                                "const polygonId = layer.id || ('zona-desconocida-' + Math.random());" +
                                
                                // Determinar qué tipo de evento manejar basado en teclas modificadoras
                                "if (event.ctrlKey || event.metaKey) {" +
                                    "console.log('Ctrl+Clic detectado en:', polygonId);" +
                                    // Si Ctrl/Cmd está presionado, mostrar menú contextual de edición (solo para gestores)
                                    "if (" + esGestor + ") {" +
                                        "document.getElementById('" + getId().orElse("main-view") + "').$server.zonaClicked(" +
                                            "polygonId, event.clientX, event.clientY, true);" +
                                    "}" +
                                "} else {" +
                                    "console.log('Clic normal detectado en:', polygonId);" +
                                    // Clic normal muestra detalles para todos los usuarios
                                    "document.getElementById('" + getId().orElse("main-view") + "').$server.zonaClicked(" +
                                        "polygonId, event.clientX, event.clientY, false);" +
                                "}" +
                                
                                "event.stopPropagation();" + // Prevenir que el mapa reciba el clic
                            "});" +
                            
                            // Añadir también un estilo hover para mejorar la UX
                            "layer.addEventListener('mouseover', () => {" +
                                "layer.setAttribute('data-original-fill-opacity', layer.getAttribute('fill-opacity') || '0.4');" +
                                "layer.setAttribute('fill-opacity', '0.6');" +
                                "layer.setAttribute('cursor', 'pointer');" +
                            "});" +
                            
                            "layer.addEventListener('mouseout', () => {" +
                                "layer.setAttribute('fill-opacity', layer.getAttribute('data-original-fill-opacity'));" +
                            "});" +
                        "}" +
                    "});" +
                "};" +
                
                // Ejecutar la configuración inicial
                "setupPolygonListeners();" +
                
                // Configurar observador para detectar nuevos polígonos dinámicamente
                "console.log('Configurando observador para nuevos polígonos...');" +
                "const observer = new MutationObserver((mutations) => {" +
                    "console.log('Mutación detectada, revisando nuevos polígonos...');" +
                    "setupPolygonListeners();" +
                "});" +
                
                "const mapPane = document.querySelector('.leaflet-overlay-pane');" +
                "if (mapPane) {" +
                    "console.log('Observando cambios en el mapa...');" +
                    "observer.observe(mapPane, { childList: true, subtree: true });" +
                "} else {" +
                    "console.error('No se encontró el contenedor del mapa');" +
                "}" +
                
                // Verificación adicional para polígonos existentes
                "setTimeout(() => {" +
                    "const existingPolygons = document.querySelectorAll('path[id^=\"zona-\"]');" +
                    "console.log('Verificación final: Se encontraron', existingPolygons.length, 'polígonos de zonas');" +
                    "existingPolygons.forEach(p => console.log('  Zona:', p.id));" +
                "}, 2000);" +
            "}, 1000);"
        );
    }
    
    @ClientCallable
    public void zonaClicked(String polygonId, double clientX, double clientY, boolean isContextMenu) {
        try {
            System.out.println("Zona clickeada: ID=" + polygonId + ", x=" + clientX + ", y=" + clientY);
            
            // Si el ID está vacío o es null, mostrar mensaje y return
            if (polygonId == null || polygonId.isEmpty()) {
                System.err.println("Error: ID de polígono es null o vacío");
                showNotification("No se pudo identificar la zona. Por favor, intente de nuevo.", 
                               NotificationVariant.LUMO_ERROR);
                return;
            }
            
            // Extraer el ID de la zona del ID del polígono
            // Formato esperado: zona-{id}-{timestamp}
            String[] partes = polygonId.split("-");
            System.out.println("Partes del ID: " + String.join(", ", partes));
            
            if (partes.length >= 2) {
                try {
                    Long zonaId = Long.parseLong(partes[1]);
                    System.out.println("Buscando zona con ID: " + zonaId);
                    
                    Optional<ZonaEncuentro> optZona = zonaEncuentroServicio.obtenerZonaEncuentroPorId(zonaId);
                    
                    if (optZona.isPresent()) {
                        System.out.println("Zona encontrada: " + optZona.get().getNombre());
                        zonaSeleccionada = optZona.get();
                        
                        if (isContextMenu && esGestor) {
                            // Mostrar menú contextual para gestores (editar/eliminar)
                            mostrarMenuContextualZona(clientX, clientY);
                        } else {
                            // Mostrar detalles para todos los usuarios
                            mostrarDetallesZona(clientX, clientY);
                        }
                    } else {
                        System.err.println("No se encontró la zona con ID: " + zonaId);
                        showNotification("No se encontró información para esta zona", 
                                       NotificationVariant.LUMO_WARNING);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error al convertir el ID de zona: " + partes[1] + ", " + e.getMessage());
                    showNotification("ID de zona inválido", NotificationVariant.LUMO_ERROR);
                }
            } else {
                System.err.println("Formato de ID incorrecto: " + polygonId);
                showNotification("Formato de zona incorrecto", NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            System.err.println("Error al procesar clic en zona: " + e.getMessage());
            e.printStackTrace();
            showNotification("Error al procesar la zona seleccionada", NotificationVariant.LUMO_ERROR);
        }
    }
    
    /**
     * Muestra un panel con detalles de la zona seleccionada
     */
    private void mostrarDetallesZona(double x, double y) {
        if (zonaSeleccionada == null) return;
        
        Dialog detallesDialog = new Dialog();
        detallesDialog.setWidth("400px");
        detallesDialog.setHeaderTitle("Detalles de Zona de Encuentro");
        
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        
        // Nombre de la zona
        H3 nombreZona = new H3(zonaSeleccionada.getNombre());
        nombreZona.getStyle()
            .set("margin-top", "0")
            .set("color", "var(--lumo-primary-color)");
        content.add(nombreZona);
        
        // Descripción
        if (zonaSeleccionada.getDescripcion() != null && !zonaSeleccionada.getDescripcion().isEmpty()) {
            Paragraph descripcion = new Paragraph(zonaSeleccionada.getDescripcion());
            descripcion.getStyle().set("font-style", "italic");
            content.add(descripcion);
        }
        
        // Información de la tarea asociada
        H4 tareaTitle = new H4("Tarea Asociada");
        tareaTitle.getStyle().set("margin-bottom", "0.2em");
        
        Div tareaInfo = new Div();
        tareaInfo.getStyle()
            .set("padding", "10px")
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "4px")
            .set("margin-bottom", "15px");
        
        try {
            if (zonaSeleccionada.getTarea() != null) {
                Tarea tarea = zonaSeleccionada.getTarea();
                Span tareaNombre = new Span(tarea.getNombre());
                tareaNombre.getStyle().set("font-weight", "bold");
                
                HorizontalLayout tareaHeader = new HorizontalLayout(tareaNombre);
                tareaHeader.setAlignItems(FlexComponent.Alignment.CENTER);
                
                // Añadir indicador de tipo/estado si está disponible
                try {
                    String tipoTexto = tarea.getTipo() != null ? tarea.getTipo().name() : "N/A";
                    String estadoTexto = tarea.getEstado() != null ? tarea.getEstado().name() : "N/A";
                    
                    Span tipo = new Span(tipoTexto);
                    tipo.getElement().getThemeList().add("badge success");
                    
                    Span estado = new Span(estadoTexto);
                    estado.getElement().getThemeList().add("badge contrast");
                    
                    tareaHeader.add(tipo, estado);
                } catch (Exception e) {
                    // Ignorar si no se puede acceder a estos campos
                }
                
                tareaInfo.add(tareaHeader);
                
                // Descripción de la tarea
                try {
                    if (tarea.getDescripcion() != null && !tarea.getDescripcion().isEmpty()) {
                        Paragraph tareaDesc = new Paragraph(tarea.getDescripcion());
                        tareaDesc.getStyle()
                            .set("margin-top", "5px")
                            .set("margin-bottom", "0");
                        tareaInfo.add(tareaDesc);
                    }
                } catch (Exception e) {
                    // Ignorar si no se puede acceder a la descripción
                }
            } else {
                tareaInfo.add(new Span("No hay tarea asociada"));
            }
        } catch (Exception e) {
            tareaInfo.add(new Span("No se puede acceder a la información de la tarea"));
        }
        
        // Información técnica de la zona
        H4 datosTitle = new H4("Datos Técnicos");
        datosTitle.getStyle().set("margin-bottom", "0.2em");
        
        List<double[]> coordenadas = zonaSeleccionada.getCoordenadaComoLista();
        
        Div datosInfo = new Div();
        datosInfo.getStyle()
            .set("padding", "10px")
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "4px");
        
        datosInfo.add(new Paragraph("ID: " + zonaSeleccionada.getId()));
        datosInfo.add(new Paragraph("Número de puntos: " + coordenadas.size()));
        
        // Calcular el centro aproximado para mostrar
        if (!coordenadas.isEmpty()) {
            double sumLat = 0;
            double sumLng = 0;
            for (double[] coord : coordenadas) {
                sumLat += coord[0];
                sumLng += coord[1];
            }
            double centerLat = sumLat / coordenadas.size();
            double centerLng = sumLng / coordenadas.size();
            
            datosInfo.add(new Paragraph("Centro aproximado: " + 
                String.format("%.6f, %.6f", centerLat, centerLng)));
        }
        
        datosInfo.add(new Paragraph("Fecha de creación: " + 
            (zonaSeleccionada.getFechaCreacion() != null ? 
             zonaSeleccionada.getFechaCreacion().toString() : "No disponible")));
        
        content.add(tareaTitle, tareaInfo, datosTitle, datosInfo);
        
        // Botones para gestores
        if (esGestor) {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setWidthFull();
            actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            actions.setMargin(true);
            actions.getStyle().set("margin-top", "15px");
            
            Button editButton = new Button("Editar zona", new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            editButton.addClickListener(e -> {
                detallesDialog.close();
                iniciarEdicionZona();
            });
            
            Button deleteButton = new Button("Eliminar", new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.addClickListener(e -> {
                detallesDialog.close();
                confirmarEliminarZona();
            });
            
            actions.add(editButton, deleteButton);
            content.add(actions);
        }
        
        // Botón cerrar para todos
        Button closeButton = new Button("Cerrar", e -> detallesDialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        HorizontalLayout footer = new HorizontalLayout(closeButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        detallesDialog.getFooter().add(footer);
        detallesDialog.add(content);
        detallesDialog.open();
    }
    
    /**
     * Inicia el modo de edición para una zona existente
     */
    private void iniciarEdicionZona() {
        if (zonaSeleccionada == null) return;
        
        // Guardar colores originales para restaurar si se cancela
        colorOriginalBorde = zonaSeleccionada.getColorBorde();
        colorOriginalRelleno = zonaSeleccionada.getColorRelleno();
        
        // Buscar la tarea asociada a la zona
        Tarea tarea = zonaSeleccionada.getTarea();
        if (tarea == null) {
            Notification.show("No se puede editar una zona sin tarea asociada", 
                             3000, Notification.Position.MIDDLE)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        // Crear el comando de edición de zona
        editarZonaCommand = new EditarZonaEncuentroCommand(
            tareaServicio, zonaEncuentroServicio, tarea, map, registry, zonaSeleccionada);
        
        // Establecer como comando activo
        activeCommand = editarZonaCommand;
        
        // Mostrar notificación y botón de finalizar edición
        showNotification("Modo edición: Haz clic en el mapa para añadir nuevos puntos a la zona", 
                         NotificationVariant.LUMO_PRIMARY);
                         
        // Si ya hay un botón de finalizar, eliminarlo
        if (btnFinalizarZona != null) {
            remove(btnFinalizarZona);
        }
        
        // Crear botón de finalizar edición
        btnFinalizarZona = new Button("Finalizar edición de zona", new Icon(VaadinIcon.CHECK_CIRCLE));
        btnFinalizarZona.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnFinalizarZona.getStyle()
                .set("position", "fixed")
                .set("bottom", "20px")
                .set("right", "20px")
                .set("z-index", "1000")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.3)");
        
        btnFinalizarZona.addClickListener(e -> finalizarEdicionZona());
        
        // Añadir botón para cancelar
        Button btnCancelarEdicion = new Button("Cancelar edición", new Icon(VaadinIcon.CLOSE_CIRCLE));
        btnCancelarEdicion.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        btnCancelarEdicion.getStyle()
                .set("position", "fixed")
                .set("bottom", "20px")
                .set("right", "250px")
                .set("z-index", "1000")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.3)");
        
        btnCancelarEdicion.addClickListener(e -> cancelarEdicionZona());
        
        // Añadir los botones a la UI
        add(btnFinalizarZona, btnCancelarEdicion);
    }
    
    /**
     * Finaliza la edición de una zona guardando los cambios
     */
    private void finalizarEdicionZona() {
        if (activeCommand instanceof EditarZonaEncuentroCommand) {
            EditarZonaEncuentroCommand comando = (EditarZonaEncuentroCommand) activeCommand;
            
            // Finalizar la edición
            comando.finalizarEdicion();
            
            // Limpiar estado
            limpiarEstadoEdicion();
            
            // Recargar zonas para mostrar los cambios
            cargarZonasEncuentro();
        }
    }
    
    /**
     * Cancela la edición y restaura el estado original
     */
    private void cancelarEdicionZona() {
        if (activeCommand instanceof EditarZonaEncuentroCommand) {
            EditarZonaEncuentroCommand comando = (EditarZonaEncuentroCommand) activeCommand;
            
            // Cancelar la edición
            comando.cancelarEdicion();
            
            // Restaurar colores originales si es necesario
            if (zonaSeleccionada != null && colorOriginalBorde != null) {
                zonaSeleccionada.setColorBorde(colorOriginalBorde);
                zonaSeleccionada.setColorRelleno(colorOriginalRelleno);
                zonaEncuentroServicio.actualizarZonaEncuentro(zonaSeleccionada);
            }
            
            // Limpiar estado
            limpiarEstadoEdicion();
            
            // Recargar zonas para mostrar estado original
            cargarZonasEncuentro();
        }
    }
    
    /**
     * Limpia el estado de edición
     */
    private void limpiarEstadoEdicion() {
        // Quitar los botones
        getChildren().forEach(componente -> {
            if (componente instanceof Button) {
                Button btn = (Button) componente;
                if (btn.getText().contains("edición")) {
                    remove(btn);
                }
            }
        });
        
        btnFinalizarZona = null;
        activeCommand = null;
        zonaSeleccionada = null;
        colorOriginalBorde = null;
        colorOriginalRelleno = null;
    }
    
    /**
     * Muestra confirmación para eliminar una zona
     */
    private void confirmarEliminarZona() {
        if (zonaSeleccionada == null) return;
        
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");
        confirmDialog.setWidth("400px");
        
        VerticalLayout content = new VerticalLayout();
        
        Icon warningIcon = VaadinIcon.WARNING.create();
        warningIcon.setColor("var(--lumo-error-color)");
        warningIcon.setSize("2em");
        
        Div iconContainer = new Div(warningIcon);
        iconContainer.getStyle()
            .set("display", "flex")
            .set("justify-content", "center")
            .set("margin-bottom", "1em");
        
        content.add(iconContainer);
        
        // Mensaje de advertencia destacado
        H4 warningTitle = new H4("¡Esta acción no se puede deshacer!");
        warningTitle.getStyle().set("text-align", "center").set("color", "var(--lumo-error-color)");
        content.add(warningTitle);
        
        content.add(new Paragraph("¿Estás seguro de que quieres eliminar la zona \"" + 
                                 zonaSeleccionada.getNombre() + "\"?"));
                                 
        // Información adicional sobre la tarea
        try {
            if (zonaSeleccionada.getTarea() != null) {
                Paragraph tareaInfo = new Paragraph("La zona está asociada a la tarea: " + 
                                               zonaSeleccionada.getTarea().getNombre());
                tareaInfo.getStyle().set("font-style", "italic");
                content.add(tareaInfo);
            }
        } catch (Exception e) {
            // Si hay error con la tarea, mostrar mensaje genérico
            Paragraph tareaInfo = new Paragraph("La zona está asociada a una tarea. " +
                                           "Esta asociación será eliminada.");
            tareaInfo.getStyle().set("font-style", "italic");
            content.add(tareaInfo);
        }
                                 
        content.setPadding(true);
        
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();
        buttons.setSpacing(true);
        
        Button cancelBtn = new Button("Cancelar", e -> confirmDialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        Button deleteBtn = new Button("Eliminar zona", e -> {
            eliminarZonaSeleccionada();
            confirmDialog.close();
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteBtn.setIcon(VaadinIcon.TRASH.create());
        
        buttons.add(cancelBtn, deleteBtn);
        content.add(buttons);
        
        confirmDialog.add(content);
        confirmDialog.open();
    }
    
    /**
     * Elimina la zona seleccionada
     */
    private void eliminarZonaSeleccionada() {
        if (zonaSeleccionada == null) return;
        
        try {
            // Guardar ID para mensaje
            Long zonaId = zonaSeleccionada.getId();
            
            // Eliminar la zona
            zonaEncuentroServicio.eliminarZonaEncuentro(zonaId);
            
            // Mostrar notificación
            Notification.show("Zona eliminada con éxito", 
                             3000, Notification.Position.BOTTOM_START)
                       .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                       
            // Recargar zonas
            cargarZonasEncuentro();
            
            // Limpiar selección
            zonaSeleccionada = null;
        } catch (Exception e) {
            System.err.println("Error al eliminar zona: " + e.getMessage());
            Notification.show("Error al eliminar zona: " + e.getMessage(), 
                             5000, Notification.Position.BOTTOM_START)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Muestra un menú contextual para editar o eliminar la zona
     */
    private void mostrarMenuContextualZona(double clientX, double clientY) {
        if (zonaSeleccionada == null) return;
        
        Dialog menuDialog = new Dialog();
        menuDialog.setWidth("250px");
        menuDialog.setHeight("auto");
        
        // Posicionar el diálogo cerca del clic
        // Usar valores fijos razonables en lugar de obtener dimensiones de la página
        String topPosition = Math.min(clientY, 600) + "px";
        String leftPosition = Math.min(clientX, 800) + "px";
        
        menuDialog.getElement().getStyle().set("position", "fixed");
        menuDialog.getElement().getStyle().set("top", topPosition);
        menuDialog.getElement().getStyle().set("left", leftPosition);
        menuDialog.getElement().getStyle().set("margin", "0");
        
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        
        // Título del menú
        H4 title = new H4("Zona: " + zonaSeleccionada.getNombre());
        title.getStyle().set("margin", "8px 16px");
        
        // Botones de acción
        Button btnEditar = new Button("Editar zona", new Icon(VaadinIcon.EDIT));
        btnEditar.setWidthFull();
        btnEditar.getStyle().set("text-align", "left");
        btnEditar.addClickListener(e -> {
            menuDialog.close();
            iniciarEdicionZona();
        });
        
        Button btnEliminar = new Button("Eliminar zona", new Icon(VaadinIcon.TRASH));
        btnEliminar.setWidthFull();
        btnEliminar.getStyle().set("text-align", "left");
        btnEliminar.getStyle().set("color", "var(--lumo-error-color)");
        btnEliminar.addClickListener(e -> {
            menuDialog.close();
            confirmarEliminarZona();
        });
        
        Button btnVer = new Button("Ver detalles", new Icon(VaadinIcon.INFO_CIRCLE));
        btnVer.setWidthFull();
        btnVer.getStyle().set("text-align", "left");
        btnVer.addClickListener(e -> {
            menuDialog.close();
            mostrarDetallesZona(clientX, clientY);
        });
        
        Button btnCerrar = new Button("Cerrar", new Icon(VaadinIcon.CLOSE));
        btnCerrar.setWidthFull();
        btnCerrar.getStyle().set("text-align", "left");
        btnCerrar.addClickListener(e -> menuDialog.close());
        
        content.add(title, btnVer, btnEditar, btnEliminar, btnCerrar);
        menuDialog.add(content);
        menuDialog.open();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Inicializar listeners para zonas al cargar la vista para todos los usuarios
        initZonaListeners();
    }

    /**
     * Muestra un diálogo con todas las zonas de encuentro disponibles
     */
    private void mostrarListadoZonas() {
        // Obtener todas las zonas de encuentro
        List<ZonaEncuentro> zonas = zonaEncuentroServicio.listarZonasEncuentro();
        
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Zonas de Encuentro Disponibles");
        dialog.setWidth("600px");
        dialog.setHeight("500px");
        
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        content.setSizeFull();
        
        if (zonas.isEmpty()) {
            Paragraph noZonas = new Paragraph("No hay zonas de encuentro disponibles");
            noZonas.getStyle().set("font-style", "italic").set("color", "var(--lumo-contrast-70pct)");
            content.add(noZonas);
        } else {
            Paragraph instrucciones = new Paragraph("Haga clic en una zona para ver detalles completos");
            instrucciones.getStyle().set("font-style", "italic").set("margin-bottom", "16px");
            content.add(instrucciones);
            
            // Crear una tarjeta para cada zona
            for (ZonaEncuentro zona : zonas) {
                HorizontalLayout card = new HorizontalLayout();
                card.setWidthFull();
                card.setSpacing(true);
                card.setPadding(true);
                card.getStyle()
                    .set("border", "1px solid var(--lumo-contrast-10pct)")
                    .set("border-radius", "8px")
                    .set("margin-bottom", "8px")
                    .set("cursor", "pointer");
                
                // Cuadrado de color según tipo de tarea
                Div colorIndicator = new Div();
                colorIndicator.setHeight("40px");
                colorIndicator.setWidth("40px");
                
                // Usar el color de la zona si está disponible
                String colorBorde = zona.getColorBorde();
                if (colorBorde == null && zona.getTarea() != null && zona.getTarea().getTipo() != null) {
                    colorBorde = getColorForTaskType(zona.getTarea().getTipo());
                }
                if (colorBorde == null) colorBorde = "#3388ff"; // Color por defecto
                
                colorIndicator.getStyle()
                    .set("background-color", colorBorde)
                    .set("border-radius", "4px")
                    .set("margin-right", "16px");
                
                // Información de la zona
                VerticalLayout zonaInfo = new VerticalLayout();
                zonaInfo.setPadding(false);
                zonaInfo.setSpacing(false);
                
                H4 nombreZona = new H4(zona.getNombre());
                nombreZona.getStyle().set("margin", "0").set("margin-bottom", "4px");
                
                // Añadir información de la tarea si está disponible
                Paragraph tareaInfo = null;
                try {
                    if (zona.getTarea() != null) {
                        String tipoTarea = zona.getTarea().getTipo() != null 
                                          ? zona.getTarea().getTipo().name() 
                                          : "No especificado";
                        tareaInfo = new Paragraph("Tarea: " + zona.getTarea().getNombre() + " (" + tipoTarea + ")");
                    } else {
                        tareaInfo = new Paragraph("No hay tarea asociada");
                    }
                    tareaInfo.getStyle().set("margin", "0").set("margin-top", "4px");
                } catch (Exception e) {
                    tareaInfo = new Paragraph("Error al cargar información de tarea");
                    tareaInfo.getStyle().set("color", "var(--lumo-error-color)");
                }
                
                zonaInfo.add(nombreZona, tareaInfo);
                card.add(colorIndicator, zonaInfo);
                
                // Al hacer clic, mostrar detalles completos
                final ZonaEncuentro zonaFinal = zona;
                card.addClickListener(e -> {
                    dialog.close();
                    zonaSeleccionada = zonaFinal;
                    mostrarDetallesZona(300, 300); // Posición central aproximada
                });
                
                content.add(card);
            }
        }
        
        Button cerrarBtn = new Button("Cerrar", e -> dialog.close());
        cerrarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        // Si hay zonas y el usuario es gestor, añadir botón para editar
        if (!zonas.isEmpty() && esGestor) {
            Button btnCrearZona = new Button("Crear Nueva Zona", e -> {
                dialog.close();
                mostrarDialogoSeleccionTarea();
            });
            btnCrearZona.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialog.getFooter().add(btnCrearZona);
        }
        
        dialog.getFooter().add(cerrarBtn);
        dialog.add(content);
        dialog.open();
    }
}

