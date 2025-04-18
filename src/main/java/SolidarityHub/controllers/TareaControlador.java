package SolidarityHub.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import SolidarityHub.models.Habilidad;
import SolidarityHub.models.Recursos;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea.EstadoTarea;
import SolidarityHub.services.AsignacionTareaServicio;
import SolidarityHub.services.AutomatizacionServicio;
import SolidarityHub.services.RecursoServicio;
import SolidarityHub.services.TareaServicio;
import SolidarityHub.services.UsuarioServicio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tareas")
public class TareaControlador {

    private final TareaServicio tareaServicio;
    private final UsuarioServicio usuarioServicio;
    private final AutomatizacionServicio automatizacionServicio;
    private final AsignacionTareaServicio asignacionTareaServicio;
    private final RecursoServicio recursoServicio;

    public TareaControlador(TareaServicio tareaServicio, UsuarioServicio usuarioServicio, 
                           AutomatizacionServicio automatizacionServicio,
                           AsignacionTareaServicio asignacionTareaServicio,
                           RecursoServicio recursoServicio) {
        this.tareaServicio = tareaServicio;
        this.usuarioServicio = usuarioServicio;
        this.automatizacionServicio = automatizacionServicio;
        this.asignacionTareaServicio = asignacionTareaServicio;
        this.recursoServicio = recursoServicio;
    }

    @GetMapping
    public List<Tarea> obtenerTareas() {
        return tareaServicio.listarTareas();
    }

