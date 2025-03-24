package SolidarityHub.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import SolidarityHub.models.Necesidad;
import SolidarityHub.services.NecesidadServicio;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/necesidades")
public class NecesidadControlador {

    private final NecesidadServicio necesidadServicio;

    public NecesidadControlador(NecesidadServicio necesidadServicio) {
        this.necesidadServicio = necesidadServicio;
    }

    @GetMapping
    public List<Necesidad> obtenerNecesidades() {
        return necesidadServicio.listarNecesidades();
    }

    @PostMapping("/crear")
    public Necesidad crearNecesidad(@RequestBody Necesidad necesidad) {
        return necesidadServicio.guardarNecesidad(necesidad);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Necesidad> obtenerNecesidad(@PathVariable Long id) {
        Optional<Necesidad> necesidad = necesidadServicio.obtenerNecesidadPorId(id);
        if (necesidad.isPresent()) {
            return new ResponseEntity<>(necesidad.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNecesidad(@PathVariable Long id) {
        necesidadServicio.eliminarNecesidad(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
