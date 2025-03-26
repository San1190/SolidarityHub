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
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {
    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return usuarioServicio.listarUsuarios();
    }
    
    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioServicio.guardarUsuario(usuario);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioServicio.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<String> subirFoto(@PathVariable Long id, @RequestParam("foto") MultipartFile foto) throws IOException {
        Usuario usuario = usuarioServicio.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        usuario.setFoto(foto.getBytes());
        usuarioServicio.guardarUsuario(usuario);
        return ResponseEntity.ok("Foto subida correctamente.");
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> obtenerFoto(@PathVariable Long id) throws IOException {
        Usuario usuario = usuarioServicio.obtenerUsuarioPorId(id);

        byte[] foto = (usuario != null && usuario.getFoto() != null)
                ? usuario.getFoto()
                : usuarioServicio.getDefaultProfileImage();

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(foto);
    }
}
