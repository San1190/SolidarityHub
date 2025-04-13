package SolidarityHub.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import SolidarityHub.models.Recursos;
import SolidarityHub.models.Recursos.TipoRecurso;

import SolidarityHub.services.RecursoServicio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recursos")
public class RecursoControlador {

    private final RecursoServicio recursoServicio;

    public RecursoControlador(RecursoServicio recursoServicio) {
        this.recursoServicio = recursoServicio;
    }

    @GetMapping
    public List<Recursos> obtenerRecursos() {
        return recursoServicio.listarRecursos();
    }

    @PostMapping("/crear")
    public Recursos crearRecurso(@RequestBody Recursos recurso) {
        return recursoServicio.guardarRecursos(recurso);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recursos> obtenerRecurso(@PathVariable Long id) {
        Optional<Recursos> recurso = recursoServicio.obtenerRecursoPorId(id);
        if (recurso.isPresent()) {
            return new ResponseEntity<>(recurso.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recursos> actualizarRecurso(@PathVariable Long id, @RequestBody Recursos recurso) {
        Optional<Recursos> recursoExistente = recursoServicio.obtenerRecursoPorId(id);
        if (recursoExistente.isPresent()) {
            recurso.setId(id);
            Recursos recursoActualizado = recursoServicio.actualizarRecurso(recurso);
            return new ResponseEntity<>(recursoActualizado, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRecurso(@PathVariable Long id) {
        Optional<Recursos> recursoExistente = recursoServicio.obtenerRecursoPorId(id);
        if (recursoExistente.isPresent()) {
            recursoServicio.eliminarRecurso(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/filtrar")
    public List<Recursos> filtrarRecursos(@RequestParam Map<String, String> params) {
        Optional<TipoRecurso> tipoRecurso = Optional.ofNullable(TipoRecurso.valueOf(params.get("tipoRecurso")));
        return recursoServicio.filtrarPorTipo(tipoRecurso.orElse(null));
    }

}
