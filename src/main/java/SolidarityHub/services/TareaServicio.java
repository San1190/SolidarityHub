package SolidarityHub.services;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea.EstadoTarea;
import SolidarityHub.repository.TareaRepositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class TareaServicio {
    
    private final TareaRepositorio tareaRepositorio;

    public TareaServicio(TareaRepositorio tareaRepositorio) {
        this.tareaRepositorio = tareaRepositorio;
    }

    // Método para listar todas las tareas
    public List<Tarea> listarTareas() {
        return tareaRepositorio.findAll();
    }

    // Método para obtener una tarea por su ID
    public Optional<Tarea> obtenerTareaPorId(Long id) {
        return tareaRepositorio.findById(id);
    }

    // Método para guardar una tarea
    public Tarea guardarTarea(Tarea tarea) {
        return tareaRepositorio.save(tarea);
    }

    // Método para actualizar una tarea
    public Tarea actualizarTarea(Tarea tarea) {
        return tareaRepositorio.save(tarea);
    }

    // Método para eliminar una tarea
    public void eliminarTarea(Long id) {
        tareaRepositorio.deleteById(id);
    }
    
    // Método para filtrar tareas por estado
    public List<Tarea> filtrarPorEstado(EstadoTarea estado) {
        return tareaRepositorio.findByEstado(estado);
    }
    
    // Método para filtrar tareas por tipo
    public List<Tarea> filtrarPorTipo(TipoNecesidad tipo) {
        return tareaRepositorio.findByTipo(tipo);
    }
    
    // Método para filtrar tareas por estado y tipo
    public List<Tarea> filtrarPorEstadoYTipo(EstadoTarea estado, TipoNecesidad tipo) {
        return tareaRepositorio.findByEstadoAndTipo(estado, tipo);
    }
}