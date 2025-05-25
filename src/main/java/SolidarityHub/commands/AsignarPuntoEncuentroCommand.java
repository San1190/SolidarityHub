package SolidarityHub.commands;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.vector.LCircleMarker;
import software.xdev.vaadin.maps.leaflet.layer.vector.LPolyline;
import software.xdev.vaadin.maps.leaflet.layer.vector.LPolygon;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;

import java.util.ArrayList;
import java.util.List;

import SolidarityHub.models.Necesidad.Urgencia;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.ZonaEncuentro;
import SolidarityHub.services.TareaServicio;
import SolidarityHub.services.ZonaEncuentroServicio;

/**
 * Comando para asignar puntos de encuentro a una tarea.
 * Permite crear una zona con múltiples puntos que se conectan.
 */
public class AsignarPuntoEncuentroCommand implements MapCommand {
    
    private final TareaServicio tareaServicio;
    private final ZonaEncuentroServicio zonaEncuentroServicio;
    private final Tarea tareaSeleccionada;
    private final List<LLatLng> puntos = new ArrayList<>();
    private final List<LCircleMarker> marcadores = new ArrayList<>();
    private final List<double[]> coordenadas = new ArrayList<>();
    private LPolyline zona;
    private LPolygon poligono;
    private final LMap map;
    private final LComponentManagementRegistry registry;
    
    /**
     * Constructor que recibe la tarea a la que se asignará el punto de encuentro.
     * 
     * @param tareaServicio Servicio para actualizar la tarea
     * @param zonaEncuentroServicio Servicio para gestionar zonas de encuentro
     * @param tareaSeleccionada Tarea a la que se asignará el punto de encuentro
     * @param map Referencia al mapa
     * @param registry Registro de componentes
     */
    public AsignarPuntoEncuentroCommand(TareaServicio tareaServicio, 
                                        ZonaEncuentroServicio zonaEncuentroServicio,
                                        Tarea tareaSeleccionada, 
                                        LMap map, LComponentManagementRegistry registry) {
        this.tareaServicio = tareaServicio;
        this.zonaEncuentroServicio = zonaEncuentroServicio;
        this.tareaSeleccionada = tareaSeleccionada;
        this.map = map;
        this.registry = registry;
    }
    