    @PostMapping("/crear")
    public Tarea crearTarea(@RequestBody Tarea tarea) {
        // Verificar que el creador esté establecido
        if (tarea.getCreador() == null) {
            throw new IllegalArgumentException("El creador de la tarea no puede ser nulo");
        }
        
        // Verificar que el creador existe en la base de datos
        Usuario creador = usuarioServicio.obtenerUsuarioPorId(tarea.getCreador().getId());
        if (creador == null) {
            throw new IllegalArgumentException("El creador especificado no existe");
        }
        
        // Establecer el creador verificado
        tarea.setCreador(creador);
        
        // Inicializar la lista de voluntarios asignados si es nula
        if (tarea.getVoluntariosAsignados() == null) {
            tarea.setVoluntariosAsignados(new ArrayList<>());
        }
        
        // Guardar la tarea con su creador
        Tarea tareaNueva = tareaServicio.guardarTarea(tarea);
        
        // Si se desea asignación automática, intentar asignar voluntarios
        if (tareaNueva.getEstado() == EstadoTarea.PREPARADA) {
            // Asignar voluntarios automáticamente (radio de 10km como ejemplo)
            asignacionTareaServicio.asignarVoluntariosAutomaticamente(tareaNueva, 10.0);
            
            // Asignar recursos automáticamente
            asignarRecursosAutomaticamente(tareaNueva);
            
            // Actualizar la tarea con los voluntarios asignados
            tareaNueva = tareaServicio.actualizarTarea(tareaNueva);
        }
        
        return tareaNueva;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> obtenerTarea(@PathVariable Long id) {
        Optional<Tarea> tarea = tareaServicio.obtenerTareaPorId(id);
        if (tarea.isPresent()) {
            return new ResponseEntity<>(tarea.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Método privado para asignar recursos automáticamente a una tarea
     * @param tarea La tarea a la que se asignarán recursos
     */
    private void asignarRecursosAutomaticamente(Tarea tarea) {
        if (tarea == null || tarea.getTipo() == null) {
            return;
        }
        
        // Convertir el tipo de necesidad al tipo de recurso correspondiente
        Recursos.TipoRecurso tipoRecursoNecesario = null;
        try {
            tipoRecursoNecesario = Recursos.TipoRecurso.valueOf(tarea.getTipo().name());
        } catch (IllegalArgumentException e) {
            // Si no hay correspondencia exacta, no se puede asignar automáticamente
            return;
        }
        
        // Buscar recursos disponibles del tipo necesario
        List<Recursos> recursosDisponibles = recursoServicio.filtrarPorTipo(tipoRecursoNecesario).stream()
                .filter(r -> r.getEstado() == Recursos.EstadoRecurso.DISPONIBLE)
                .collect(Collectors.toList());
        
        // Asignar hasta 3 recursos a la tarea (o menos si no hay suficientes)
        int recursosAsignar = Math.min(recursosDisponibles.size(), 3);
        for (int i = 0; i < recursosAsignar; i++) {
            Recursos recurso = recursosDisponibles.get(i);
            recurso.setEstado(Recursos.EstadoRecurso.ASIGNADO);
            recurso.setTareaAsignada(tarea);
            recursoServicio.actualizarRecurso(recurso);
        }
    }
    
    /**
     * Endpoint para asignar manualmente un voluntario a una tarea
     */
    @PostMapping("/{tareaId}/asignar-voluntario/{voluntarioId}")
    public ResponseEntity<Tarea> asignarVoluntarioManualmente(
            @PathVariable Long tareaId, 
            @PathVariable Long voluntarioId) {
        
        Optional<Tarea> tareaOpt = tareaServicio.obtenerTareaPorId(tareaId);
        if (!tareaOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Tarea tarea = tareaOpt.get();
        Object usuario = usuarioServicio.obtenerUsuarioPorId(voluntarioId);
        
        if (usuario == null || !(usuario instanceof Voluntario)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        Voluntario voluntario = (Voluntario) usuario;
        
        // Añadir el voluntario a la lista de asignados si no está ya
        if (tarea.getVoluntariosAsignados() == null) {
            tarea.setVoluntariosAsignados(new ArrayList<>());
        }
        
        if (!tarea.getVoluntariosAsignados().contains(voluntario)) {
            tarea.getVoluntariosAsignados().add(voluntario);
            tarea = tareaServicio.actualizarTarea(tarea);
        }
        
        return new ResponseEntity<>(tarea, HttpStatus.OK);
    }
    
    /**
     * Endpoint para asignar manualmente un recurso a una tarea
     */
    @PostMapping("/{tareaId}/asignar-recurso/{recursoId}")
    public ResponseEntity<Tarea> asignarRecursoManualmente(
            @PathVariable Long tareaId, 
            @PathVariable Long recursoId) {
        
        Optional<Tarea> tareaOpt = tareaServicio.obtenerTareaPorId(tareaId);
        if (!tareaOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Optional<Recursos> recursoOpt = recursoServicio.obtenerRecursoPorId(recursoId);
        if (!recursoOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Tarea tarea = tareaOpt.get();
        Recursos recurso = recursoOpt.get();
        
        // Asignar el recurso a la tarea
        recurso.setEstado(Recursos.EstadoRecurso.ASIGNADO);
        recurso.setTareaAsignada(tarea);
        recursoServicio.actualizarRecurso(recurso);
        
        return new ResponseEntity<>(tarea, HttpStatus.OK);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizarTarea(@PathVariable Long id, @RequestBody Tarea tarea) {
        Optional<Tarea> tareaExistente = tareaServicio.obtenerTareaPorId(id);
        if (tareaExistente.isPresent()) {
            // Mantener el creador original
            if (tarea.getCreador() == null) {
                tarea.setCreador(tareaExistente.get().getCreador());
            }
            
            tarea.setId(id);
            Tarea tareaActualizada = tareaServicio.actualizarTarea(tarea);
            
            // Si la tarea cambió a estado PREPARADA, intentar asignación automática
            if (tareaActualizada.getEstado() == EstadoTarea.PREPARADA && 
                (tareaExistente.get().getEstado() != EstadoTarea.PREPARADA || 
                 tareaActualizada.getVoluntariosAsignados() == null || 
                 tareaActualizada.getVoluntariosAsignados().isEmpty())) {
                
                // Asignar voluntarios automáticamente
                asignacionTareaServicio.asignarVoluntariosAutomaticamente(tareaActualizada, 10.0);
                
                // Asignar recursos automáticamente
                asignarRecursosAutomaticamente(tareaActualizada);
            }
            
            return new ResponseEntity<>(tareaActualizada, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        tareaServicio.eliminarTarea(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/filtrar")
    public List<Tarea> filtrarTareas(
            @RequestParam(required = false) EstadoTarea estado,
            @RequestParam(required = false) TipoNecesidad tipo) {
        
        if (estado != null && tipo != null) {
            return tareaServicio.filtrarPorEstadoYTipo(estado, tipo);
        } else if (estado != null) {
            return tareaServicio.filtrarPorEstado(estado);
        } else if (tipo != null) {
            return tareaServicio.filtrarPorTipo(tipo);
        } else {
            return tareaServicio.listarTareas();
        }
    }
    
    /**
     * Endpoint para obtener las tareas asignadas a un voluntario específico
     * @param voluntarioId ID del voluntario
     * @return Lista de tareas asignadas al voluntario
     */
    @GetMapping("/voluntario/{voluntarioId}")
    public ResponseEntity<List<Tarea>> obtenerTareasDeVoluntario(@PathVariable Long voluntarioId) {
        try {
            // Obtener el voluntario
            Object usuario = usuarioServicio.obtenerUsuarioPorId(voluntarioId);
            if (usuario == null || !(usuario instanceof Voluntario)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            Voluntario voluntario = (Voluntario) usuario;
            
            // Obtener todas las tareas
            List<Tarea> todasLasTareas = tareaServicio.listarTareas();
            
            // Filtrar las tareas donde el voluntario está asignado
            List<Tarea> tareasAsignadas = todasLasTareas.stream()
                .filter(tarea -> tarea.getVoluntariosAsignados() != null && 
                                tarea.getVoluntariosAsignados().stream()
                                    .anyMatch(vol -> vol.getId().equals(voluntarioId)))
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(tareasAsignadas, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Endpoint para obtener las tareas compatibles con las habilidades de un voluntario
     * @param voluntarioId ID del voluntario
     * @return Lista de tareas compatibles con las habilidades del voluntario
     */
    @GetMapping("/compatibles/voluntario/{voluntarioId}")
    public ResponseEntity<List<Tarea>> obtenerTareasCompatiblesConVoluntario(@PathVariable Long voluntarioId) {
        try {
            // Obtener el voluntario
            Object usuario = usuarioServicio.obtenerUsuarioPorId(voluntarioId);
            if (usuario == null || !(usuario instanceof Voluntario)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            Voluntario voluntario = (Voluntario) usuario;
            
            // Verificar que el voluntario tenga habilidades
            if (voluntario.getHabilidades() == null || voluntario.getHabilidades().isEmpty()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
            }
            
            // Obtener todas las tareas
            List<Tarea> todasLasTareas = tareaServicio.listarTareas();
            
            // Mapeo entre tipos de necesidad y habilidades requeridas (similar al de AutomatizacionServicio)
            Map<TipoNecesidad, Habilidad> mapeoHabilidades = new HashMap<>();
            mapeoHabilidades.put(TipoNecesidad.PRIMEROS_AUXILIOS, Habilidad.PRIMEROS_AUXILIOS);
            mapeoHabilidades.put(TipoNecesidad.ALIMENTACION, Habilidad.COCINA);
            mapeoHabilidades.put(TipoNecesidad.ALIMENTACION_BEBE, Habilidad.COCINA);
            mapeoHabilidades.put(TipoNecesidad.SERVICIO_LIMPIEZA, Habilidad.LIMPIEZA);
            mapeoHabilidades.put(TipoNecesidad.AYUDA_PSICOLOGICA, Habilidad.AYUDA_PSICOLOGICA);
            mapeoHabilidades.put(TipoNecesidad.AYUDA_CARPINTERIA, Habilidad.CARPINTERIA);
            mapeoHabilidades.put(TipoNecesidad.AYUDA_ELECTRICIDAD, Habilidad.ELECTICISTA);
            mapeoHabilidades.put(TipoNecesidad.AYUDA_FONTANERIA, Habilidad.FONTANERIA);
            
            // Filtrar las tareas compatibles con las habilidades del voluntario
            List<Tarea> tareasCompatibles = todasLasTareas.stream()
                .filter(tarea -> {
                    if (tarea.getTipo() == null) return false;
                    Habilidad habilidadRequerida = mapeoHabilidades.get(tarea.getTipo());
                    return habilidadRequerida != null && voluntario.getHabilidades().contains(habilidadRequerida);
                })
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(tareasCompatibles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Endpoint para emparejar automáticamente todas las tareas con voluntarios
     */
    @PostMapping("/emparejar-todas")
    public ResponseEntity<String> emparejarTodasLasTareas() {
        try {
            automatizacionServicio.emparejarTodasLasTareas();
            return ResponseEntity.ok("Todas las tareas han sido emparejadas con voluntarios compatibles");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al emparejar tareas: " + e.getMessage());
        }
    }
}