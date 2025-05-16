package SolidarityHub.services;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.ZonaEncuentro;
import SolidarityHub.repository.ZonaEncuentroRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar las zonas de encuentro.
 */
@Service
public class ZonaEncuentroServicio {

    private final ZonaEncuentroRepositorio zonaEncuentroRepositorio;
    private final TareaServicio tareaServicio;

    @Autowired
    public ZonaEncuentroServicio(ZonaEncuentroRepositorio zonaEncuentroRepositorio, TareaServicio tareaServicio) {
        this.zonaEncuentroRepositorio = zonaEncuentroRepositorio;
        this.tareaServicio = tareaServicio;
    }

    /**
     * Obtiene todas las zonas de encuentro con sus tareas precargadas
     * para evitar el error "Could not initialize proxy - no session"
     * @return Lista de todas las zonas de encuentro
     */
    @Transactional(readOnly = true)
    public List<ZonaEncuentro> listarZonasEncuentro() {
        try {
            // Usar el nuevo método que carga las tareas directamente
            return zonaEncuentroRepositorio.findAllWithTareas();
        } catch (Exception e) {
            // Si hay algún error con el método optimizado, usar el método estándar
            // e inicializar las tareas manualmente dentro de la transacción
            List<ZonaEncuentro> zonas = zonaEncuentroRepositorio.findAll();
            
            // Forzar la inicialización de cada tarea dentro de la transacción
            for (ZonaEncuentro zona : zonas) {
                if (zona.getTarea() != null) {
                    try {
                        // Acceder a alguna propiedad de la tarea fuerza su inicialización
                        zona.getTarea().getNombre();
                    } catch (Exception ex) {
                        // Si no se puede inicializar, dejamos la tarea como null para evitar errores
                        System.err.println("Error al inicializar tarea para zona " + zona.getId() + ": " + ex.getMessage());
                    }
                }
            }
            
            return zonas;
        }
    }

    /**
     * Busca una zona de encuentro por su ID con su tarea precargada
     * @param id ID de la zona de encuentro
     * @return Optional con la zona si existe, vacío si no
     */
    @Transactional(readOnly = true)
    public Optional<ZonaEncuentro> obtenerZonaEncuentroPorId(Long id) {
        try {
            // Intentar usar el método optimizado que carga la tarea directamente
            ZonaEncuentro zona = zonaEncuentroRepositorio.findByIdWithTarea(id);
            return Optional.ofNullable(zona);
        } catch (Exception e) {
            // Si hay algún error, usar el método estándar e inicializar la tarea manualmente
            Optional<ZonaEncuentro> optZona = zonaEncuentroRepositorio.findById(id);
            
            if (optZona.isPresent()) {
                ZonaEncuentro zona = optZona.get();
                if (zona.getTarea() != null) {
                    try {
                        // Acceder a alguna propiedad de la tarea fuerza su inicialización
                        zona.getTarea().getNombre();
                    } catch (Exception ex) {
                        // Si no se puede inicializar, cargar la tarea completa desde su servicio
                        Long tareaId = null;
                        try {
                            // Intentar extraer el ID de la tarea
                            tareaId = zona.getTarea().getId();
                            if (tareaId != null) {
                                Optional<Tarea> optTarea = tareaServicio.obtenerTareaPorId(tareaId);
                                optTarea.ifPresent(zona::setTarea);
                            }
                        } catch (Exception e2) {
                            System.err.println("No se pudo recuperar la tarea con ID " + tareaId + " para la zona " + id);
                        }
                    }
                }
            }
            
            return optZona;
        }
    }

    /**
     * Guarda una nueva zona de encuentro
     * @param zonaEncuentro La zona a guardar
     * @return La zona guardada con su ID generado
     */
    @Transactional
    public ZonaEncuentro guardarZonaEncuentro(ZonaEncuentro zonaEncuentro) {
        return zonaEncuentroRepositorio.save(zonaEncuentro);
    }

    /**
     * Actualiza una zona de encuentro existente
     * @param zonaEncuentro La zona con los datos actualizados
     * @return La zona actualizada
     */
    @Transactional
    public ZonaEncuentro actualizarZonaEncuentro(ZonaEncuentro zonaEncuentro) {
        // Verificar que la zona existe
        if (zonaEncuentro.getId() != null && 
            zonaEncuentroRepositorio.existsById(zonaEncuentro.getId())) {
            return zonaEncuentroRepositorio.save(zonaEncuentro);
        }
        throw new IllegalArgumentException("No se puede actualizar una zona que no existe");
    }

