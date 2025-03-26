package SolidarityHub.services;

//import SolidarityHub.factories.UsuarioFactory;
import SolidarityHub.models.Usuario;
import SolidarityHub.repository.UsuarioRepositorio;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.springframework.stereotype.Service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


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
        if(usuario.getFoto() == null) {
            try{
                usuario.setFoto(getDefaultProfileImage());
            } catch (IOException e) {
                e.printStackTrace();
                usuario.setFoto(null);
                throw new RuntimeException("Error al obtener la imagen por defecto", e);
                
            }
        }
        return usuarioRepositorio.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepositorio.deleteById(id);
    }



    public byte[] getDefaultProfileImage() throws IOException {
        Resource defaultImage = new ClassPathResource("static/images/IconoUsuarioPorDefecto.png");
        System.out.println("Ha entrado en getDefaultProfileImage");
        return Files.readAllBytes(defaultImage.getFile().toPath());
    }


}