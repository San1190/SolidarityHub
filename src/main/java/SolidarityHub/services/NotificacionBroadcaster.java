package SolidarityHub.services;

import com.vaadin.flow.shared.Registration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Clase que implementa el patrón broadcaster para enviar notificaciones en tiempo real
 * usando la API Push de Vaadin en lugar de WebSockets directos.
 */
public class NotificacionBroadcaster {
    
    private static final Executor executor = Executors.newSingleThreadExecutor();
    
    // Mapa que almacena los listeners por ID de usuario
    private static final Map<Long, Map<Integer, Consumer<String>>> listeners = new ConcurrentHashMap<>();
    
    private static int nextId = 0;
    
    /**
     * Registra un nuevo listener para recibir notificaciones para un usuario específico
     * 
     * @param userId ID del usuario que recibirá las notificaciones
     * @param listener Función que se ejecutará cuando llegue una notificación
     * @return Un objeto Registration que permite cancelar la suscripción
     */
    public static synchronized Registration register(Long userId, Consumer<String> listener) {
        // Obtener o crear el mapa de listeners para este usuario
        Map<Integer, Consumer<String>> userListeners = listeners.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        
        // Asignar un ID único a este listener
        int id = nextId++;
        userListeners.put(id, listener);
        
        // Devolver un objeto Registration que permite cancelar la suscripción
        return () -> {
            synchronized (NotificacionBroadcaster.class) {
                // Eliminar este listener cuando se cancele la suscripción
                Map<Integer, Consumer<String>> userMap = listeners.get(userId);
                if (userMap != null) {
                    userMap.remove(id);
                    // Si no quedan listeners para este usuario, eliminar la entrada del mapa principal
                    if (userMap.isEmpty()) {
                        listeners.remove(userId);
                    }
                }
            }
        };
    }
    
    /**
     * Envía una notificación a un usuario específico
     * 
     * @param userId ID del usuario que recibirá la notificación
     * @param message Mensaje o datos de la notificación
     */
    public static void broadcast(Long userId, String message) {
        // Obtener los listeners para este usuario
        Map<Integer, Consumer<String>> userListeners = listeners.get(userId);
        
        if (userListeners != null) {
            // Ejecutar cada listener en un hilo separado para no bloquear
            executor.execute(() -> {
                userListeners.values().forEach(listener -> {
                    try {
                        listener.accept(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        }
    }
    
    /**
     * Envía una notificación a todos los usuarios registrados
     * 
     * @param message Mensaje o datos de la notificación
     */
    public static void broadcastAll(String message) {
        // Enviar la notificación a todos los usuarios registrados
        listeners.keySet().forEach(userId -> broadcast(userId, message));
    }
}