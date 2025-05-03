package SolidarityHub.repository;

import java.util.List;
import java.util.Optional;

import SolidarityHub.models.Habilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email); 
    Optional<Usuario> findByEmail(String email);


    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND TYPE(u) = :tipoUsuario")
    Optional<Usuario> findByEmailAndTipoUsuario(@Param("email") String email, @Param("tipoUsuario") Class<? extends Usuario> tipoUsuario);
    
    // Consulta para cargar un Voluntario con sus colecciones (eager loading)
    @Query("SELECT v FROM Voluntario v LEFT JOIN FETCH v.diasDisponibles WHERE v.id = :id")
    Optional<Voluntario> findVoluntarioByIdWithDiasDisponibles(@Param("id") Long id);

    @Query("SELECT v FROM Voluntario v WHERE EXISTS (SELECT 1 FROM v.habilidades h WHERE h IN :habilidades)")
    List<Voluntario> findByHabilidadesIn(@Param("habilidades") List<Habilidad> habilidades);
}
