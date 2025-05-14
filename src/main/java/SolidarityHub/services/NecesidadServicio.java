package SolidarityHub.services;

import SolidarityHub.models.Necesidad;
import SolidarityHub.repository.NecesidadRepositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class NecesidadServicio {
    
    private final NecesidadRepositorio necesidadRepositorio;
    private final ApplicationEventPublisher eventPublisher;

    public NecesidadServicio(NecesidadRepositorio necesidadRepositorio, ApplicationEventPublisher eventPublisher) {
        this.necesidadRepositorio = necesidadRepositorio;
        this.eventPublisher = eventPublisher;
    }

    public List<Necesidad> listarNecesidades() {
        return necesidadRepositorio.findAll();
    }

    public Optional<Necesidad> obtenerNecesidadPorId(Long id) {
        return necesidadRepositorio.findById(id);
    }

    public Necesidad guardarNecesidad(Necesidad necesidad) {
        // Tomamos el momento actual
        LocalDateTime ahora = LocalDateTime.now();
     
        // Switch sobre el enum Urgencia, no sobre un String
        switch (necesidad.getUrgencia()) {
            case ALTA:
                // añadimos días a un LocalDateTime
                necesidad.setFechaInicio(ahora.plusDays(1));
                break;
            case MEDIA:
                necesidad.setFechaInicio(ahora.plusDays(3));
                break;
            case BAJA:
                necesidad.setFechaInicio(ahora.plusWeeks(1));
                break;
            default:
                necesidad.setFechaInicio(ahora);
                break;
        }
        // Guardar la necesidad en la base de datos
        Necesidad necesidadGuardada = necesidadRepositorio.save(necesidad);
        // Publicar un evento para que otros servicios puedan reaccionar
        eventPublisher.publishEvent(necesidadGuardada);
        return necesidadGuardada;
    }

    public void eliminarNecesidad(Long id) {
        necesidadRepositorio.deleteById(id);
    }
}
