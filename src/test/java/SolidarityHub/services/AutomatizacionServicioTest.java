package SolidarityHub.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import SolidarityHub.models.*;
import SolidarityHub.repository.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class AsignacionTareaServicioTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private TareaServicio tareaServicio;

    @InjectMocks
    private AutomatizacionServicio servicio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Helper para crear voluntarios reales con habilidades
    private Voluntario crearVoluntarioConHabilidad(String nombre, Habilidad habilidad) {
        Voluntario v = new Voluntario();
        v.setNombre(nombre);
        v.setHabilidades(List.of(habilidad));
        return v;
    }

    // Helper para crear tareas reales
    private Tarea crearTareaConTipoYNumVoluntarios(Necesidad.TipoNecesidad tipo, int num) {
        Tarea tarea = new Tarea();
        tarea.setTipo(tipo);
        tarea.setNumeroVoluntariosNecesarios(num);
        tarea.setNombre("Tarea Test");
        return tarea;
    }

    @Test
    void asignarVoluntariosAutomaticamente_AsignaCorrectamente() {
        Voluntario v1 = crearVoluntarioConHabilidad("Voluntario1", Habilidad.COCINA);
        Voluntario v2 = crearVoluntarioConHabilidad("Voluntario2", Habilidad.COCINA);
        List<Usuario> usuarios = List.of(v1, v2);
        when(usuarioRepositorio.findAll()).thenReturn(usuarios);

        Tarea tarea = crearTareaConTipoYNumVoluntarios(Necesidad.TipoNecesidad.ALIMENTACION, 1);

        servicio.asignarVoluntariosAutomaticamente(tarea);

        assertNotNull(tarea.getVoluntariosAsignados());
        assertEquals(1, tarea.getVoluntariosAsignados().size());
        assertTrue(
                tarea.getVoluntariosAsignados().contains(v1) ||
                        tarea.getVoluntariosAsignados().contains(v2)
        );

        verify(tareaServicio, times(1)).actualizarTarea(tarea);
    }

    @Test
    void asignarVoluntariosAutomaticamente_TareaNull_NoAsigna() {
        servicio.asignarVoluntariosAutomaticamente(null);
        verifyNoInteractions(usuarioRepositorio, tareaServicio);
    }

    @Test
    void asignarVoluntariosAutomaticamente_TipoNull_NoAsigna() {
        Tarea tarea = crearTareaConTipoYNumVoluntarios(null, 2);
        servicio.asignarVoluntariosAutomaticamente(tarea);
        verifyNoInteractions(usuarioRepositorio, tareaServicio);
    }

    @Test
    void asignarVoluntariosAutomaticamente_SinVoluntariosConHabilidad() {
        Voluntario v = crearVoluntarioConHabilidad("Voluntario", Habilidad.LIMPIEZA);
        when(usuarioRepositorio.findAll()).thenReturn(List.of(v));

        Tarea tarea = crearTareaConTipoYNumVoluntarios(Necesidad.TipoNecesidad.ALIMENTACION, 1);

        servicio.asignarVoluntariosAutomaticamente(tarea);

        assertTrue(tarea.getVoluntariosAsignados().isEmpty());
        verify(tareaServicio, times(1)).actualizarTarea(tarea);
    }

    @Test
    void asignarVoluntariosAutomaticamente_TipoNoMapeado() {
        Tarea tarea = crearTareaConTipoYNumVoluntarios(Necesidad.TipoNecesidad.MEDICAMENTOS, 1);

        servicio.asignarVoluntariosAutomaticamente(tarea);

        verifyNoInteractions(usuarioRepositorio, tareaServicio);
    }

    @Test
    void asignarVoluntariosAutomaticamente_NumeroVoluntariosCero() {
        Voluntario v = crearVoluntarioConHabilidad("Voluntario", Habilidad.COCINA);
        when(usuarioRepositorio.findAll()).thenReturn(List.of(v));

        Tarea tarea = crearTareaConTipoYNumVoluntarios(Necesidad.TipoNecesidad.ALIMENTACION, 0);

        servicio.asignarVoluntariosAutomaticamente(tarea);

        assertTrue(tarea.getVoluntariosAsignados().isEmpty());
        verify(tareaServicio, times(1)).actualizarTarea(tarea);
    }
}
