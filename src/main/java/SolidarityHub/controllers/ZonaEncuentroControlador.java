package SolidarityHub.controllers;

import SolidarityHub.models.ZonaEncuentro;
import SolidarityHub.models.Tarea;
import SolidarityHub.services.ZonaEncuentroServicio;
import SolidarityHub.services.TareaServicio;
import SolidarityHub.repository.ZonaEncuentroRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Controlador REST para gestionar las zonas de encuentro.
 */
@RestController
@RequestMapping("/api/zonas-encuentro")
public class ZonaEncuentroControlador {

    private final ZonaEncuentroServicio zonaEncuentroServicio;
    private final TareaServicio tareaServicio;

    @Autowired
    public ZonaEncuentroControlador(ZonaEncuentroServicio zonaEncuentroServicio, TareaServicio tareaServicio) {
        this.zonaEncuentroServicio = zonaEncuentroServicio;
        this.tareaServicio = tareaServicio;
    }

    /**
     * Obtiene todas las zonas de encuentro
     * @return Lista de zonas de encuentro
     */
    @GetMapping
    public ResponseEntity<List<ZonaEncuentro>> obtenerTodasLasZonas() {
        List<ZonaEncuentro> zonas = zonaEncuentroServicio.listarZonasEncuentro();
        return new ResponseEntity<>(zonas, HttpStatus.OK);
    }

