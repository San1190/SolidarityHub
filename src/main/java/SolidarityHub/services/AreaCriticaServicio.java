package SolidarityHub.services;

import SolidarityHub.models.AreaCritica;
import SolidarityHub.repository.AreaCriticaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AreaCriticaServicio {

    private final AreaCriticaRepositorio areaCriticaRepositorio;

    @Autowired
    public AreaCriticaServicio(AreaCriticaRepositorio areaCriticaRepositorio) {
        this.areaCriticaRepositorio = areaCriticaRepositorio;
    }

    /**
     * Obtiene todas las áreas críticas
     * @return Lista de áreas críticas
     */
    public List<AreaCritica> listarAreasCriticas() {
        return areaCriticaRepositorio.findAll();
    }

    /**
     * Obtiene un área crítica por su ID
     * @param id ID del área crítica
     * @return Área crítica opcional
     */
    public Optional<AreaCritica> obtenerAreaCriticaPorId(Long id) {
        return areaCriticaRepositorio.findById(id);
    }

    /**
     * Guarda un área crítica
     * @param areaCritica Área crítica a guardar
     * @return Área crítica guardada
     */
    public AreaCritica guardarAreaCritica(AreaCritica areaCritica) {
        if (areaCritica.getFechaCreacion() == null) {
            areaCritica.setFechaCreacion(LocalDateTime.now());
        }
        areaCritica.setFechaActualizacion(LocalDateTime.now());
        return areaCriticaRepositorio.save(areaCritica);
    }

    /**
     * Elimina un área crítica por su ID
     * @param id ID del área crítica a eliminar
     */
    public void eliminarAreaCritica(Long id) {
        areaCriticaRepositorio.deleteById(id);
    }

    /**
     * Obtiene todas las áreas críticas creadas por un usuario específico
     * @param creadorId ID del usuario creador
     * @return Lista de áreas críticas del creador
     */
    public List<AreaCritica> obtenerAreasCriticasPorCreador(Long creadorId) {
        return areaCriticaRepositorio.findByCreadorId(creadorId);
    }
}