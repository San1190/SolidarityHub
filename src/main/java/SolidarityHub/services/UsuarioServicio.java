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

    // Método para obtener usuario por email y tipo
    public Usuario buscarUsuarioPorEmailYTipo(String email, String tipoUsuario) {
        return usuarioRepositorio.findByEmailAndTipoUsuario(email, tipoUsuario)
                .orElse(null);  // Retorna null si no se encuentra
    }

    // Otros métodos que ya tienes
    public List<Usuario> listarUsuarios() {
        return usuarioRepositorio.findAll();
    }

    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepositorio.findById(id).orElse(null);
    }

    public Usuario guardarUsuario(Usuario usuario) {
        // Obtener tipo de usuario
        String tipoUsuario = usuario.getTipoUsuario();

        // Comprobar si ya existe un usuario con el mismo email y tipo
        Optional<Usuario> usuarioExistente = usuarioRepositorio.findByEmailAndTipoUsuario(usuario.getEmail(), tipoUsuario);

        if (usuarioExistente.isPresent()) {
            throw new RuntimeException("Ya existe un usuario del tipo '" + tipoUsuario + "' con este email.");
        }

        return usuarioRepositorio.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepositorio.deleteById(id);
    }

    public byte[] getDefaultProfileImage() throws IOException {
        Resource defaultImage = new ClassPathResource("static/images/IconoUsuarioPorDefecto.png");
        try (var inputStream = defaultImage.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }
}