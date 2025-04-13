package SolidarityHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import SolidarityHub.models.Recursos;
import SolidarityHub.models.Recursos.TipoRecurso;

import java.util.List;

public interface RecursoRepositorio extends JpaRepository<Recursos, Long> {
    // Método para encontrar recursos por tipo
    List<Recursos> findByTipoRecurso(TipoRecurso tipoRecurso);
}
