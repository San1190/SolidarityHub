package SolidarityHub.repository;

import SolidarityHub.models.ZonaEncuentro;
import SolidarityHub.models.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio para acceder a las entidades ZonaEncuentro en la base de datos.
 */
public interface ZonaEncuentroRepositorio extends JpaRepository<ZonaEncuentro, Long> {
    
    /**
     * Encuentra todas las zonas de encuentro asociadas a una tarea específica
     * @param tarea La tarea para la que se buscan zonas de encuentro
     * @return Lista de zonas de encuentro
     */
    List<ZonaEncuentro> findByTarea(Tarea tarea);
    
    /**
     * Encuentra todas las zonas de encuentro asociadas a una tarea por su ID
     * @param tareaId ID de la tarea
     * @return Lista de zonas de encuentro
     */
    @Query("SELECT z FROM ZonaEncuentro z WHERE z.tarea.id = :tareaId")
    List<ZonaEncuentro> findByTareaId(@Param("tareaId") Long tareaId);
    
    /**
     * Encuentra todas las zonas de encuentro y carga sus tareas asociadas de forma inmediata
     * Esto soluciona el problema de "No session" en el lazy loading
     * @return Lista de zonas de encuentro con tareas cargadas
     */
    @Query("SELECT z FROM ZonaEncuentro z JOIN FETCH z.tarea")
    List<ZonaEncuentro> findAllWithTareas();
    
    /**
     * Encuentra una zona de encuentro por ID y carga su tarea asociada de forma inmediata
     * @param id ID de la zona de encuentro
     * @return La zona de encuentro con su tarea cargada
     */
    @Query("SELECT z FROM ZonaEncuentro z JOIN FETCH z.tarea WHERE z.id = :id")
    ZonaEncuentro findByIdWithTarea(@Param("id") Long id);
} 