    /**
     * Elimina una zona de encuentro y limpia la referencia en la tarea asociada
     * @param id ID de la zona a eliminar
     */
    @Transactional
    public void eliminarZonaEncuentro(Long id) {
        // Obtener la zona primero para acceder a su tarea
        Optional<ZonaEncuentro> optZona = obtenerZonaEncuentroPorId(id);
        if (optZona.isPresent()) {
            ZonaEncuentro zona = optZona.get();
            
            // Si hay una tarea asociada, limpiar su punto de encuentro
            if (zona.getTarea() != null) {
                try {
                    Tarea tarea = zona.getTarea();
                    tarea.setPuntoEncuentro(null); // Limpiar punto de encuentro
                    tareaServicio.actualizarTarea(tarea);
                } catch (Exception e) {
                    // Si hay error al acceder a la tarea, intentar usar el ID
                    try {
                        Long tareaId = zona.getTarea().getId();
                        Optional<Tarea> optTarea = tareaServicio.obtenerTareaPorId(tareaId);
                        if (optTarea.isPresent()) {
                            Tarea tarea = optTarea.get();
                            tarea.setPuntoEncuentro(null);
                            tareaServicio.actualizarTarea(tarea);
                        }
                    } catch (Exception ex) {
                        System.err.println("No se pudo actualizar la tarea asociada a la zona: " + ex.getMessage());
                    }
                }
            }
        }
        
        // Finalmente eliminar la zona
        zonaEncuentroRepositorio.deleteById(id);
    }

    /**
     * Encuentra todas las zonas de encuentro asociadas a una tarea
     * @param tareaId ID de la tarea
     * @return Lista de zonas de encuentro
     */
    @Transactional(readOnly = true)
    public List<ZonaEncuentro> obtenerZonasPorTarea(Long tareaId) {
        return zonaEncuentroRepositorio.findByTareaId(tareaId);
    }
    
    /**
     * Crea una zona de encuentro para una tarea con las coordenadas proporcionadas.
     * Verifica que la tarea no tenga ya una zona de encuentro asignada.
     * @param tareaId ID de la tarea asociada
     * @param coordenadas String con las coordenadas en formato "lat1,lng1;lat2,lng2;..."
     * @param colorBorde Color del borde del polígono
     * @param colorRelleno Color del relleno del polígono
     * @return La zona de encuentro creada
     */
    @Transactional
    public ZonaEncuentro crearZonaParaTarea(Long tareaId, String coordenadas, String colorBorde, String colorRelleno) {
        // Buscar la tarea
        Optional<Tarea> optTarea = tareaServicio.obtenerTareaPorId(tareaId);
        if (!optTarea.isPresent()) {
            throw new IllegalArgumentException("La tarea con ID " + tareaId + " no existe");
        }
        
        Tarea tarea = optTarea.get();
        
        // Verificar si la tarea ya tiene una zona de encuentro asignada
        List<ZonaEncuentro> zonasExistentes = obtenerZonasPorTarea(tareaId);
        if (!zonasExistentes.isEmpty()) {
            throw new IllegalStateException("La tarea ya tiene una zona de encuentro asignada. Solo se permite una zona por tarea.");
        }
        
        // Crear la zona de encuentro
        ZonaEncuentro zona = new ZonaEncuentro();
        zona.setNombre("Zona de encuentro para: " + tarea.getNombre());
        zona.setDescripcion("Punto de encuentro para tarea: " + tarea.getDescripcion());
        zona.setCoordenadas(coordenadas);
        zona.setTarea(tarea);
        zona.setColorBorde(colorBorde);
        zona.setColorRelleno(colorRelleno);
        
        // Guardar en la base de datos
        ZonaEncuentro zonaSalvada = zonaEncuentroRepositorio.save(zona);
        
        // También actualizar el punto de encuentro en la tarea
        tarea.setPuntoEncuentro(coordenadas);
        tareaServicio.actualizarTarea(tarea);
        
        return zonaSalvada;
    }
} 