    @Override
    public void execute(LMap map, LComponentManagementRegistry registry, double lat, double lng) {
        // Crear punto y añadirlo a la lista
        LLatLng punto = new LLatLng(registry, lat, lng);
        puntos.add(punto);
        coordenadas.add(new double[]{lat, lng});
        
        // Añadir marcador
        LCircleMarker marcador = new LCircleMarker(registry, punto);
        marcador.setRadius(4);
        marcador.bindTooltip("Punto " + puntos.size() + " de zona");
        marcador.addTo(map);
        marcadores.add(marcador);
        
        // Si hay al menos 2 puntos, crear o actualizar la línea
        if (puntos.size() >= 2) {
            if (zona != null) {
                map.removeLayer(zona);
            }
            
            zona = new LPolyline(registry, puntos.toArray(new LLatLng[0]));
            zona.addTo(map);
        }
        
        // Actualizar el punto de encuentro en la tarea como texto
        StringBuilder coordenadasStr = new StringBuilder();
        for (int i = 0; i < coordenadas.size(); i++) {
            if (i > 0) coordenadasStr.append(";");
            double[] coord = coordenadas.get(i);
            coordenadasStr.append(coord[0]).append(",").append(coord[1]);
        }
        
        tareaSeleccionada.setPuntoEncuentro(coordenadasStr.toString());
        
        // Guardar la tarea con la actualización
        tareaServicio.actualizarTarea(tareaSeleccionada);
        
        Notification.show("Punto " + puntos.size() + " añadido a la zona de encuentro para: " + 
                          tareaSeleccionada.getNombre(), 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    /**
     * Finaliza la zona creando un polígono cerrado y aplicando el color según la urgencia
     */
    public void finalizarZona() {
        if (puntos.size() < 3) {
            Notification.show("Se necesitan al menos 3 puntos para crear una zona cerrada", 
                             3000, Notification.Position.MIDDLE)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        // Eliminar la línea abierta si existe
        if (zona != null) {
            map.removeLayer(zona);
            zona = null;
        }
        
        // Eliminar el polígono anterior si existe
        if (poligono != null) {
            map.removeLayer(poligono);
        }
        
        // Crear el polígono (automáticamente cierra la figura)
        poligono = new LPolygon(registry, puntos.toArray(new LLatLng[0]));
        
        // Aplicar color según la urgencia de la tarea
        String color = getColorByUrgencia(tareaSeleccionada);
        String colorFill = getColorFillByUrgencia(tareaSeleccionada);
        
        // Añadir el polígono al mapa (esto crea el objeto en el navegador)
        poligono.addTo(map);
        
        // Usar JavaScript para encontrar el polígono recién añadido y darle estilo
        com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
            "setTimeout(() => {" +
                "const leafletLayers = document.querySelectorAll('.leaflet-interactive');" +
                "const polygons = Array.from(leafletLayers).filter(el => el.tagName === 'path');" +
                "if (polygons.length > 0) {" +
                    "const lastPolygon = polygons[polygons.length - 1];" +
                    "lastPolygon.setAttribute('stroke', '" + color + "');" +
                    "lastPolygon.setAttribute('fill', '" + colorFill + "');" +
                    "lastPolygon.setAttribute('fill-opacity', '0.4');" +
                    "lastPolygon.setAttribute('stroke-width', '3');" +
                "}" +
            "}, 200);");
        
        poligono.bindTooltip("Zona de encuentro para: " + tareaSeleccionada.getNombre());
        
        // Guardar la última versión de las coordenadas
        StringBuilder coordenadasStr = new StringBuilder();
        for (int i = 0; i < coordenadas.size(); i++) {
            if (i > 0) coordenadasStr.append(";");
            double[] coord = coordenadas.get(i);
            coordenadasStr.append(coord[0]).append(",").append(coord[1]);
        }
        
        // Crear y guardar la zona de encuentro en la base de datos con reintentos
        try {
            // Mostrar mensaje de guardado
            Notification guardarNotification = Notification.show(
                "Guardando zona de encuentro...", 
                10000, 
                Notification.Position.BOTTOM_CENTER);
                
            guardarNotification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            
            // Intento 1
            try {
                ZonaEncuentro zonaEncuentro = zonaEncuentroServicio.crearZonaParaTarea(
                    tareaSeleccionada.getId(),
                    coordenadasStr.toString(),
                    color,
                    colorFill
                );
                
                guardarNotification.close();
                Notification.show("Zona de encuentro creada y guardada en la base de datos con ID: " + 
                                 zonaEncuentro.getId(), 5000, Notification.Position.BOTTOM_START)
                           .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                           
                System.out.println("Zona de encuentro guardada con éxito. ID: " + zonaEncuentro.getId());
                System.out.println("Coordenadas: " + coordenadasStr.toString());
                System.out.println("Colores: " + color + ", " + colorFill);

                // Asignar el ID al polígono
                final Long zonaId = zonaEncuentro.getId();
                final String mainViewId = com.vaadin.flow.component.UI.getCurrent().getElement().getAttribute("id");
                com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
                    "setTimeout(() => {" +
                        "const leafletLayers = document.querySelectorAll('.leaflet-interactive');" +
                        "const polygons = Array.from(leafletLayers).filter(el => el.tagName === 'path');" +
                        "if (polygons.length > 0) {" +
                            "const lastPolygon = polygons[polygons.length - 1];" +
                            "lastPolygon.id = 'zona-" + zonaId + "';" +
                            "lastPolygon.setAttribute('data-zona-id', '" + zonaId + "');" +
                            "lastPolygon.onclick = function(event) {" +
                                "if (event.ctrlKey || event.metaKey) {" +
                                    "document.getElementById('" + mainViewId + "').$server.zonaClicked(" +
                                        "this.id, event.clientX, event.clientY, true);" +
                                "} else {" +
                                    "document.getElementById('" + mainViewId + "').$server.zonaClicked(" +
                                        "this.id, event.clientX, event.clientY, false);" +
                                "}" +
                                "event.stopPropagation();" +
                            "};" +
                        "}" +
                    "}, 500);"
                );
                
            } catch (Exception e) {
                System.err.println("Primer intento fallido: " + e.getMessage());
                // Intento 2 con retraso
                com.vaadin.flow.component.UI.getCurrent().access(() -> {
                    try {
                        Thread.sleep(1000);
                        ZonaEncuentro zonaEncuentro = zonaEncuentroServicio.crearZonaParaTarea(
                            tareaSeleccionada.getId(),
                            coordenadasStr.toString(),
                            color,
                            colorFill
                        );
                        
                        guardarNotification.close();
                        Notification.show("Zona de encuentro guardada en el segundo intento con ID: " + 
                                         zonaEncuentro.getId(), 5000, Notification.Position.BOTTOM_START)
                                   .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    } catch (Exception e2) {
                        // Si falla la creación en la base de datos, al menos actualizar la tarea
                        guardarNotification.close();
                        tareaSeleccionada.setPuntoEncuentro(coordenadasStr.toString());
                        tareaServicio.actualizarTarea(tareaSeleccionada);
                        
                        Notification.show("Zona guardada en tarea, pero hubo problemas al guardar en base de datos: " + 
                                        e2.getMessage(), 5000, Notification.Position.BOTTOM_START)
                                .addThemeVariants(NotificationVariant.LUMO_WARNING);
                        
                        System.err.println("Error al guardar zona de encuentro: " + e2.getMessage());
                        e2.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            // Capturar cualquier excepción que pudiera ocurrir
            System.err.println("Error crítico al guardar zona: " + e.getMessage());
            e.printStackTrace();
            Notification.show("Error al guardar zona: " + e.getMessage(), 
                             5000, Notification.Position.BOTTOM_START)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    /**
     * Obtiene el color de borde según la urgencia de la tarea
     */
    private String getColorByUrgencia(Tarea tarea) {
        if (tarea.getTipo() != null) {
            // Si hay un tipo de necesidad, usar ese color
            return switch (tarea.getTipo()) {
                case PRIMEROS_AUXILIOS -> "#c0392b";
                case MEDICAMENTOS -> "#e67e22";
                case ALIMENTACION -> "#e74c3c";
                case ALIMENTACION_BEBE -> "#d35400";
                case REFUGIO -> "#8e44ad";
                case ROPA -> "#f39c12";
                case SERVICIO_LIMPIEZA -> "#16a085";
                case AYUDA_PSICOLOGICA -> "#3498db";
                case AYUDA_CARPINTERIA -> "#27ae60";
                case AYUDA_ELECTRICIDAD -> "#2980b9";
                case AYUDA_FONTANERIA -> "#2c3e50";
                case MATERIAL_HIGENE -> "#1abc9c";
                default -> "#3388ff";
            };
        }
        
        // Color por defecto si no hay tipo
        return "#3388ff";
    }
    
    /**
     * Obtiene el color de relleno según la urgencia
     */
    private String getColorFillByUrgencia(Tarea tarea) {
        // Si la tarea tiene información de necesidad con urgencia
        if (tarea.getTipoNecesidad() != null) {
            try {
                // Intentar obtener la urgencia asociada
                Urgencia urgencia = Urgencia.MEDIA; // Valor por defecto
                
                // Lógica para determinar la urgencia - esto dependerá de tu modelo de datos
                // En este ejemplo, usamos un enfoque simple
                
                return switch (urgencia) {
                    case ALTA -> "#e74c3c"; // Rojo para urgencia alta
                    case MEDIA -> "#f39c12"; // Naranja para urgencia media
                    case BAJA -> "#2ecc71"; // Verde para urgencia baja
                };
            } catch (Exception e) {
                return "#3498db"; // Azul por defecto si hay error
            }
        }
        
        // Color por defecto
        return "#3498db";
    }
    
    public void limpiarPuntos() {
        // Eliminar marcadores y líneas
        for (LCircleMarker marcador : marcadores) {
            map.removeLayer(marcador);
        }
        if (zona != null) {
            map.removeLayer(zona);
        }
        if (poligono != null) {
            map.removeLayer(poligono);
        }
        
        // Limpiar listas
        puntos.clear();
        marcadores.clear();
        coordenadas.clear();
        zona = null;
        poligono = null;
    }
    
    @Override
    public String getDescription() {
        return "Haz clic en el mapa para añadir puntos a la zona de encuentro para la tarea: " + 
               tareaSeleccionada.getNombre();
    }
} 