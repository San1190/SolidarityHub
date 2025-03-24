package SolidarityHub.services;

import SolidarityHub.models.Necesidad;
import SolidarityHub.repository.NecesidadRepositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class NecesidadServicio {
    
    private final NecesidadRepositorio necesidadRepositorio;

    public NecesidadServicio(NecesidadRepositorio necesidadRepositorio) {
        this.necesidadRepositorio = necesidadRepositorio;
    }

    public List<Necesidad> listarNecesidades() {
        return necesidadRepositorio.findAll();
    }

    public Optional<Necesidad> obtenerNecesidadPorId(Long id) {
        return necesidadRepositorio.findById(id);
    }

    public Necesidad guardarNecesidad(Necesidad necesidad) {
        return necesidadRepositorio.save(necesidad);
    }

    public void eliminarNecesidad(Long id) {
        necesidadRepositorio.deleteById(id);
    }

    


    
}
