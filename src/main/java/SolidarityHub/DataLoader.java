package SolidarityHub;

import SolidarityHub.models.*;
import SolidarityHub.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/*@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepositorio usuarioRepositorio;
    private final NecesidadRepositorio necesidadRepositorio;

    public DataLoader(UsuarioRepositorio usuarioRepositorio, NecesidadRepositorio necesidadRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.necesidadRepositorio = necesidadRepositorio;
    }

    @Override
    public void run(String... args) throws Exception {

        // Crear Necesidades
        Necesidad necesidad1 = new Necesidad(
                Necesidad.TipoNecesidad.ALIMENTACION,
                "Necesito alimentos no perecederos",
                Necesidad.EstadoNecesidad.REGISTRADA,
                Necesidad.Urgencia.ALTA,
                "Madrid, Calle Falsa 123",
                LocalDateTime.now()
        );

        Necesidad necesidad2 = new Necesidad(
                Necesidad.TipoNecesidad.MEDICAMENTOS,
                "Medicamentos para diabetes tipo 2",
                Necesidad.EstadoNecesidad.REGISTRADA,
                Necesidad.Urgencia.MEDIA,
                "Madrid, Calle Verdadera 456",
                LocalDateTime.now()
        );

        // Guardar necesidades
        necesidadRepositorio.saveAll(Arrays.asList(necesidad1, necesidad2));

        // Crear Afectados
       //Afectado afectado1 = new Afectado(
       //        "12345678A", "Juan", "Pérez", "juanperez@gmail.com", "password123", 
       //        "612345678", "Calle Falsa 123", null, 
       //        Arrays.asList(necesidad1) // Asignar necesidad
       //);

       //Afectado afectado2 = new Afectado(
       //        "87654321B", "Ana", "García", "anagarcia@gmail.com", "password456", 
       //        "623456789", "Calle Verdadera 456", null, 
       //        Arrays.asList(necesidad2) // Asignar necesidad
       //);

        // Crear Voluntarios
        List<Habilidad> habilidades = Arrays.asList(
                Habilidad.LIMPIEZA,
                Habilidad.COCINA,
                Habilidad.PRIMEROS_AUXILIOS
        );
        Voluntario voluntario1 = new Voluntario(
                "12378945C", "Carlos", "López", "carloslopez@gmail.com", "password789",
                "634567890", "Calle Real 789", null, 
                habilidades,
                LocalTime.of(9, 0), LocalTime.of(17, 0)
        );

        Voluntario voluntario2 = new Voluntario(
                "98765432D", "Lucía", "Martínez", "luciamartinez@gmail.com", "password012",
                "645678901", "Calle Sol 321", null, 
                habilidades, 
                LocalTime.of(10, 0), LocalTime.of(18, 0)
        );

        // Guardar usuarios
        usuarioRepositorio.saveAll(Arrays.asList(voluntario1, voluntario2));

        System.out.println("Datos de prueba cargados correctamente.");
    }
}
*/