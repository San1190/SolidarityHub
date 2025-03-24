package SolidarityHub.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import SolidarityHub.models.Usuario;
import SolidarityHub.services.UsuarioServicio;

import java.io.IOException;
import java.util.List;

/*
 * import java.util.List;
 
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
 
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
 
    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return usuarioService.listarUsuarios();
    }
 
    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioService.guardarUsuario(usuario);
    }
}
 */

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {
    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        try {
            return usuarioServicio.listarUsuarios();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener los usuarios", e);
        }
    }

    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioServicio.guardarUsuario(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioServicio.eliminarUsuario(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    




}