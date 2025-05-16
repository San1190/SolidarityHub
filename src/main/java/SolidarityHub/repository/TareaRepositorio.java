package SolidarityHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea.EstadoTarea;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TareaRepositorio extends JpaRepository<Tarea, Long> {
    
    // Método para encontrar tareas por estado
    List<Tarea> findByEstado(EstadoTarea estado);
    
    // Método para encontrar tareas por tipo
    List<Tarea> findByTipo(TipoNecesidad tipo);
    
    // Método para encontrar tareas por estado y tipo
    List<Tarea> findByEstadoAndTipo(EstadoTarea estado, TipoNecesidad tipo);
    
    /**
     * Método que carga todas las tareas con sus voluntarios asignados en una sola consulta
     * utilizando JOIN FETCH para evitar el LazyInitializationException
     * 
     * @return Lista de tareas con sus voluntarios asignados cargados
     */
    @Query("SELECT DISTINCT t FROM Tarea t LEFT JOIN FETCH t.voluntariosAsignados")
    List<Tarea> findAllWithVoluntariosAsignados();

    @Query("SELECT DISTINCT t FROM Tarea t LEFT JOIN FETCH t.suscriptores")
    List<Tarea> findAllWithSuscriptores();

    @Query("SELECT t FROM Tarea t LEFT JOIN FETCH t.suscriptores WHERE t.id = :id")
    @Transactional(readOnly = true)
    Tarea obtenerTareaPorIdConSuscriptores(@Param("id") Long id);

    @Query("SELECT FUNCTION('MONTH', t.fechaInicio) as mes, t.nombre as nombre, COUNT(t) as cantidad " +
       "FROM Tarea t " +
       "GROUP BY mes, nombre " +
       "ORDER BY mes, nombre")
    List<Object[]> contarTareasPorNombreYMes();

}