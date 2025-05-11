package SolidarityHub.repository;

import SolidarityHub.models.AreaCritica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaCriticaRepositorio extends JpaRepository<AreaCritica, Long> {
    List<AreaCritica> findByCreadorId(Long creadorId);
}