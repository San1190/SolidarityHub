package SolidarityHub.controllers;

import org.springframework.http.MediaType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import SolidarityHub.models.Afectado;
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

    // 🔹 Obtener todos los usuarios
    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return usuarioServicio.listarUsuarios();
    }

    // 🔹 Crear un nuevo usuario
    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioServicio.guardarUsuario(usuario);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 🔹 Eliminar usuario por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioServicio.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    // 🔹 **Método corregido para Login**
    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody Usuario usuario) {
        System.out.println("Usuario: " + usuario.getEmail() + ", Tipo: " + usuario.getTipoUsuario());

        // 🔹 Convertimos el tipoUsuario de String a Class
        Class<? extends Usuario> tipoClase = convertirTipoUsuario(usuario.getTipoUsuario());
        if (tipoClase == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo de usuario inválido.");
        }

        // 🔹 Buscar usuario en la base de datos
        Usuario usuarioEncontrado = usuarioServicio.buscarUsuarioPorEmailYTipo(usuario.getEmail(), tipoClase);

        if (usuarioEncontrado != null && usuarioEncontrado.getPassword().equals(usuario.getPassword())) {
            return ResponseEntity.ok(usuarioEncontrado);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    // 🔹 Método para convertir String a Class<? extends Usuario>
    private Class<? extends Usuario> convertirTipoUsuario(String tipoUsuario) {
        return switch (tipoUsuario.toLowerCase()) {
            case "voluntario" -> Voluntario.class;
            case "afectado" -> Afectado.class;
            default -> null;
        };
    }

    // 🔹 Subir foto de perfil
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

    // 🔹 Obtener foto de perfil del usuario
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> obtenerFoto(@PathVariable Long id) throws IOException {
        Usuario usuario = usuarioServicio.obtenerUsuarioPorId(id);

        byte[] foto = (usuario != null && usuario.getFoto() != null)
                ? usuario.getFoto()
                : usuarioServicio.getDefaultProfileImage();

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(foto);
    }
}
