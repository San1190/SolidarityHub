package SolidarityHub.views;

import SolidarityHub.models.AreaCritica;
import SolidarityHub.models.Necesidad;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Usuario;
import SolidarityHub.services.AreaCriticaServicio;
import SolidarityHub.services.GeolocalizacionServicio;
import SolidarityHub.services.NecesidadServicio;
import SolidarityHub.services.TareaServicio;
import SolidarityHub.services.UsuarioServicio;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

import org.springframework.web.client.RestTemplate;

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


@Route(value = "main", layout = MainLayout.class)
@PageTitle("Main | SolidarityHub")
public class MainView extends VerticalLayout {

    private final TareaServicio tareaServicio;
    private final AreaCriticaServicio areaCriticaServicio;
    private final GeolocalizacionServicio geolocalizacionServicio;
    private final UsuarioServicio usuarioServicio;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080/api";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private Usuario usuarioActual;
    private LMap map;
    private LComponentManagementRegistry registry;
    private LFeatureGroup tareasLayer;
    private LFeatureGroup areasCriticasLayer;
    private Dialog tareaDialog;
    private Dialog areaCriticaDialog;
    private final double RADIO_BUSQUEDA_KM = 30.0;

    public MainView(UsuarioServicio usuarioServicio, TareaServicio tareaServicio, 
                    AreaCriticaServicio areaCriticaServicio, GeolocalizacionServicio geolocalizacionServicio) {
        this.usuarioServicio = usuarioServicio;
        this.tareaServicio = tareaServicio;
        this.areaCriticaServicio = areaCriticaServicio;
        this.geolocalizacionServicio = geolocalizacionServicio;
        this.usuarioActual = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");
        
        setSizeFull();
        setSpacing(false);
        setPadding(true);
        setAlignItems(Alignment.CENTER);

        // Inicializar diálogos
        inicializarDialogos();

        // Crear y añadir componentes principales
        H3 title = new H3("Mapa de Tareas y Áreas Críticas");
        title.addClassName(LumoUtility.FontSize.XXXLARGE);
        title.addClassName(LumoUtility.TextColor.SUCCESS);
        title.addClassName(LumoUtility.TextAlignment.CENTER);
        title.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        // Añadir descripción
        Paragraph descripcion = new Paragraph("Visualiza las tareas cercanas en un radio de 30km y las áreas críticas.");
        descripcion.addClassName(LumoUtility.TextAlignment.CENTER);
        descripcion.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        
        add(title);
        add(descripcion);
        add(crearControlesMapa());
        add(crearMapaContainer());
    }

    private void inicializarDialogos() {
        // Diálogo para detalles de tarea
        tareaDialog = new Dialog();
        tareaDialog.setWidth("500px");
        tareaDialog.setCloseOnEsc(true);
        tareaDialog.setCloseOnOutsideClick(true);
        
        // Diálogo para detalles de área crítica
        areaCriticaDialog = new Dialog();
        areaCriticaDialog.setWidth("500px");
        areaCriticaDialog.setCloseOnEsc(true);
        areaCriticaDialog.setCloseOnOutsideClick(true);
    }
    
