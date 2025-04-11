package SolidarityHub.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea.EstadoTarea;
import SolidarityHub.services.TareaServicio;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tareas")
public class TareaControlador {

    private final TareaServicio tareaServicio;

    public TareaControlador(TareaServicio tareaServicio) {
        this.tareaServicio = tareaServicio;
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
}