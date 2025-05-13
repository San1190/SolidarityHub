package SolidarityHub.observer;

import SolidarityHub.models.Tarea;

/**
 * Interfaz para el patrón Observador que define el método que será llamado
 * cuando una tarea sea creada o actualizada.
 */
public interface TareaObserver {
    /**
     * Método que se ejecuta cuando una tarea es creada o actualizada
     * @param tarea La tarea que ha sido creada o actualizada
     */
    void onTareaCreated(Tarea tarea);
}