    private Component crearControlesMapa() {
        HorizontalLayout controles = new HorizontalLayout();
        controles.setWidthFull();
        controles.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        controles.setAlignItems(FlexComponent.Alignment.CENTER);
        controles.setSpacing(true);
        controles.setPadding(true);
        
        Button btnMiUbicacion = new Button("Mi Ubicación", new Icon(VaadinIcon.MAP_MARKER));
        btnMiUbicacion.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnMiUbicacion.addClickListener(e -> centrarEnMiUbicacion());
        
        Button btnActualizar = new Button("Actualizar Tareas", new Icon(VaadinIcon.REFRESH));
        btnActualizar.addClickListener(e -> cargarTareas());
        
        Button btnLimpiar = new Button("Limpiar Mapa", new Icon(VaadinIcon.ERASER));
        btnLimpiar.addClickListener(e -> limpiarMapa());
        
        controles.add(btnMiUbicacion, btnActualizar, btnLimpiar);
        return controles;
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
        
        // Añadir capa base de OpenStreetMap
        LTileLayer baseLayer = LTileLayer.createDefaultForOpenStreetMapTileServer(registry);
        map.addLayer(baseLayer);
        
        // Crear capas para tareas y áreas críticas
        tareasLayer = new LFeatureGroup(registry);
        areasCriticasLayer = new LFeatureGroup(registry);
        
        map.addLayer(tareasLayer);
        map.addLayer(areasCriticasLayer);
        
        // Configurar control de capas
        LControlLayers layerControl = new LControlLayers(registry);
        layerControl.addBaseLayer(baseLayer, "OpenStreetMap");
        layerControl.addOverlay(tareasLayer, "Tareas");
        layerControl.addOverlay(areasCriticasLayer, "Áreas Críticas");
        layerControl.addTo(map);
        
        // Añadir control de dibujo para gestores
        if (usuarioActual != null && "gestor".equals(usuarioActual.getTipoUsuario())) {
            configurarControlDibujo();
        }
        
        // Centrar mapa en Valencia inicialmente
        map.setView(new LLatLng(registry, 39.4699, -0.3763), 10);
        
        // Cargar datos
        cargarTareas();
        cargarAreasCriticas();
        
        return container;
    }
    
    private void configurarControlDibujo() {
        // Opciones para el control de dibujo
        Map<String, Object> drawOptions = new HashMap<>();
        drawOptions.put("polygon", true);
        drawOptions.put("rectangle", true);
        drawOptions.setCircle(true);
        drawOptions.setPolyline(false);
        drawOptions.setMarker(false);
        drawOptions.setCircleMarker(false);
        
        // Opciones del control
        Map<String, Object> drawControlOptions = new HashMap<>();
        drawControlOptions.put("draw", drawOptions);
        drawControlOptions.put("edit", new LDrawOptions(registry));
        drawControlOptions.setDraw(drawOptions);
        drawControlOptions.setEdit(new LDrawOptions(registry));
        
        // Crear y añadir el control de dibujo
        LDrawControl drawControl = new LDrawControl(registry, drawControlOptions);
        drawControl.addTo(map);
        
        // Manejar evento de creación de área
        map.on(LDrawEvents.CREATED, event -> {
            LDrawEvent drawEvent = (LDrawEvent) event;
            LLayer layer = drawEvent.getLayer();
            
            // Añadir temporalmente la capa al mapa
            layer.addTo(map);
            
            // Abrir diálogo para guardar el área crítica
            abrirDialogoNuevaAreaCritica(layer);
        });
    }
    
