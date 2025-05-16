package SolidarityHub.commands;

import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;

/**
 * Interfaz que define el contrato para los comandos del mapa.
 * Siguiendo el patrón Comando, cada implementación encapsulará una acción específica.
 */
public interface MapCommand {
    
    /**
     * Ejecuta la acción del comando en las coordenadas especificadas.
     * 
     * @param map El mapa de Leaflet donde se ejecutará el comando
     * @param registry El registro de componentes para crear elementos del mapa
     * @param lat Latitud donde se ejecutará el comando
     * @param lng Longitud donde se ejecutará el comando
     */
    void execute(LMap map, LComponentManagementRegistry registry, double lat, double lng);
    
    /**
     * Devuelve una descripción del comando para mostrar al usuario.
     * 
     * @return Mensaje descriptivo sobre la acción del comando
     */
    String getDescription();
}