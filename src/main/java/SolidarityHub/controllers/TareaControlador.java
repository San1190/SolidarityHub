package SolidarityHub.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import SolidarityHub.models.Habilidad;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Voluntario;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea.EstadoTarea;
import SolidarityHub.services.AutomatizacionServicio;
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

    public TareaControlador(TareaServicio tareaServicio, UsuarioServicio usuarioServicio, AutomatizacionServicio automatizacionServicio) {
        this.tareaServicio = tareaServicio;
        this.usuarioServicio = usuarioServicio;
        this.automatizacionServicio = automatizacionServicio;
    }

    @GetMapping
    public List<Tarea> obtenerTareas() {
        return tareaServicio.listarTareas();
    }

    @PostMapping("/crear")
    public Tarea crearTarea(@RequestBody Tarea tarea) {
        return tareaServicio.guardarTarea(tarea);
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
    
    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizarTarea(@PathVariable Long id, @RequestBody Tarea tarea) {
        Optional<Tarea> tareaExistente = tareaServicio.obtenerTareaPorId(id);
        if (tareaExistente.isPresent()) {
            tarea.setId(id);
            return new ResponseEntity<>(tareaServicio.actualizarTarea(tarea), HttpStatus.OK);
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