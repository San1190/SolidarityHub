package SolidarityHub.services;

import static org.junit.jupiter.api.Assertions.*;

import SolidarityHub.models.Necesidad;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Voluntario;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

class VerificadorDisponibilidadTest {

    private final AsignacionTareaServicio servicio = new AsignacionTareaServicio();

    private Voluntario crearVoluntario(List<String> diasDisponibles, String turno) {
        return new Voluntario(
                "12345678A",
                "Juan",
                "Pérez",
                "juan@example.com",
                "password",
                "600000000",
                "Calle Falsa 123",
                null,
                Collections.emptyList(),
                diasDisponibles,
                turno,
                10.0
        );
    }

    private Tarea crearTarea(LocalDateTime fechaInicio) {
        return new Tarea(
                "Reparto",
                "Reparto de alimentos",
                Necesidad.TipoNecesidad.ALIMENTACION,
                "Centro de ayuda",
                5,
                fechaInicio,
                "Punto A",
                "MAÑANA",
                Tarea.EstadoTarea.PREPARADA,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                0.0,
                0.0
        );
    }

    @Test
    void testDisponibleMañanaLunes() {
        Voluntario voluntario = crearVoluntario(List.of("MONDAY"), "MAÑANA");
        Tarea tarea = crearTarea(LocalDateTime.of(2025, 6, 2, 9, 0)); // Lunes 9 AM
        assertTrue(servicio.verificarDisponibilidadHorario(voluntario, tarea));
    }

    @Test
    void testNoDisponiblePorTurno() {
        Voluntario voluntario = crearVoluntario(List.of("MONDAY"), "TARDE");
        Tarea tarea = crearTarea(LocalDateTime.of(2025, 6, 2, 9, 0)); // Lunes 9 AM
        assertFalse(servicio.verificarDisponibilidadHorario(voluntario, tarea));
    }

    @Test
    void testTareaSinFecha() {
        Voluntario voluntario = crearVoluntario(List.of("MONDAY"), "MAÑANA");
        Tarea tarea = crearTarea(null);
        assertTrue(servicio.verificarDisponibilidadHorario(voluntario, tarea));
    }
    @Test
    void testVoluntarioSinDiasDisponibles() {
        Voluntario voluntario = crearVoluntario(null, "MAÑANA");
        Tarea tarea = crearTarea(LocalDateTime.of(2025, 6, 2, 9, 0)); // Lunes
        assertFalse(servicio.verificarDisponibilidadHorario(voluntario, tarea));
    }

    @Test
    void testVoluntarioSinTurno() {
        Voluntario voluntario = crearVoluntario(List.of("MONDAY"), null);
        Tarea tarea = crearTarea(LocalDateTime.of(2025, 6, 2, 10, 0)); // Lunes 10 AM
        assertTrue(servicio.verificarDisponibilidadHorario(voluntario, tarea));
    }

    @Test
    void testDiaEnMinusculas() {
        Voluntario voluntario = crearVoluntario(List.of("monday"), "MAÑANA");
        Tarea tarea = crearTarea(LocalDateTime.of(2025, 6, 2, 10, 0)); // Lunes
        assertFalse(servicio.verificarDisponibilidadHorario(voluntario, tarea)); // si no haces toUpperCase()
    }

    @Test
    void testLímiteInicioTarde() {
        Voluntario voluntario = crearVoluntario(List.of("MONDAY"), "TARDE");
        Tarea tarea = crearTarea(LocalDateTime.of(2025, 6, 2, 14, 0)); // justo a las 14:00
        assertTrue(servicio.verificarDisponibilidadHorario(voluntario, tarea));
    }

    @Test
    void testLímiteFinMañana() {
        Voluntario voluntario = crearVoluntario(List.of("MONDAY"), "MAÑANA");
        Tarea tarea = crearTarea(LocalDateTime.of(2025, 6, 2, 13, 59)); // 13:59
        assertTrue(servicio.verificarDisponibilidadHorario(voluntario, tarea));
    }

    @Test
    void testLímiteNocheInicio() {
        Voluntario voluntario = crearVoluntario(List.of("MONDAY"), "NOCHE");
        Tarea tarea = crearTarea(LocalDateTime.of(2025, 6, 2, 20, 0)); // justo 20:00
        assertTrue(servicio.verificarDisponibilidadHorario(voluntario, tarea));
    }

    @Test
    void testNocheMadrugada() {
        Voluntario voluntario = crearVoluntario(List.of("MONDAY"), "NOCHE");
        Tarea tarea = crearTarea(LocalDateTime.of(2025, 6, 2, 3, 0)); // 3 AM
        assertTrue(servicio.verificarDisponibilidadHorario(voluntario, tarea));
    }

    @Test
    void testTurnoDesconocido() {
        Voluntario voluntario = crearVoluntario(List.of("MONDAY"), "DESCONOCIDO");
        Tarea tarea = crearTarea(LocalDateTime.of(2025, 6, 2, 10, 0)); // Lunes 10 AM
        assertTrue(servicio.verificarDisponibilidadHorario(voluntario, tarea)); // se asume disponibilidad
    }
}