package SolidarityHub.repository;

import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import org.springframework.data.jpa.repository.JpaRepository;
import SolidarityHub.models.Notificacion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificacionRepositorio extends JpaRepository<Notificacion, Long> {
    @Query("SELECT n FROM Notificacion n WHERE n.usuario = :usuario AND n.estado = :estado")
    List<Notificacion> findByVoluntarioAndEstado(
            @Param("usuario") Usuario usuario,
            @Param("estado") Notificacion.EstadoNotificacion estado
    );
}
