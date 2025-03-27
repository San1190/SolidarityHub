package SolidarityHub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import SolidarityHub.models.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email); 
    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.getTipoUsuario() = :tipoUsuario")
    Optional<Usuario> findByEmailAndTipoUsuario(@Param("email") String email, @Param("tipoUsuario") String tipoUsuario);

}