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
 * Comando para editar zonas de encuentro existentes.
 * Permite modificar polígonos añadiendo o eliminando puntos.
 */
public class EditarZonaEncuentroCommand implements MapCommand {
    
    private final TareaServicio tareaServicio;
    private final ZonaEncuentroServicio zonaEncuentroServicio;
    private final Tarea tareaAsociada;
    private final List<LLatLng> puntos = new ArrayList<>();
    private final List<LCircleMarker> marcadores = new ArrayList<>();
    private final List<double[]> coordenadas = new ArrayList<>();
    private LPolyline lineaTemporal;
    private LPolygon poligonoTemporal;
    private LPolygon poligonoOriginal;
    private final LMap map;
    private final LComponentManagementRegistry registry;
    private final ZonaEncuentro zonaOriginal;
    
    /**
     * Constructor para editar una zona existente
     * 
     * @param tareaServicio Servicio para gestionar tareas
     * @param zonaEncuentroServicio Servicio para gestionar zonas de encuentro
     * @param tareaAsociada Tarea asociada a la zona
     * @param map Referencia al mapa
     * @param registry Registro de componentes
     * @param zonaOriginal Zona de encuentro original a editar
     */
    public EditarZonaEncuentroCommand(TareaServicio tareaServicio, 
                                     ZonaEncuentroServicio zonaEncuentroServicio,
                                     Tarea tareaAsociada, 
                                     LMap map, LComponentManagementRegistry registry,
                                     ZonaEncuentro zonaOriginal) {
        this.tareaServicio = tareaServicio;
        this.zonaEncuentroServicio = zonaEncuentroServicio;
        this.tareaAsociada = tareaAsociada;
        this.map = map;
        this.registry = registry;
        this.zonaOriginal = zonaOriginal;
        
        // Inicializar con los puntos de la zona original
        cargarPuntosOriginales();
    }
    
