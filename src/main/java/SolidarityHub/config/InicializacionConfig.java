package SolidarityHub.config;

import SolidarityHub.models.Necesidad;
import SolidarityHub.repository.NecesidadRepositorio;
import SolidarityHub.services.AutomatizacionServicio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class InicializacionConfig {

    private final NecesidadRepositorio necesidadRepositorio;
    private final ApplicationEventPublisher eventPublisher;

    public InicializacionConfig(NecesidadRepositorio necesidadRepositorio, ApplicationEventPublisher eventPublisher) {
        this.necesidadRepositorio = necesidadRepositorio;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Este bean se ejecuta al iniciar la aplicación y procesa todas las necesidades existentes
     * para crear tareas automáticamente si no existen, publicando eventos para cada necesidad.
     */
    @Bean
    public CommandLineRunner inicializarTareas() {
        return args -> {
            System.out.println("Procesando necesidades existentes para crear tareas automáticamente...");
            List<Necesidad> necesidades = necesidadRepositorio.findAll();
            for (Necesidad necesidad : necesidades) {
                // Publicar un evento para cada necesidad existente
                eventPublisher.publishEvent(necesidad);
            }
            System.out.println("Procesamiento de necesidades completado.");
        };
    }
}