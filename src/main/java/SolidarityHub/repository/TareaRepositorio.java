package SolidarityHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea.EstadoTarea;

import java.util.List;

public interface TareaRepositorio extends JpaRepository<Tarea, Long> {
    
    // Método para encontrar tareas por estado
    List<Tarea> findByEstado(EstadoTarea estado);
    
    // Método para encontrar tareas por tipo
    List<Tarea> findByTipo(TipoNecesidad tipo);
    
    // Método para encontrar tareas por estado y tipo
    List<Tarea> findByEstadoAndTipo(EstadoTarea estado, TipoNecesidad tipo);
}