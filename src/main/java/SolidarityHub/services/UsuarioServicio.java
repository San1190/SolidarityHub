package SolidarityHub.services;

//import SolidarityHub.factories.UsuarioFactory;
import SolidarityHub.models.Usuario;
import SolidarityHub.repository.UsuarioRepositorio;

import java.util.List;
import org.springframework.stereotype.Service;


@Service
public class UsuarioServicio {
    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepositorio.findAll();
    }

    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepositorio.findById(id).orElse(null);
    }

    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepositorio.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepositorio.deleteById(id);
    }


}