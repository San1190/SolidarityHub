package SolidarityHub.services;

import SolidarityHub.models.Habilidad;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Voluntario;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VerificadorHabilidadesTest {

    private final AsignacionTareaServicio servicio = new AsignacionTareaServicio();
    private Voluntario crearVoluntarioConHabilidades(List<Habilidad> habilidades, List<String> diasDisponibles, String turnoDisponibilidad) {
        String dni = "12345678A";
        String nombre = "Test";
        String apellidos = "User";
        String email = "test@example.com";
        String password = "password";
        String telefono = "123456789";
        String direccion = "Calle Falsa 123";
        byte[] foto = null;
        Double radioAccion = 10.0;

        return new Voluntario(
                dni,
                nombre,
                apellidos,
                email,
                password,
                telefono,
                direccion,
                foto,
                habilidades,
                diasDisponibles,
                turnoDisponibilidad,
                radioAccion
        );
    }

    private Tarea crearTareaConHabilidadesRequeridas(List<Habilidad> habilidadesRequeridas) {
        return new Tarea(
                "Nombre Tarea",
                "Descripción",
                null,
                "Ubicación",
                1,
                LocalDateTime.of(2025, 6, 2, 9, 0),
                "Punto Encuentro",
                "MAÑANA",
                Tarea.EstadoTarea.PREPARADA,
                null,
                null,
                null,
                habilidadesRequeridas,
                null,
                0.0,
                0.0
        );
    }

    @Test
    void testTareaSinHabilidadesRequeridas() {
        Voluntario voluntario = crearVoluntarioConHabilidades(List.of(Habilidad.COCINA), List.of("MONDAY"), "MAÑANA");
        Tarea tarea = crearTareaConHabilidadesRequeridas(Collections.emptyList());

        assertTrue(servicio.verificarHabilidadesCompatibles(voluntario, tarea));
    }

    @Test
    void testVoluntarioSinHabilidades() {
        Voluntario voluntario = crearVoluntarioConHabilidades(Collections.emptyList(), List.of("MONDAY"), "MAÑANA");
        Tarea tarea = crearTareaConHabilidadesRequeridas(List.of(Habilidad.CARPINTERIA));

        assertFalse(servicio.verificarHabilidadesCompatibles(voluntario, tarea));
    }

    @Test
    void testVoluntarioConHabilidadCompatible() {
        Voluntario voluntario = crearVoluntarioConHabilidades(List.of(Habilidad.COCINA, Habilidad.LIMPIEZA), List.of("MONDAY"), "MAÑANA");
        Tarea tarea = crearTareaConHabilidadesRequeridas(List.of(Habilidad.LIMPIEZA));

        assertTrue(servicio.verificarHabilidadesCompatibles(voluntario, tarea));
    }

    @Test
    void testVoluntarioSinHabilidadRequerida() {
        Voluntario voluntario = crearVoluntarioConHabilidades(List.of(Habilidad.LAVANDERIA), List.of("MONDAY"), "MAÑANA");
        Tarea tarea = crearTareaConHabilidadesRequeridas(List.of(Habilidad.TRANSPORTE_PERSONAS));

        assertFalse(servicio.verificarHabilidadesCompatibles(voluntario, tarea));
    }

    @Test
    void testConHabilidadesNulasEnListas() {
        List<Habilidad> habilidadesVoluntario = new ArrayList<>();
        habilidadesVoluntario.add(null);
        habilidadesVoluntario.add(Habilidad.CARPINTERIA);

        List<Habilidad> habilidadesTarea = new ArrayList<>();
        habilidadesTarea.add(null);
        habilidadesTarea.add(Habilidad.CARPINTERIA);

        Voluntario voluntario = crearVoluntarioConHabilidades(habilidadesVoluntario, List.of("MONDAY"), "MAÑANA");
        Tarea tarea = crearTareaConHabilidadesRequeridas(habilidadesTarea);

        assertTrue(servicio.verificarHabilidadesCompatibles(voluntario, tarea));
    }
}