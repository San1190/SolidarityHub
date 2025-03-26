package SolidarityHub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import SolidarityHub.models.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email); 
    Optional<Usuario> findByEmailAndTipoUsuario(String email, String tipoUsuario);
}
