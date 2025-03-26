package SolidarityHub.controllers;

import org.springframework.http.MediaType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import SolidarityHub.models.Usuario;
import SolidarityHub.services.UsuarioServicio;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

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

    

    @PostMapping("/{id}/foto")
    public ResponseEntity<String> subirFoto(@PathVariable Long id, @RequestParam("foto") MultipartFile foto) throws IOException {
        Usuario usuario = usuarioServicio.obtenerUsuarioPorId(id);
        if (usuario != null) {
            byte[] fotoBytes = foto.getBytes();
            usuario.setFoto(fotoBytes);
            usuarioServicio.guardarUsuario(usuario);
            return ResponseEntity.ok("Foto subida correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }
    }


    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> obtenerFoto(@PathVariable Long id) throws IOException {
        Usuario usuario = usuarioServicio.obtenerUsuarioPorId(id);
    
        if (usuario != null && usuario.getFoto() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Esto se debe ajustar según el formato de la imagen guardada
                    .body(usuario.getFoto());
        } else {
            byte[] defaultImage = usuarioServicio.getDefaultProfileImage();
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG) // Ajusta el tipo de contenido a 'image/png' si la imagen por defecto es PNG
                    .body(defaultImage);
        }
    }

    






}