package SolidarityHub.services;

import SolidarityHub.models.Afectado;
import SolidarityHub.models.Habilidad;
import SolidarityHub.models.Necesidad;
import SolidarityHub.models.Recursos;
import SolidarityHub.models.Recursos.EstadoRecurso;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Tarea.EstadoTarea;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import SolidarityHub.repository.UsuarioRepositorio;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

@Service
public class AutomatizacionServicio implements ApplicationListener<ContextRefreshedEvent> {

    private final TareaServicio tareaServicio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RecursoServicio recursoServicio;

    public AutomatizacionServicio(TareaServicio tareaServicio, UsuarioRepositorio usuarioRepositorio, RecursoServicio recursoServicio) {
        this.tareaServicio = tareaServicio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.recursoServicio = recursoServicio;
    }

    /**
     * Este método se ejecuta cuando se inicia la aplicación.
     * Ya no necesitamos procesar necesidades aquí, ya que lo haremos a través de eventos.
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // La inicialización ahora se maneja en InicializacionConfig
    }
    
    /**
     * Este método escucha eventos de tipo Necesidad y crea tareas automáticamente.
     * @param necesidad La necesidad que se ha creado o actualizado
     */
    @EventListener(Necesidad.class)
    @Transactional
    public void handleNecesidadEvent(Necesidad necesidad) {
        // Primero procesamos la necesidad para crear una tarea
        try {
            if (necesidad == null) {
                System.err.println("Error: Se recibió un evento de Necesidad con valor nulo");
                return;
            }
            
            if (necesidad.getDescripcion() == null) {
                System.err.println("Error: La necesidad recibida no tiene descripción");
                return;
            }
            
            System.out.println("Evento de Necesidad recibido: " + necesidad.getDescripcion());
            crearTareaDesdeNecesidad(necesidad);
        } catch (Exception e) {
            System.err.println("Error al procesar evento de Necesidad: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Asigna automáticamente recursos disponibles a una tarea según su tipo de necesidad
     * @param tarea La tarea a la que se asignarán recursos
     */
    @Transactional
    public void asignarRecursosAutomaticamente(Tarea tarea) {
        try {
            if (tarea == null || tarea.getTipo() == null) {
                System.err.println("Error: La tarea no tiene tipo definido para asignar recursos");
                return;
            }
            
            System.out.println("Buscando recursos disponibles para la tarea: " + tarea.getNombre());
            
            // Convertir el tipo de necesidad de la tarea al tipo de recurso correspondiente
            String tipoNecesidadStr = tarea.getTipo().name();
            Recursos.TipoRecurso tipoRecursoNecesario = null;
            
            try {
                tipoRecursoNecesario = Recursos.TipoRecurso.valueOf(tipoNecesidadStr);
            } catch (IllegalArgumentException e) {
                System.err.println("No existe un tipo de recurso equivalente para la necesidad: " + tipoNecesidadStr);
                return;
            }
            
            // Buscar recursos disponibles del tipo necesario
            List<Recursos> recursosDisponibles = recursoServicio.filtrarPorTipo(tipoRecursoNecesario).stream()
                .filter(r -> r.getEstado() == EstadoRecurso.DISPONIBLE)
                .toList();
            
            System.out.println("Recursos disponibles encontrados: " + recursosDisponibles.size());
            
            if (recursosDisponibles.isEmpty()) {
                System.out.println("No hay recursos disponibles del tipo: " + tipoRecursoNecesario);
                return;
            }
            
            // Asignar el primer recurso disponible a la tarea
            Recursos recursoParaAsignar = recursosDisponibles.get(0);
            recursoParaAsignar.setEstado(EstadoRecurso.ASIGNADO);
            recursoParaAsignar.setTareaAsignada(tarea);
            
            recursoServicio.actualizarRecurso(recursoParaAsignar);
            
            System.out.println("Recurso asignado automáticamente a la tarea: " + 
                              recursoParaAsignar.getDescripcion() + " -> " + tarea.getNombre());
            
        } catch (Exception e) {
            System.err.println("Error al asignar recursos automáticamente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Este método escucha eventos de tipo Usuario y crea tareas automáticamente si es necesario.
     * Si el usuario es un Afectado, se crearán tareas basadas en sus necesidades.
     * @param usuario El usuario que se ha creado o actualizado
     */
    @EventListener(Usuario.class)
    @Transactional
    public void handleUsuarioEvent(Usuario usuario) {
        try {
            if (usuario == null) {
                System.err.println("Error: Se recibió un evento de Usuario con valor nulo");
                return;
            }
            
            System.out.println("Evento de Usuario recibido: " + usuario.getNombre());
            
            // Solo procesamos usuarios de tipo Afectado
            if (usuario instanceof Afectado) {
                Afectado afectado = (Afectado) usuario;
                
                // Si el afectado tiene necesidades asociadas, creamos tareas para cada una
                if (afectado.getNecesidades() != null && !afectado.getNecesidades().isEmpty()) {
                    System.out.println("Procesando " + afectado.getNecesidades().size() + " necesidades para el afectado: " + afectado.getNombre());
                    for (Necesidad necesidad : afectado.getNecesidades()) {
                        if (necesidad != null) {
                            crearTareaDesdeNecesidad(necesidad);
                        } else {
                            System.err.println("Error: Se encontró una necesidad nula en la lista de necesidades del afectado");
                        }
                    }
                } else {
                    // Si el afectado no tiene necesidades específicas, podríamos crear una tarea genérica
                    // basada en el registro de un nuevo afectado, si es necesario
                    System.out.println("Nuevo afectado registrado: " + afectado.getNombre() + ". No tiene necesidades específicas.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al procesar evento de Usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Este método se llama cuando se guarda una nueva necesidad.
     * Crea automáticamente una tarea basada en la necesidad.
     * @param necesidad La necesidad para la que se creará una tarea
     */
    @Transactional
    public void crearTareaDesdeNecesidad(Necesidad necesidad) {
        try {
            // Validar que la necesidad tenga los datos necesarios
            if (necesidad == null || necesidad.getDescripcion() == null || necesidad.getTipoNecesidad() == null) {
                System.out.println("Error: La necesidad no tiene los datos requeridos para crear una tarea");
                return;
            }
            
            System.out.println("Creando tarea para necesidad: " + necesidad.getDescripcion());
            
            // Verificar si la necesidad ya tiene una tarea asociada
            List<Tarea> tareasExistentes = tareaServicio.filtrarPorTipo(necesidad.getTipoNecesidad());
            System.out.println("Tareas existentes del mismo tipo: " + tareasExistentes.size());
            
            boolean tareaExiste = false;
            Tarea tareaAsociada = null;
            for (Tarea tarea : tareasExistentes) {
                // Verificar si ya existe una tarea con descripción similar
                if (tarea.getDescripcion() != null && 
                    tarea.getDescripcion().contains(necesidad.getDescripcion())) {
                    tareaExiste = true;
                    tareaAsociada = tarea;
                    System.out.println("Ya existe una tarea similar: " + tarea.getDescripcion());
                    break;
                }
            }
            
            if (!tareaExiste) {
                System.out.println("Creando nueva tarea para la necesidad...");
                // Crear una nueva tarea basada en la necesidad
                Tarea nuevaTarea = new Tarea();
                nuevaTarea.setNombre("Tarea para " + necesidad.getTipoNecesidad().name());
                nuevaTarea.setDescripcion("Atender necesidad: " + necesidad.getDescripcion());
                nuevaTarea.setTipo(necesidad.getTipoNecesidad());
                nuevaTarea.setLocalizacion(necesidad.getUbicacion() != null ? necesidad.getUbicacion() : "Sin ubicación especificada");
                // Asignar la fecha de inicio sin la hora (aunque ya viene sin hora desde el DatePicker)
                nuevaTarea.setFechaInicio(necesidad.getFechaInicio() != null ? necesidad.getFechaInicio() : LocalDateTime.now().toLocalDate().atStartOfDay());
                nuevaTarea.setCreador(necesidad.getCreador());
                
                
                ;
                switch (necesidad.getUrgencia()) {
                    case BAJA:
                        nuevaTarea.setFechaFin(necesidad.getFechaInicio()!= null? necesidad.getFechaInicio().plusDays(1) : LocalDateTime.now().toLocalDate().atStartOfDay().plusDays(3));
                        nuevaTarea.setNumeroVoluntariosNecesarios(2);
                        break;
                    case MEDIA:
                        nuevaTarea.setFechaFin(necesidad.getFechaInicio()!= null? necesidad.getFechaInicio().plusDays(2) : LocalDateTime.now().toLocalDate().atStartOfDay().plusDays(2));
                        nuevaTarea.setNumeroVoluntariosNecesarios(3);
                        break;
                    case ALTA:
                        nuevaTarea.setFechaFin(necesidad.getFechaInicio()!= null? necesidad.getFechaInicio().plusDays(3) : LocalDateTime.now().toLocalDate().atStartOfDay().plusDays(1));
                        nuevaTarea.setNumeroVoluntariosNecesarios(5);
                        break;
                }
                // Establecer estado inicial
                nuevaTarea.setEstado(EstadoTarea.PREPARADA);
                
                // Inicializar listas vacías
                nuevaTarea.setAfectados(new ArrayList<>());
                nuevaTarea.setVoluntariosAsignados(new ArrayList<>());
                
                // Guardar la tarea
                Tarea tareaGuardada = tareaServicio.guardarTarea(nuevaTarea);
                System.out.println("Tarea creada con éxito. ID: " + tareaGuardada.getId());
                
                // Asignar voluntarios automáticamente
                asignarVoluntariosAutomaticamente(tareaGuardada);
            } else if (tareaAsociada != null) {
                // Si la tarea ya existe, verificamos si necesita recursos
                asignarRecursosAutomaticamente(tareaAsociada);
            
            } else {
                System.out.println("No se creó tarea porque ya existe una similar.");
            }
        } catch (Exception e) {

            System.err.println("Error al crear tarea desde necesidad: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Método para asignar automáticamente voluntarios a una tarea basado en sus habilidades
     * @param tarea La tarea a la que se asignarán voluntarios
     */
    @Transactional
    public void asignarVoluntariosAutomaticamente(Tarea tarea) {
        try {
            if (tarea == null || tarea.getTipo() == null) {
                System.out.println("Error: La tarea no tiene los datos requeridos para asignar voluntarios");
                return;
            }
            
            System.out.println("Asignando voluntarios automáticamente a la tarea: " + tarea.getNombre());
            
            // Mapeo entre tipos de necesidad y habilidades requeridas
            Map<Necesidad.TipoNecesidad, Habilidad> mapeoHabilidades = new HashMap<>();
            mapeoHabilidades.put(Necesidad.TipoNecesidad.PRIMEROS_AUXILIOS, Habilidad.PRIMEROS_AUXILIOS);
            mapeoHabilidades.put(Necesidad.TipoNecesidad.ALIMENTACION, Habilidad.COCINA);
            mapeoHabilidades.put(Necesidad.TipoNecesidad.ALIMENTACION_BEBE, Habilidad.COCINA);
            mapeoHabilidades.put(Necesidad.TipoNecesidad.SERVICIO_LIMPIEZA, Habilidad.LIMPIEZA);
            mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_PSICOLOGICA, Habilidad.AYUDA_PSICOLOGICA);
            mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_CARPINTERIA, Habilidad.CARPINTERIA);
            mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_ELECTRICIDAD, Habilidad.ELECTICISTA);
            mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_FONTANERIA, Habilidad.FONTANERIA);
            
            // Obtener la habilidad requerida para esta tarea
            Habilidad habilidadRequerida = mapeoHabilidades.get(tarea.getTipo());
            
            if (habilidadRequerida != null) {
                // Obtener todos los voluntarios
                List<Usuario> usuarios = usuarioRepositorio.findAll();
                List<Voluntario> voluntariosCompatibles = new ArrayList<>();
                
                // Filtrar solo voluntarios con la habilidad requerida
                for (Usuario usuario : usuarios) {
                    if (usuario instanceof Voluntario) {
                        Voluntario voluntario = (Voluntario) usuario;
                        if (voluntario.getHabilidades() != null && voluntario.getHabilidades().contains(habilidadRequerida)) {
                            voluntariosCompatibles.add(voluntario);
                        }
                    }
                }
                
                System.out.println("Encontrados " + voluntariosCompatibles.size() + " voluntarios con la habilidad " + habilidadRequerida);
                
                // Asignar voluntarios hasta alcanzar el número necesario
                int voluntariosNecesarios = tarea.getNumeroVoluntariosNecesarios();
                int voluntariosAsignados = 0;
                
                List<Voluntario> asignados = new ArrayList<>();
                for (Voluntario voluntario : voluntariosCompatibles) {
                    if (voluntariosAsignados < voluntariosNecesarios) {
                        asignados.add(voluntario);
                        voluntariosAsignados++;
                        System.out.println("Asignado voluntario: " + voluntario.getNombre());
                    } else {
                        break;
                    }
                }
                
                // Actualizar la tarea con los voluntarios asignados
                tarea.setVoluntariosAsignados(asignados);
                tareaServicio.actualizarTarea(tarea);
                
                System.out.println("Se han asignado " + voluntariosAsignados + " voluntarios a la tarea");
            } else {
                System.out.println("No se encontró una habilidad correspondiente para el tipo de necesidad: " + tarea.getTipo());
            }
        } catch (Exception e) {
            System.err.println("Error al asignar voluntarios automáticamente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Método para emparejar todas las tareas existentes con voluntarios
     * Útil para ejecutar manualmente o al iniciar la aplicación
     */
    @Transactional
    public void emparejarTodasLasTareas() {
        try {
            List<Tarea> tareas = tareaServicio.listarTareas();
            System.out.println("Emparejando " + tareas.size() + " tareas con voluntarios");
            
            for (Tarea tarea : tareas) {
                if (tarea.getVoluntariosAsignados() == null || tarea.getVoluntariosAsignados().isEmpty()) {
                    asignarVoluntariosAutomaticamente(tarea);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al emparejar todas las tareas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}