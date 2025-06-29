package SolidarityHub.controllers;

import SolidarityHub.models.Notificacion;
import SolidarityHub.repository.UsuarioRepositorio;
import SolidarityHub.services.NotificacionServicio;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import SolidarityHub.models.Gestor;
import SolidarityHub.models.Afectado;
import SolidarityHub.services.UsuarioServicio;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {
    private final UsuarioServicio usuarioServicio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final NotificacionServicio notificacionServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio, UsuarioRepositorio usuarioRepositorio, NotificacionServicio notificacionServicio) {
        this.usuarioServicio = usuarioServicio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.notificacionServicio = notificacionServicio;
    }

    // 🔹 Obtener todos los usuarios
    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return usuarioServicio.listarUsuarios();
    }
    
    // 🔹 Obtener notificaciones de un usuario
    @GetMapping("/{usuarioId}/notificaciones")
    public ResponseEntity<List<Notificacion>> obtenerNotificacionesUsuario(@PathVariable Long usuarioId) {
        Usuario usuario = usuarioServicio.obtenerUsuarioPorId(usuarioId);
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<Notificacion> notificaciones = notificacionServicio.findByVoluntarioAndEstado(usuario, Notificacion.EstadoNotificacion.PENDIENTE);
        return new ResponseEntity<>(notificaciones, HttpStatus.OK);
    }

    // 🔹 Crear un nuevo usuario
    @PostMapping("/registrar")
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario usuario) {
        try {
            // Verificar si el usuario tiene foto, si no, asignar la foto por defecto
            if (usuario.getFoto() == null || usuario.getFoto().length == 0) {
                try {
                    usuario.setFoto(usuarioServicio.getDefaultProfileImage());
                } catch (IOException e) {
                    // Si hay un error al obtener la imagen por defecto, continuamos sin foto
                    System.err.println("Error al cargar la imagen por defecto: " + e.getMessage());
                }
            }
            
            Usuario nuevoUsuario = usuarioServicio.guardarUsuario(usuario);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    // Endpoint para actualizar un usuario (voluntario o afectado)
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        Usuario usuarioExistente = usuarioServicio.obtenerUsuarioPorId(id);
        if (usuarioExistente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        // Actualización de campos comunes
        usuarioExistente.setNombre(usuarioActualizado.getNombre());
        usuarioExistente.setApellidos(usuarioActualizado.getApellidos());
        usuarioExistente.setEmail(usuarioActualizado.getEmail());
        usuarioExistente.setPassword(usuarioActualizado.getPassword());

        // Actualización de campos específicos según el tipo
        if (usuarioExistente instanceof Voluntario && usuarioActualizado instanceof Voluntario) {
            Voluntario volExist = (Voluntario) usuarioExistente;
            Voluntario volAct = (Voluntario) usuarioActualizado;
            volExist.setHabilidades(volAct.getHabilidades());
            volExist.setDiasDisponibles(volAct.getDiasDisponibles());
            volExist.setTurnoDisponibilidad(volAct.getTurnoDisponibilidad());
            
        } else if (usuarioExistente instanceof Afectado && usuarioActualizado instanceof Afectado) {
            Afectado afectExist = (Afectado) usuarioExistente;
            Afectado afectAct = (Afectado) usuarioActualizado;
            
            // Manejo correcto de la colección con orphanRemoval=true
            // Primero limpiamos la lista actual pero manteniendo la referencia
            if (afectExist.getNecesidades() != null) {
                afectExist.getNecesidades().clear();
                
                // Luego agregamos todos los elementos de la nueva lista
                if (afectAct.getNecesidades() != null) {
                    afectExist.getNecesidades().addAll(afectAct.getNecesidades());
                }
            } else {
                // Si la lista es null, simplemente asignamos la nueva lista
                afectExist.setNecesidades(afectAct.getNecesidades());
            }
        }

        Usuario actualizado = usuarioServicio.guardarUsuario(usuarioExistente);
        return ResponseEntity.ok(actualizado);
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
            case "gestor" -> Gestor.class;
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

    // 🔹 Eliminar foto de perfil (establecer a la imagen por defecto)
    @DeleteMapping("/{id}/foto/eliminar")
    public ResponseEntity<String> eliminarFoto(@PathVariable Long id) throws IOException {
        Usuario usuario = usuarioServicio.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }
        
        // Establecer la imagen por defecto
        usuario.setFoto(usuarioServicio.getDefaultProfileImage());
        usuarioServicio.guardarUsuario(usuario);
        return ResponseEntity.ok("Foto eliminada correctamente.");
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

    @GetMapping(value = "/{id}/actualizar-notificaciones", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Notificacion>> actualizarNotificaciones(@PathVariable long id) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario instanceof Voluntario voluntario) {
                List<Notificacion> notificaciones = usuarioServicio.actualizarNotificaciones(voluntario);
                return ResponseEntity.ok(notificaciones);
            } else {
                return ResponseEntity.notFound().build();
            }
        }else{
            return ResponseEntity.notFound().build();
        }
    }
}
