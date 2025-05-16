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

import com.vaadin.flow.component.dialog.Dialog;
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
                    // Extraer coordenadas
                    double[] coords = geolocalizacionServicio.extraerCoordenadas(tarea.getLocalizacion());
                    double lat = coords[0];
                    double lng = coords[1];
                    
                    // Crear marcador según el tipo de tarea
                    LCircleMarker marker = new LCircleMarker(registry, new LLatLng(registry, lat, lng));
                    
                    // Personalizar apariencia según tipo de tarea
                    marker.setRadius(8);
                    
                    // Asignar color según el tipo de tarea
                    String color = getColorForTaskType(tarea.getTipo());
                    
                    // Aplicamos colores directamente usando funciones de API directas
                    // Nota: En versiones recientes de la biblioteca, estos métodos podrían existir
                    marker.bindTooltip("<strong>" + tarea.getNombre() + "</strong><br/>" +
                                     "Tipo: " + (tarea.getTipo() != null ? tarea.getTipo().name() : "N/A") + "<br/>" +
                                     "Estado: " + (tarea.getEstado() != null ? tarea.getEstado().name() : "N/A") + "<br/>" +
                                     "Voluntarios: " + tarea.getNumeroVoluntariosNecesarios());
                    
                    // Primero agregamos el marcador al mapa para que sea visible
                    marker.addTo(map);
                    stores.add(marker);
                    
                    // Como último recurso, usamos JavaScript para cambiar los colores - utilizando un identificador de posición
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
                    
                    // Crear un identificador único para este polígono
                    final String poligonoId = "zona-" + zona.getId() + "-" + System.currentTimeMillis();
                    
                    // Usar JavaScript para aplicar estilos al polígono, con un retraso para asegurar que está en el DOM
                    UI.getCurrent().getPage().executeJs(
                        "setTimeout(() => {" +
                            "try {" +
                                "const leafletLayers = document.querySelectorAll('.leaflet-interactive');" +
                                "const polygons = Array.from(leafletLayers).filter(el => el.tagName === 'path');" +
                                "if (polygons.length > 0) {" +
                                    "const lastPolygon = polygons[polygons.length - 1];" +
                                    "lastPolygon.setAttribute('stroke', '" + colorBorde + "');" +
                                    "lastPolygon.setAttribute('fill', '" + colorRelleno + "');" +
                                    "lastPolygon.setAttribute('fill-opacity', '0.4');" +
                                    "lastPolygon.setAttribute('stroke-width', '3');" +
                                    "lastPolygon.id = '" + poligonoId + "';" +
                                    "console.log('Estilos aplicados a zona con ID: " + zona.getId() + "');" +
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
        
        sidePanel.add(panelTitle, legendTitle, legend, meetingZoneLegendTitle, meetingZoneLegend, statsTitle, stats, userTitle, userInfo);
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
            showNotification(pointCommand.getDescription(), NotificationVariant.LUMO_PRIMARY);
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
            showNotification(storeCommand.getDescription(), NotificationVariant.LUMO_SUCCESS);
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
            showNotification(areaCriticaCommand.getDescription(), NotificationVariant.LUMO_ERROR);
        });
        
        // Botón de asignar punto de encuentro a tarea
        Button btnAsignarPuntoEncuentro = new Button("Asignar Punto Encuentro", new Icon(VaadinIcon.CONNECT));
        btnAsignarPuntoEncuentro.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_CONTRAST);
        btnAsignarPuntoEncuentro.addClickListener(e -> {
            mostrarDialogoSeleccionTarea();
        });
        
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

        controls.add(gestorLabel, btnPoint, btnStore, btnVol, btnAreaCritica, btnAsignarPuntoEncuentro, btnClear);
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
            showNotification("Selecciona primero una acción antes de hacer clic en el mapa",
                             NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearMap() {
        markers.forEach(m -> map.removeLayer(m)); markers.clear();
        circles.forEach(c -> map.removeLayer(c)); circles.clear();
        stores.forEach(s -> map.removeLayer(s)); stores.clear();
        
        // Solo limpiamos áreas críticas temporales, no las zonas de encuentro persistentes
        if (areasCriticas != null) {
            areasCriticas.forEach(a -> map.removeLayer(a)); 
            areasCriticas.clear();
        }
        
        // Si hay un botón de finalizar, lo eliminamos
        if (btnFinalizarZona != null) {
            remove(btnFinalizarZona);
            btnFinalizarZona = null;
            activeCommand = null;
        }
        
        showNotification("Mapa limpiado", NotificationVariant.LUMO_SUCCESS);
        
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
        if (!esGestor) return; // Solo los gestores pueden editar zonas
        
        // Usar JavaScript para detectar clics en las zonas (polígonos)
        UI.getCurrent().getPage().executeJs(
            "setTimeout(() => {" +
                "const setupPolygonListeners = () => {" +
                    "const layers = document.querySelectorAll('.leaflet-overlay-pane .leaflet-interactive');" +
                    "layers.forEach(layer => {" +
                        "if (layer.tagName === 'path' && !layer.hasAttribute('data-has-listener')) {" +
                            "layer.setAttribute('data-has-listener', 'true');" +
                            "layer.addEventListener('click', (event) => {" +
                                "if (event.ctrlKey || event.metaKey) {" + // Requiere tecla Ctrl/Cmd para activar menú
                                    "const rect = layer.getBoundingClientRect();" +
                                    "const polygonId = layer.id;" +
                                    "document.getElementById('" + getId().orElse("main-view") + "').$server.zonaClicked(" +
                                        "polygonId, event.clientX, event.clientY);" +
                                    "event.stopPropagation();" + // Prevenir que el mapa reciba el clic
                                "}" +
                            "});" +
                        "}" +
                    "});" +
                "};" +
                
                "setupPolygonListeners();" +
                
                "// Configurar observador para detectar nuevos polígonos dinamicamente" +
                "const observer = new MutationObserver(setupPolygonListeners);" +
                "const mapPane = document.querySelector('.leaflet-overlay-pane');" +
                "if (mapPane) {" +
                    "observer.observe(mapPane, { childList: true, subtree: true });" +
                "}" +
            "}, 1000);"
        );
    }
    
    @ClientCallable
    public void zonaClicked(String polygonId, double clientX, double clientY) {
        if (!esGestor) return;
        
        try {
            // Extraer el ID de la zona del ID del polígono
            // Formato esperado: zona-{id}-{timestamp}
            String[] partes = polygonId.split("-");
            if (partes.length >= 2) {
                Long zonaId = Long.parseLong(partes[1]);
                Optional<ZonaEncuentro> optZona = zonaEncuentroServicio.obtenerZonaEncuentroPorId(zonaId);
                
                if (optZona.isPresent()) {
                    zonaSeleccionada = optZona.get();
                    mostrarMenuContextualZona(clientX, clientY);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al procesar clic en zona: " + e.getMessage());
        }
    }
    
    /**
     * Muestra un menú contextual para editar/eliminar una zona
     */
    private void mostrarMenuContextualZona(double x, double y) {
        if (zonaSeleccionada == null) return;
        
        Dialog contextMenu = new Dialog();
        contextMenu.setWidth("250px");
        contextMenu.getElement().getStyle().set("box-shadow", "0 0 10px rgba(0,0,0,0.5)");
        contextMenu.getElement().getStyle().set("padding", "0");
        contextMenu.getElement().getStyle().set("border-radius", "8px");
        
        // Posicionar el menú en las coordenadas del clic
        contextMenu.getElement().getStyle().set("position", "fixed");
        contextMenu.getElement().getStyle().set("left", x + "px");
        contextMenu.getElement().getStyle().set("top", y + "px");
        
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        
        // Título del menú
        H4 titulo = new H4("Zona: " + zonaSeleccionada.getNombre());
        titulo.getStyle().set("margin", "10px").set("padding", "0");
        content.add(titulo);
        
        // Información de la tarea asociada
        Paragraph tareaInfo = new Paragraph("Tarea: " + zonaSeleccionada.getNombreTareaSeguro());
        tareaInfo.getStyle().set("margin", "0 10px 10px 10px").set("color", "var(--lumo-secondary-text-color)");
        content.add(tareaInfo);
        
        // Mostrar coordenadas
        Paragraph coordInfo = new Paragraph("Puntos: " + zonaSeleccionada.getCoordenadaComoLista().size());
        coordInfo.getStyle().set("margin", "0 10px 15px 10px").set("font-size", "0.85em").set("color", "var(--lumo-tertiary-text-color)");
        content.add(coordInfo);
        
        // Botones de acciones
        Button btnEditar = new Button("Editar zona", new Icon(VaadinIcon.EDIT));
        btnEditar.setWidthFull();
        btnEditar.getStyle().set("text-align", "left").set("border-radius", "0");
        btnEditar.addClickListener(e -> {
            iniciarEdicionZona();
            contextMenu.close();
        });
        
        Button btnEliminar = new Button("Eliminar zona", new Icon(VaadinIcon.TRASH));
        btnEliminar.setWidthFull();
        btnEliminar.getStyle().set("text-align", "left").set("border-radius", "0");
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        btnEliminar.addClickListener(e -> {
            confirmarEliminarZona();
            contextMenu.close();
        });
        
        Button btnCerrar = new Button("Cerrar", new Icon(VaadinIcon.CLOSE));
        btnCerrar.setWidthFull();
        btnCerrar.getStyle().set("text-align", "left").set("border-radius", "0");
        btnCerrar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnCerrar.addClickListener(e -> contextMenu.close());
        
        content.add(btnEditar, btnEliminar, btnCerrar);
        contextMenu.add(content);
        contextMenu.open();
        
        // Cerrar el menú si se hace clic fuera
        UI.getCurrent().getPage().executeJs(
            "const closeDialogOnOutsideClick = (e) => {" +
                "const dialog = document.querySelector('vaadin-dialog-overlay');" +
                "if (dialog && !dialog.contains(e.target)) {" +
                    "document.removeEventListener('click', closeDialogOnOutsideClick);" +
                    "$0.$server.close();" +
                "}" +
            "};" +
            "setTimeout(() => {" +
                "document.addEventListener('click', closeDialogOnOutsideClick);" +
            "}, 300);", contextMenu.getElement());
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Inicializar listeners para zonas al cargar la vista
        if (esGestor) {
            initZonaListeners();
        }
    }
}