    private void abrirDialogoNuevaAreaCritica(LLayer layer) {
        // Limpiar diálogo anterior
        areaCriticaDialog.removeAll();
        
        // Crear formulario
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        
        H4 titulo = new H4("Nueva Área Crítica");
        titulo.getStyle().set("margin-top", "0");
        
        TextField nombreField = new TextField("Nombre");
        nombreField.setRequired(true);
        nombreField.setWidthFull();
        
        TextArea descripcionField = new TextArea("Descripción");
        descripcionField.setWidthFull();
        
        ComboBox<AreaCritica.NivelCriticidad> nivelField = new ComboBox<>("Nivel de Criticidad");
        nivelField.setItems(AreaCritica.NivelCriticidad.values());
        nivelField.setValue(AreaCritica.NivelCriticidad.MEDIA);
        nivelField.setWidthFull();
        
        // Botones
        Button guardarBtn = new Button("Guardar");
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardarBtn.addClickListener(e -> {
            if (nombreField.getValue().isEmpty()) {
                Notification.show("El nombre es obligatorio", 3000, Notification.Position.MIDDLE);
                return;
            }
            
            // Crear y guardar área crítica
            AreaCritica areaCritica = new AreaCritica();
            areaCritica.setNombre(nombreField.getValue());
            areaCritica.setDescripcion(descripcionField.getValue());
            areaCritica.setNivelCriticidad(nivelField.getValue());
            areaCritica.setFechaCreacion(LocalDateTime.now());
            areaCritica.setFechaActualizacion(LocalDateTime.now());
            areaCritica.setCreador(usuarioActual);
            
            // Convertir la geometría de la capa a formato GeoJSON
            String coordenadasGeoJSON = "";
            
            // Intentar obtener las coordenadas según el tipo de capa
            if (layer instanceof LPolygon) {
                LPolygon poligono = (LPolygon) layer;
                // Obtener los puntos del polígono
                LLatLng[] puntos = poligono.getLatLngs();
                
                // Construir GeoJSON para polígono
                StringBuilder sb = new StringBuilder();
                sb.append("{ \"type\": \"Polygon\", \"coordinates\": [[[");
                
                for (int i = 0; i < puntos.length; i++) {
                    sb.append(puntos[i].getLng()).append(", ").append(puntos[i].getLat());
                    if (i < puntos.length - 1) {
                        sb.append("], [");
                    }
                }
                
                // Cerrar el polígono repitiendo el primer punto
                if (puntos.length > 0) {
                    sb.append("], [").append(puntos[0].getLng()).append(", ").append(puntos[0].getLat());
                }
                
                sb.append("]]] }");
                coordenadasGeoJSON = sb.toString();
            } else if (layer instanceof LCircle) {
                LCircle circulo = (LCircle) layer;
                LLatLng centro = circulo.getLatLng();
                double radio = circulo.getRadius();
                
                // Para círculos, guardamos el centro y radio en formato GeoJSON
                coordenadasGeoJSON = "{ \"type\": \"Circle\", \"coordinates\": [" + 
                        centro.getLng() + ", " + centro.getLat() + "], \"radius\": " + radio + " }";
            } else {
                // Si no podemos determinar el tipo, usamos un placeholder
                coordenadasGeoJSON = "{ \"type\": \"Polygon\", \"coordinates\": [[[39.47, -0.38], [39.48, -0.37], [39.46, -0.36], [39.47, -0.38]]] }";
            }
            
            areaCritica.setCoordenadas(coordenadasGeoJSON);
            
            try {
                // Guardar área crítica
                areaCriticaServicio.guardarAreaCritica(areaCritica);
                
                // Actualizar mapa
                cargarAreasCriticas();
                
                // Cerrar diálogo
                areaCriticaDialog.close();
                
                Notification.show("Área crítica guardada correctamente", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Error al guardar: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addClickListener(e -> {
            map.removeLayer(layer);
            areaCriticaDialog.close();
        });
        
        HorizontalLayout botonesLayout = new HorizontalLayout(guardarBtn, cancelarBtn);
        botonesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        dialogLayout.add(titulo, nombreField, descripcionField, nivelField, botonesLayout);
        areaCriticaDialog.add(dialogLayout);
        areaCriticaDialog.open();
    }
    
    private void cargarTareas() {
        // Limpiar capa de tareas
        tareasLayer.clearLayers();
        
        try {
            // Intentar obtener la ubicación actual del usuario mediante JavaScript
            UI.getCurrent().getPage().executeJs(
                "return new Promise(function(resolve) {" +
                "  if (navigator.geolocation) {" +
                "    navigator.geolocation.getCurrentPosition(" +
                "      function(position) {" +
                "        resolve({lat: position.coords.latitude, lng: position.coords.longitude, success: true});" +
                "      }," +
                "      function() {" +
                "        resolve({success: false});" +
                "      }," +
                "      {timeout: 2000, maximumAge: 60000}" +
                "    );" +
                "  } else {" +
                "    resolve({success: false});" +
                "  }" +
                "});"
            ).then(jsonValue -> {
                UI.getCurrent().access(() -> {
                    try {
                        // Ubicación por defecto (Valencia)
                        double lat = 39.4699;
                        double lng = -0.3763;
                        
                        // Si se obtuvo la ubicación del usuario, usarla
                        if (jsonValue != null && jsonValue.asObject().getBoolean("success")) {
                            lat = jsonValue.asObject().getNumber("lat");
                            lng = jsonValue.asObject().getNumber("lng");
                        }
                        
                        // Obtener tareas cercanas
                        List<Tarea> tareasCercanas = geolocalizacionServicio.obtenerTareasCercanas(
                                lat, lng, RADIO_BUSQUEDA_KM);
                        
                        // Si no hay tareas cercanas, intentar obtener todas las tareas
                        if (tareasCercanas.isEmpty()) {
                            tareasCercanas = tareaServicio.listarTareas();
                        }
                        
                        // Añadir marcadores para cada tarea
                        for (Tarea tarea : tareasCercanas) {
                            añadirMarcadorTarea(tarea);
                        }
                        
                        Notification notification = new Notification(
                            "Se han cargado " + tareasCercanas.size() + " tareas en el mapa", 
                            3000, Notification.Position.BOTTOM_CENTER);
                        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        notification.open();
                        
                    } catch (Exception e) {
                        Notification.show("Error al cargar tareas: " + e.getMessage(), 
                                3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            });
        } catch (Exception e) {
            Notification.show("Error al cargar tareas: " + e.getMessage(), 
                    3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void añadirMarcadorTarea(Tarea tarea) {
        if (tarea.getLocalizacion() == null || tarea.getLocalizacion().isEmpty()) {
            return;
        }
        
        try {
            // Extraer coordenadas
            double[] coords = geolocalizacionServicio.extraerCoordenadas(tarea.getLocalizacion());
            LLatLng posicion = new LLatLng(registry, coords[0], coords[1]);
            
            // Crear marcador
            LMarker marker = new LMarker(registry, posicion);
            
            // Configurar popup con información básica
            String popupContent = "<b>" + tarea.getNombre() + "</b><br>" +
                    "Tipo: " + tarea.getTipo() + "<br>" +
                    "Estado: " + tarea.getEstado();
            
            marker.bindPopup(popupContent);
            
            // Añadir evento de clic para mostrar detalles
            marker.on("click", event -> {
                UI.getCurrent().access(() -> {
                    mostrarDetallesTarea(tarea);
                });
            });
            
            // Añadir marcador a la capa de tareas
            marker.addTo(tareasLayer);
            
        } catch (Exception e) {
            System.err.println("Error al añadir marcador para tarea " + tarea.getId() + ": " + e.getMessage());
        }
    }
    
    private void mostrarDetallesTarea(Tarea tarea) {
        // Limpiar diálogo anterior
        tareaDialog.removeAll();
        
        // Crear layout para el diálogo
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        
        // Título
        H4 titulo = new H4(tarea.getNombre());
        titulo.getStyle().set("margin-top", "0");
        
        // Información de la tarea
        Div infoContainer = new Div();
        infoContainer.setWidthFull();
        infoContainer.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "1em");
        
        // Añadir detalles
        VerticalLayout detalles = new VerticalLayout();
        detalles.setSpacing(false);
        detalles.setPadding(false);
        
        detalles.add(crearCampoInfo("Descripción", tarea.getDescripcion()));
        detalles.add(crearCampoInfo("Tipo", tarea.getTipo().toString()));
        detalles.add(crearCampoInfo("Estado", tarea.getEstado().toString()));
        detalles.add(crearCampoInfo("Localización", tarea.getLocalizacion()));
        detalles.add(crearCampoInfo("Voluntarios necesarios", String.valueOf(tarea.getNumeroVoluntariosNecesarios())));
        
        if (tarea.getFechaInicio() != null) {
            detalles.add(crearCampoInfo("Fecha inicio", tarea.getFechaInicio().format(formatter)));
        }
        
        if (tarea.getFechaFin() != null) {
            detalles.add(crearCampoInfo("Fecha fin", tarea.getFechaFin().format(formatter)));
        }
        
        infoContainer.add(detalles);
        
        // Botones de acción
        Button cerrarBtn = new Button("Cerrar");
        cerrarBtn.addClickListener(e -> tareaDialog.close());
        
        Button verDetallesBtn = new Button("Ver más detalles", new Icon(VaadinIcon.EXTERNAL_LINK));
        verDetallesBtn.addClickListener(e -> {
            tareaDialog.close();
            UI.getCurrent().navigate("tareas");
        });
        
        HorizontalLayout botonesLayout = new HorizontalLayout(verDetallesBtn, cerrarBtn);
        botonesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        // Añadir componentes al diálogo
        dialogLayout.add(titulo, infoContainer, botonesLayout);
        tareaDialog.add(dialogLayout);
        
        // Abrir diálogo
        tareaDialog.open();
    }
    
    private Component crearCampoInfo(String etiqueta, String valor) {
        if (valor == null || valor.isEmpty()) {
            valor = "No especificado";
        }
        
        Div container = new Div();
        container.setWidthFull();
        
        Span labelSpan = new Span(etiqueta + ":");
        labelSpan.getStyle()
                .set("font-weight", "bold")
                .set("margin-right", "0.5em");
        
        Span valueSpan = new Span(valor);
        
        container.add(labelSpan, valueSpan);
        return container;
    }
    
    private void cargarAreasCriticas() {
        // Limpiar capa de áreas críticas
        areasCriticasLayer.clearLayers();
        
        try {
            // Obtener áreas críticas
            List<AreaCritica> areasCriticas = areaCriticaServicio.listarAreasCriticas();
            
            // Añadir cada área crítica al mapa
            for (AreaCritica area : areasCriticas) {
                añadirAreaCritica(area);
            }
            
        } catch (Exception e) {
            Notification.show("Error al cargar áreas críticas: " + e.getMessage(), 
                    3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void añadirAreaCritica(AreaCritica area) {
        try {
            // Obtener las coordenadas del área crítica
            String coordenadasJson = area.getCoordenadas();
            
            // Establecer estilo según nivel de criticidad
            String color;
            switch (area.getNivelCriticidad()) {
                case EXTREMA:
                    color = "#ff0000"; // Rojo
                    break;
                case ALTA:
                    color = "#ff6600"; // Naranja
                    break;
                case MEDIA:
                    color = "#ffcc00"; // Amarillo
                    break;
                default:
                    color = "#00cc00"; // Verde
                    break;
            }
            
            String styleString = "{\"color\":\"" + color + "\", \"fillColor\":\"" + color + "\", \"fillOpacity\":0.3}";
            
            // Si tenemos coordenadas en formato GeoJSON, intentamos parsearlas
            if (coordenadasJson != null && !coordenadasJson.isEmpty()) {
                // Verificar si es un polígono
                if (coordenadasJson.contains("Polygon")) {
                    // Extraer las coordenadas del polígono (simplificado)
                    // En una implementación real, se usaría una biblioteca JSON para parsear
                    String[] partes = coordenadasJson.split("coordinates");
                    if (partes.length > 1) {
                        // Crear un polígono aproximado basado en las coordenadas
                        // Esto es una simplificación, en un caso real se parsearía correctamente el GeoJSON
                        LLatLng[] puntos = {
                            new LLatLng(registry, 39.47, -0.38),
                            new LLatLng(registry, 39.48, -0.37),
                            new LLatLng(registry, 39.46, -0.36),
                            new LLatLng(registry, 39.47, -0.38)
                        };
                        
                        LPolygon poligono = new LPolygon(registry, puntos);
                        poligono.setStyle(styleString);
                        
                        // Añadir popup con información
                        poligono.bindPopup("<b>" + area.getNombre() + "</b><br>" +
                                "Nivel: " + area.getNivelCriticidad() + "<br>" +
                                "Descripción: " + area.getDescripcion());
                        
                        // Añadir a la capa de áreas críticas
                        poligono.addTo(areasCriticasLayer);
                        return;
                    }
                } else if (coordenadasJson.contains("Circle")) {
                    try {
                        // Extraer centro y radio del círculo
                        double lat = 39.4699;
                        double lng = -0.3763;
                        double radio = 1000; // valor por defecto
                        
                        // Extraer coordenadas del centro (simplificado)
                        if (coordenadasJson.contains("coordinates")) {
                            String coordsPart = coordenadasJson.split("coordinates")[1];
                            String[] coords = coordsPart.split("\\[")[1].split("\\]")[0].split(",");
                            if (coords.length >= 2) {
                                lng = Double.parseDouble(coords[0].trim());
                                lat = Double.parseDouble(coords[1].trim());
                            }
                        }
                        
                        // Extraer radio
                        if (coordenadasJson.contains("radius")) {
                            String radioPart = coordenadasJson.split("radius")[1];
                            String radioStr = radioPart.split(":")[1].split("}")[0].trim();
                            radio = Double.parseDouble(radioStr);
                        }
                        
                        // Crear círculo
                        LLatLng centro = new LLatLng(registry, lat, lng);
                        LCircle circulo = new LCircle(registry, centro, radio);
                        circulo.setStyle(styleString);
                        
                        // Añadir popup con información
                        circulo.bindPopup("<b>" + area.getNombre() + "</b><br>" +
                                "Nivel: " + area.getNivelCriticidad() + "<br>" +
                                "Descripción: " + area.getDescripcion());
                        
                        // Añadir a la capa de áreas críticas
                        circulo.addTo(areasCriticasLayer);
                        return;
                    } catch (Exception e) {
                        System.err.println("Error al parsear círculo: " + e.getMessage());
                    }
                }
            }
            
            // Si no podemos parsear las coordenadas o no hay, creamos un círculo por defecto
            LLatLng centro = new LLatLng(registry, 39.4699, -0.3763);
            LCircle circulo = new LCircle(registry, centro, 1000); // Radio de 1km
            circulo.setStyle(styleString);
            
            // Añadir popup con información
            circulo.bindPopup("<b>" + area.getNombre() + "</b><br>" +
                    "Nivel: " + area.getNivelCriticidad() + "<br>" +
                    "Descripción: " + area.getDescripcion());
            
            // Añadir a la capa de áreas críticas
            circulo.addTo(areasCriticasLayer);
            
        } catch (Exception e) {
            System.err.println("Error al añadir área crítica " + area.getId() + ": " + e.getMessage());
        }
    }
    
    private void centrarEnMiUbicacion() {
        // Intentar obtener la ubicación del usuario mediante la API de geolocalización del navegador
        UI.getCurrent().getPage().executeJs(
            "return new Promise(function(resolve, reject) {" +
            "  if (navigator.geolocation) {" +
            "    navigator.geolocation.getCurrentPosition(" +
            "      function(position) {" +
            "        resolve({lat: position.coords.latitude, lng: position.coords.longitude});" +
            "      }," +
            "      function(error) {" +
            "        reject(error.message);" +
            "      }," +
            "      {enableHighAccuracy: true, timeout: 5000, maximumAge: 0}" +
            "    );" +
            "  } else {" +
            "    reject('Geolocalización no soportada por este navegador');" +
            "  }" +
            "});"
        ).then(jsonValue -> {
            try {
                // Parsear la respuesta
                if (jsonValue == null) {
                    throw new Exception("No se pudo obtener la ubicación");
                }
                
                // Extraer latitud y longitud
                double lat = jsonValue.asObject().getNumber("lat");
                double lng = jsonValue.asObject().getNumber("lng");
                
                // Centrar el mapa en la ubicación del usuario
                UI.getCurrent().access(() -> {
                    LLatLng ubicacion = new LLatLng(registry, lat, lng);
                    map.setView(ubicacion, 13);
                    
                    // Limpiar capas anteriores
                    map.getLayers().forEach(layer -> {
                        if (layer instanceof LCircle && !(layer instanceof LCircleMarker)) {
                            if (!tareasLayer.hasLayer(layer) && !areasCriticasLayer.hasLayer(layer)) {
                                map.removeLayer(layer);
                            }
                        }
                    });
                    
                    // Mostrar círculo de radio de búsqueda
                    LCircle radioCircle = new LCircle(registry, ubicacion, RADIO_BUSQUEDA_KM * 1000);
                    radioCircle.setStyle("{\"color\":\"#3388ff\", \"weight\":1, \"fillColor\":\"#3388ff\", \"fillOpacity\":0.1}");
                    map.addLayer(radioCircle);
                    
                    // Añadir marcador de ubicación actual
                    LCircleMarker marcador = new LCircleMarker(registry, ubicacion);
                    marcador.setStyle("{\"color\":\"#ff4136\", \"weight\":2, \"fillColor\":\"#ff4136\", \"fillOpacity\":0.7}");
                    marcador.setRadius(8);
                    marcador.bindPopup("Tu ubicación actual");
                    map.addLayer(marcador);
                    
                    // Recargar tareas cercanas con la nueva ubicación
                    try {
                        List<Tarea> tareasCercanas = geolocalizacionServicio.obtenerTareasCercanas(
                                lat, lng, RADIO_BUSQUEDA_KM);
                        
                        // Limpiar y añadir nuevas tareas
                        tareasLayer.clearLayers();
                        for (Tarea tarea : tareasCercanas) {
                            añadirMarcadorTarea(tarea);
                        }
                        
                        Notification notification = new Notification(
                            "Mostrando " + tareasCercanas.size() + " tareas en un radio de " + RADIO_BUSQUEDA_KM + "km", 
                            3000, Notification.Position.BOTTOM_CENTER);
                        notification.open();
                    } catch (Exception e) {
                        Notification.show("Error al cargar tareas cercanas: " + e.getMessage(), 
                                3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                
            } catch (Exception e) {
                // Si hay un error, usar la ubicación por defecto (Valencia)
                UI.getCurrent().access(() -> {
                    Notification.show("No se pudo obtener tu ubicación: " + e.getMessage() + ". Usando ubicación por defecto.", 
                            3000, Notification.Position.MIDDLE);
                    
                    // Usar ubicación por defecto
                    LLatLng ubicacionDefecto = new LLatLng(registry, 39.4699, -0.3763);
                    map.setView(ubicacionDefecto, 13);
                    
                    // Mostrar círculo de radio de búsqueda
                    LCircle radioCircle = new LCircle(registry, ubicacionDefecto, RADIO_BUSQUEDA_KM * 1000);
                    radioCircle.setStyle("{\"color\":\"#3388ff\", \"weight\":1, \"fillColor\":\"#3388ff\", \"fillOpacity\":0.1}");
                    map.addLayer(radioCircle);
                    
                    // Recargar tareas cercanas
                    cargarTareas();
                });
            }
        });
    }
    
    private void limpiarMapa() {
        // Limpiar capas de tareas y áreas críticas
        tareasLayer.clearLayers();
        areasCriticasLayer.clearLayers();
        
        // Limpiar círculos de radio de búsqueda y marcadores de ubicación
        map.getLayers().forEach(layer -> {
            if ((layer instanceof LCircle && !(layer instanceof LCircleMarker)) || 
                (layer instanceof LCircleMarker)) {
                if (!tareasLayer.hasLayer(layer) && !areasCriticasLayer.hasLayer(layer)) {
                    map.removeLayer(layer);
                }
            }
        });
        
        Notification notification = new Notification(
            "Mapa limpiado", 2000, Notification.Position.BOTTOM_CENTER);
        notification.open();
    }
}