    /**
     * Carga los puntos de la zona original para edición
     */
    private void cargarPuntosOriginales() {
        // Limpiar listas por si acaso
        puntos.clear();
        coordenadas.clear();
        
        try {
            // Eliminar el polígono original del mapa
            // Nota: Esto debe manejarse cuidadosamente, ya que el polígono podría 
            // no ser accesible directamente. Podría necesitar implementación específica.
            
            // Obtener coordenadas de la zona original
            List<double[]> coordsList = zonaOriginal.getCoordenadaComoLista();
            
            // Validar que hay suficientes puntos
            if (coordsList.size() < 3) {
                throw new IllegalStateException("La zona debe tener al menos 3 puntos para ser editada");
            }
            
            // Crear marcadores y añadir puntos
            for (double[] coord : coordsList) {
                LLatLng punto = new LLatLng(registry, coord[0], coord[1]);
                puntos.add(punto);
                coordenadas.add(coord);
                
                // Crear marcador visible para cada punto
                LCircleMarker marcador = new LCircleMarker(registry, punto);
                marcador.setRadius(4);
                marcador.bindTooltip("Punto existente");
                marcador.addTo(map);
                marcadores.add(marcador);
            }
            
            // Crear polígono temporal para mostrar durante la edición
            crearPoligonoTemporal();
            
            Notification.show("Zona cargada para edición. Haz clic para añadir nuevos puntos.", 
                             3000, Notification.Position.BOTTOM_START)
                       .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            
        } catch (Exception e) {
            System.err.println("Error al cargar puntos originales: " + e.getMessage());
            Notification.show("Error al cargar la zona para edición: " + e.getMessage(), 
                             5000, Notification.Position.BOTTOM_START)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    /**
     * Crea un polígono temporal para mostrar durante la edición
     */
    private void crearPoligonoTemporal() {
        if (poligonoTemporal != null) {
            map.removeLayer(poligonoTemporal);
        }
        
        if (puntos.size() >= 3) {
            poligonoTemporal = new LPolygon(registry, puntos.toArray(new LLatLng[0]));
            
            // Aplicar estilo de edición (destacado)
            String colorBorde = "#ff9800"; // Naranja
            String colorRelleno = "#ff9800"; // Naranja
            
            poligonoTemporal.addTo(map);
            
            // Usar JavaScript para aplicar estilos que destaquen el polígono en edición
            com.vaadin.flow.component.UI.getCurrent().getPage().executeJs(
                "setTimeout(() => {" +
                    "const leafletLayers = document.querySelectorAll('.leaflet-interactive');" +
                    "const polygons = Array.from(leafletLayers).filter(el => el.tagName === 'path');" +
                    "if (polygons.length > 0) {" +
                        "const lastPolygon = polygons[polygons.length - 1];" +
                        "lastPolygon.setAttribute('stroke', '" + colorBorde + "');" +
                        "lastPolygon.setAttribute('fill', '" + colorRelleno + "');" +
                        "lastPolygon.setAttribute('fill-opacity', '0.3');" +
                        "lastPolygon.setAttribute('stroke-width', '3');" +
                        "lastPolygon.setAttribute('stroke-dasharray', '5,5');" + // Línea punteada para indicar edición
                    "}" +
                "}, 200);");
            
            poligonoTemporal.bindTooltip("Zona en edición");
        }
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
        marcador.bindTooltip("Punto " + puntos.size() + " (nuevo)");
        marcador.addTo(map);
        marcadores.add(marcador);
        
        // Actualizar polígono temporal
        crearPoligonoTemporal();
        
        Notification.show("Punto añadido a la zona en edición", 
                          1500, Notification.Position.BOTTOM_START)
                   .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    /**
     * Finaliza la edición guardando los cambios
     */
    public void finalizarEdicion() {
        if (puntos.size() < 3) {
            Notification.show("Se necesitan al menos 3 puntos para crear una zona cerrada", 
                             3000, Notification.Position.MIDDLE)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        // Limpiar visualización temporal
        limpiarVisualizacionTemporal();
        
        // Actualizar las coordenadas en la zona original
        StringBuilder coordenadasStr = new StringBuilder();
        for (int i = 0; i < coordenadas.size(); i++) {
            if (i > 0) coordenadasStr.append(";");
            double[] coord = coordenadas.get(i);
            coordenadasStr.append(coord[0]).append(",").append(coord[1]);
        }
        
        try {
            // Actualizar información en la zona
            zonaOriginal.setCoordenadas(coordenadasStr.toString());
            
            // Aplicar colores según la urgencia de la tarea
            String colorBorde = getColorByUrgencia(tareaAsociada);
            String colorRelleno = getColorFillByUrgencia(tareaAsociada);
            
            zonaOriginal.setColorBorde(colorBorde);
            zonaOriginal.setColorRelleno(colorRelleno);
            
            // Guardar zona actualizada
            ZonaEncuentro zonaActualizada = zonaEncuentroServicio.actualizarZonaEncuentro(zonaOriginal);
            
            Notification.show("Zona de encuentro actualizada con éxito", 
                             3000, Notification.Position.BOTTOM_START)
                       .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                           
        } catch (Exception e) {
            System.err.println("Error al guardar zona editada: " + e.getMessage());
            Notification.show("Error al guardar los cambios: " + e.getMessage(), 
                             5000, Notification.Position.BOTTOM_START)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    /**
     * Cancela la edición sin guardar cambios
     */
    public void cancelarEdicion() {
        limpiarVisualizacionTemporal();
        
        Notification.show("Edición cancelada", 
                         3000, Notification.Position.BOTTOM_START)
                   .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }
    
    /**
     * Limpia todos los elementos visuales temporales
     */
    private void limpiarVisualizacionTemporal() {
        // Eliminar marcadores
        for (LCircleMarker marcador : marcadores) {
            map.removeLayer(marcador);
        }
        
        // Eliminar línea y polígono si existen
        if (lineaTemporal != null) {
            map.removeLayer(lineaTemporal);
        }
        
        if (poligonoTemporal != null) {
            map.removeLayer(poligonoTemporal);
        }
        
        // Limpiar listas
        marcadores.clear();
        lineaTemporal = null;
        poligonoTemporal = null;
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
    
    @Override
    public String getDescription() {
        return "Modo edición: Editar zona de encuentro para la tarea: " + tareaAsociada.getNombre();
    }
} 