    /**
     * Obtiene una zona de encuentro por su ID
     * @param id ID de la zona
     * @return La zona si existe, o 404 si no
     */
    @GetMapping("/{id}")
    public ResponseEntity<ZonaEncuentro> obtenerZonaPorId(@PathVariable Long id) {
        Optional<ZonaEncuentro> zona = zonaEncuentroServicio.obtenerZonaEncuentroPorId(id);
        return zona.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                  .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Crea una nueva zona de encuentro
     * @param zonaEncuentro Datos de la nueva zona
     * @return La zona creada con su ID
     */
    @PostMapping
    public ResponseEntity<ZonaEncuentro> crearZona(@RequestBody ZonaEncuentro zonaEncuentro) {
        ZonaEncuentro nuevaZona = zonaEncuentroServicio.guardarZonaEncuentro(zonaEncuentro);
        return new ResponseEntity<>(nuevaZona, HttpStatus.CREATED);
    }

    /**
     * Crea una zona para una tarea específica
     * @param tareaId ID de la tarea
     * @param datosZona Datos para crear la zona
     * @return La zona creada o un mensaje de error si ya existe zona para esta tarea
     */
    @PostMapping("/tarea/{tareaId}")
    public ResponseEntity<?> crearZonaParaTarea(
            @PathVariable Long tareaId,
            @RequestBody Map<String, String> datosZona) {
        
        String coordenadas = datosZona.get("coordenadas");
        String colorBorde = datosZona.getOrDefault("colorBorde", "#3388ff");
        String colorRelleno = datosZona.getOrDefault("colorRelleno", "#3388ff");
        
        try {
            ZonaEncuentro zona = zonaEncuentroServicio.crearZonaParaTarea(
                tareaId, coordenadas, colorBorde, colorRelleno);
            return new ResponseEntity<>(zona, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Tarea no encontrada
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            // Tarea ya tiene zona asignada
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        } catch (Exception e) {
            // Otros errores
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear la zona: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Actualiza una zona existente
     * @param id ID de la zona a actualizar
     * @param zonaEncuentro Datos actualizados
     * @return La zona actualizada o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<ZonaEncuentro> actualizarZona(
            @PathVariable Long id,
            @RequestBody ZonaEncuentro zonaEncuentro) {
        
        Optional<ZonaEncuentro> zonaExistente = zonaEncuentroServicio.obtenerZonaEncuentroPorId(id);
        if (!zonaExistente.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        zonaEncuentro.setId(id);
        try {
            ZonaEncuentro zonaActualizada = zonaEncuentroServicio.actualizarZonaEncuentro(zonaEncuentro);
            return new ResponseEntity<>(zonaActualizada, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Elimina una zona por su ID
     * @param id ID de la zona a eliminar
     * @return 204 si se eliminó correctamente, 404 si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarZona(@PathVariable Long id) {
        Optional<ZonaEncuentro> zona = zonaEncuentroServicio.obtenerZonaEncuentroPorId(id);
        if (!zona.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        zonaEncuentroServicio.eliminarZonaEncuentro(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Obtiene todas las zonas asociadas a una tarea
     * @param tareaId ID de la tarea
     * @return Lista de zonas de encuentro
     */
    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<List<ZonaEncuentro>> obtenerZonasPorTarea(@PathVariable Long tareaId) {
        List<ZonaEncuentro> zonas = zonaEncuentroServicio.obtenerZonasPorTarea(tareaId);
        return new ResponseEntity<>(zonas, HttpStatus.OK);
    }

    /**
     * Ruta de diagnóstico que verifica y repara las zonas con problemas
     * @return Resumen del diagnóstico
     */
    @GetMapping("/diagnostico")
    public ResponseEntity<Map<String, Object>> realizarDiagnostico() {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> detalles = new ArrayList<>();
        int totalZonas = 0;
        int zonasConProblemas = 0;
        int zonasReparadas = 0;
        
        try {
            // Obtener todas las zonas
            List<ZonaEncuentro> zonas = zonaEncuentroServicio.listarZonasEncuentro();
            totalZonas = zonas.size();
            
            // Verificar cada zona
            for (ZonaEncuentro zona : zonas) {
                Map<String, Object> detalle = new HashMap<>();
                detalle.put("id", zona.getId());
                detalle.put("nombre", zona.getNombre());
                
                boolean tieneProblemas = false;
                
                // Verificar tarea
                if (zona.getTarea() != null) {
                    try {
                        // Intentar acceder a alguna propiedad para detectar si está inicializada
                        String nombreTarea = zona.getTarea().getNombre();
                        detalle.put("tarea", nombreTarea);
                    } catch (Exception e) {
                        // Problema de inicialización de tarea
                        tieneProblemas = true;
                        zonasConProblemas++;
                        detalle.put("error", e.getMessage());
                        
                        // Intentar reparar obteniendo el ID
                        try {
                            Long tareaId = zona.getTarea().getId();
                            detalle.put("tareaId", tareaId);
                            
                            // Intentar recargar la tarea
                            Optional<Tarea> optTarea = tareaServicio.obtenerTareaPorId(tareaId);
                            if (optTarea.isPresent()) {
                                zona.setTarea(optTarea.get());
                                zonasReparadas++;
                                detalle.put("reparado", true);
                                detalle.put("tareaNueva", optTarea.get().getNombre());
                                
                                // Guardar cambios
                                zonaEncuentroServicio.actualizarZonaEncuentro(zona);
                            } else {
                                detalle.put("reparado", false);
                                detalle.put("motivo", "No se encontró la tarea con ID " + tareaId);
                            }
                        } catch (Exception e2) {
                            detalle.put("reparado", false);
                            detalle.put("errorReparacion", e2.getMessage());
                        }
                    }
                } else {
                    detalle.put("tarea", null);
                }
                
                // Verificar coordenadas
                try {
                    List<double[]> coordenadas = zona.getCoordenadaComoLista();
                    detalle.put("puntos", coordenadas.size());
                    if (coordenadas.size() < 3) {
                        tieneProblemas = true;
                        zonasConProblemas++;
                        detalle.put("errorCoordenadas", "Insuficientes puntos: " + coordenadas.size());
                    }
                } catch (Exception e) {
                    tieneProblemas = true;
                    zonasConProblemas++;
                    detalle.put("errorCoordenadas", e.getMessage());
                }
                
                if (tieneProblemas) {
                    detalles.add(detalle);
                }
            }
            
            // Resumen
            resultado.put("totalZonas", totalZonas);
            resultado.put("zonasConProblemas", zonasConProblemas);
            resultado.put("zonasReparadas", zonasReparadas);
            resultado.put("detalles", detalles);
            
            return new ResponseEntity<>(resultado, HttpStatus.OK);
            
        } catch (Exception e) {
            resultado.put("error", e.getMessage());
            return new ResponseEntity<>(resultado, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Intenta reparar todas las zonas con problemas de carga de tareas
     * @return Resumen de la reparación
     */
    @PostMapping("/reparar")
    public ResponseEntity<Map<String, Object>> repararZonas() {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> detalles = new ArrayList<>();
        int zonasReparadas = 0;
        
        try {
            // Obtener todas las zonas
            List<ZonaEncuentro> zonas = zonaEncuentroServicio.listarZonasEncuentro();
            
            for (ZonaEncuentro zona : zonas) {
                Map<String, Object> detalle = new HashMap<>();
                detalle.put("id", zona.getId());
                detalle.put("nombre", zona.getNombre());
                
                boolean reparado = false;
                
                // Verificar si hay problemas con la tarea
                if (zona.getTarea() != null) {
                    try {
                        // Probar si se puede acceder a la tarea
                        zona.getTarea().getNombre();
                    } catch (Exception e) {
                        try {
                            // Intentar recuperar el ID de la tarea
                            Long tareaId = zona.getTarea().getId();
                            
                            // Buscar la tarea por ID
                            Optional<Tarea> optTarea = tareaServicio.obtenerTareaPorId(tareaId);
                            if (optTarea.isPresent()) {
                                // Actualizar la referencia
                                zona.setTarea(optTarea.get());
                                zonaEncuentroServicio.actualizarZonaEncuentro(zona);
                                zonasReparadas++;
                                reparado = true;
                                detalle.put("tareaNueva", optTarea.get().getNombre());
                            }
                        } catch (Exception e2) {
                            detalle.put("error", "No se pudo reparar: " + e2.getMessage());
                        }
                    }
                }
                
                detalle.put("reparado", reparado);
                if (reparado) {
                    detalles.add(detalle);
                }
            }
            
            resultado.put("zonasReparadas", zonasReparadas);
            resultado.put("detalles", detalles);
            
            return new ResponseEntity<>(resultado, HttpStatus.OK);
            
        } catch (Exception e) {
            resultado.put("error", e.getMessage());
            return new ResponseEntity<>(resultado, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 