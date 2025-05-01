package SolidarityHub.services;

import SolidarityHub.models.Recursos;
import SolidarityHub.models.Recursos.TipoRecurso;
import SolidarityHub.models.Recursos.EstadoRecurso;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Tarea.EstadoTarea;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.repository.RecursoRepositorio;
import SolidarityHub.repository.TareaRepositorio;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class RecursoServicio {

    private final RecursoRepositorio recursoRepositorio;
    private final TareaRepositorio tareaRepositorio;

    public RecursoServicio(RecursoRepositorio recursoRepositorio, TareaRepositorio tareaRepositorio) {
        this.recursoRepositorio = recursoRepositorio;
        this.tareaRepositorio = tareaRepositorio;
    }

    public List<Recursos> listarRecursos() {
        return recursoRepositorio.findAll();
    }

    public Optional<Recursos> obtenerRecursoPorId(Long id) {
        return recursoRepositorio.findById(id);
    }

    public Recursos guardarRecursos(Recursos recurso) {
        // Guardar el recurso primero
        Recursos recursoGuardado = recursoRepositorio.save(recurso);
        
        // Si el recurso está disponible, intentar asignarlo automáticamente a una tarea urgente
        if (recursoGuardado.getEstado() == EstadoRecurso.DISPONIBLE) {
            asignarRecursoAutomaticamente(recursoGuardado);
        }
        
        return recursoGuardado;
    }

    public Recursos actualizarRecurso(Recursos recurso) {
        return recursoRepositorio.save(recurso);
    }

    public void eliminarRecurso(Long id) {
        recursoRepositorio.deleteById(id);
    }

    public List<Recursos> filtrarPorTipo(TipoRecurso tipoRecurso) {
        return recursoRepositorio.findByTipoRecurso(tipoRecurso);
    }
    
    /**
     * Asigna automáticamente un recurso a la tarea más urgente que necesite ese tipo de recurso
     * @param recurso El recurso a asignar
     * @return true si se asignó correctamente, false en caso contrario
     */
    public boolean asignarRecursoAutomaticamente(Recursos recurso) {
        if (recurso.getEstado() != EstadoRecurso.DISPONIBLE) {
            return false;
        }
        
        // Convertir el tipo de recurso al tipo de necesidad correspondiente
        TipoNecesidad tipoNecesidad = null;
        try {
            // Los nombres de los enums deben coincidir
            tipoNecesidad = TipoNecesidad.valueOf(recurso.getTipoRecurso().name());
        } catch (IllegalArgumentException e) {
            // Si no hay correspondencia exacta, no se puede asignar automáticamente
            return false;
        }
        
        // Buscar tareas en estado PREPARADA o EN_CURSO que necesiten este tipo de recurso
        List<Tarea> tareasCompatibles = tareaRepositorio.findByTipo(tipoNecesidad).stream()
                .filter(t -> t.getEstado() == EstadoTarea.PREPARADA || t.getEstado() == EstadoTarea.EN_CURSO)
                .collect(Collectors.toList());
        
        if (tareasCompatibles.isEmpty()) {
            return false;
        }
        
        // Ordenar las tareas por prioridad (aquí podrías implementar tu lógica de priorización)
        // Por ejemplo, podríamos priorizar las tareas con menos recursos asignados
        Tarea tareaMasUrgente = tareasCompatibles.stream()
                .min(Comparator.comparing(t -> {
                    // Contar cuántos recursos de este tipo ya tiene asignados
                    long recursosAsignados = recursoRepositorio.findAll().stream()
                            .filter(r -> r.getTareaAsignada() != null && 
                                    r.getTareaAsignada().getId().equals(t.getId()) &&
                                    r.getTipoRecurso() == recurso.getTipoRecurso())
                            .count();
                    return recursosAsignados;
                }))
                .orElse(null);
        
        if (tareaMasUrgente != null) {
            // Asignar el recurso a la tarea
            recurso.setEstado(EstadoRecurso.ASIGNADO);
            recurso.setTareaAsignada(tareaMasUrgente);
            recursoRepositorio.save(recurso);
            return true;
        }
        
        return false;
    }

    /**
     * Asigna un recurso disponible y compatible a una tarea específica
     * @param tarea La tarea a la que se asignará el recurso
     */
    public void asignarRecursoDisponibleATarea(Tarea tarea) {
        if (tarea == null || tarea.getTipo() == null) {
            return;
        }
    
        try {
            // Convertir tipo de necesidad de la tarea al tipo de recurso
            TipoRecurso tipoRecurso = TipoRecurso.valueOf(tarea.getTipo().name());
    
            // Buscar el primer recurso disponible y no asignado del tipo correcto
            Optional<Recursos> recursoOpt = recursoRepositorio.findByTipoRecurso(tipoRecurso).stream()
                    .filter(r -> r.getEstado() == EstadoRecurso.DISPONIBLE && r.getTareaAsignada() == null)
                    .findFirst();
    
            if (recursoOpt.isPresent()) {
                Recursos recurso = recursoOpt.get();
                recurso.setEstado(EstadoRecurso.ASIGNADO);
                recurso.setTareaAsignada(tarea);
                recursoRepositorio.save(recurso);
            }
    
        } catch (IllegalArgumentException e) {
            // El tipo de la tarea no corresponde con ningún tipo de recurso
            return;
        }
    }

}