package SolidarityHub.services;

import SolidarityHub.models.Usuario;
import SolidarityHub.repository.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServicio {
    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    // 🔹 Método corregido para buscar usuario por email y tipo (usando Class)
    public Usuario buscarUsuarioPorEmailYTipo(String email, Class<? extends Usuario> tipoUsuario) {
        return usuarioRepositorio.findByEmailAndTipoUsuario(email, tipoUsuario)
                .orElse(null);  // Retorna null si no se encuentra
    }

    // 🔹 Listar todos los usuarios
    public List<Usuario> listarUsuarios() {
        return usuarioRepositorio.findAll();
    }

    // 🔹 Obtener usuario por ID
    public Usuario obtenerUsuarioPorId(Long id) {
        // Primero intentamos obtener el usuario para determinar su tipo
        Usuario usuario = usuarioRepositorio.findById(id).orElse(null);
        
        // Si el usuario es de tipo Voluntario, usamos la consulta específica que carga las colecciones
        if (usuario != null && "voluntario".equals(usuario.getTipoUsuario())) {
            return usuarioRepositorio.findVoluntarioByIdWithDiasDisponibles(id).orElse((SolidarityHub.models.Voluntario)usuario);
        }
        
        return usuario;
    }

    // 🔹 Guardar usuario con validación de duplicados
    public Usuario guardarUsuario(Usuario usuario) {
        // Verificar si el usuario ya existe
        Optional<Usuario> usuarioExistente = usuarioRepositorio.findByEmailAndTipoUsuario(
                usuario.getEmail(), usuario.getClass()
        );

        // Si existe un usuario con el mismo email y tipo, verificar que no sea el mismo usuario que estamos actualizando
        if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(usuario.getId())) {
            throw new RuntimeException("Ya existe un usuario del tipo '" + usuario.getClass().getSimpleName() + "' con este email.");
        }

        return usuarioRepositorio.save(usuario);
    }

    // 🔹 Eliminar usuario por ID
    public void eliminarUsuario(Long id) {
        usuarioRepositorio.deleteById(id);
    }

    // 🔹 Obtener imagen de perfil predeterminada
    public byte[] getDefaultProfileImage() throws IOException {
        Resource defaultImage = new ClassPathResource("static/images/IconoUsuarioPorDefecto.png");
        try (var inputStream = defaultImage.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }
}
