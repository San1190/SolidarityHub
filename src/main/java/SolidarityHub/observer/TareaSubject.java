package SolidarityHub.observer;

import SolidarityHub.models.Tarea;

/**
 * Interfaz para el patrón Observador que define los métodos para registrar, eliminar
 * y notificar a los observadores cuando una tarea es creada o actualizada.
 */
public interface TareaSubject {
    /**
     * Registra un observador para recibir notificaciones
     * @param observer El observador a registrar
     */
    void registerObserver(TareaObserver observer);
    
    /**
     * Elimina un observador para que deje de recibir notificaciones
     * @param observer El observador a eliminar
     */
    void removeObserver(TareaObserver observer);
    
    /**
     * Notifica a todos los observadores registrados
     * @param tarea La tarea que ha sido creada o actualizada
     */
    void notifyObservers(Tarea tarea);
}