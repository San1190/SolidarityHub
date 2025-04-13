package SolidarityHub.services;

import SolidarityHub.models.Recursos;
import SolidarityHub.models.Recursos.TipoRecurso;

import SolidarityHub.repository.RecursoRepositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class RecursoServicio {

    private final RecursoRepositorio recursoRepositorio;

    public RecursoServicio(RecursoRepositorio recursoRepositorio) {
        this.recursoRepositorio = recursoRepositorio;
    }

    public List<Recursos> listarRecursos() {
        return recursoRepositorio.findAll();
    }

    public Optional<Recursos> obtenerRecursoPorId(Long id) {
        return recursoRepositorio.findById(id);
    }

    public Recursos guardarRecursos(Recursos recurso) {
        return recursoRepositorio.save(recurso);
    }

    public Recursos actualizarRecurso(Recursos recurso) {
        return recursoRepositorio.save(recurso);
    }

    public void eliminarRecurso(Long id) {
        recursoRepositorio.deleteById(id);
    }

    public List<Recursos> filtrarPorTipo(TipoRecurso tipoRecurso) {
        return recursoRepositorio.findByTipoRecurso(tipoRecurso);
    }
}