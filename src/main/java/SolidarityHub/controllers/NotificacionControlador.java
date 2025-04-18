package SolidarityHub.controllers;

import SolidarityHub.models.Notificacion;
import SolidarityHub.models.Usuario;
import SolidarityHub.services.NotificacionServicio;
import SolidarityHub.services.UsuarioServicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionControlador {

    @Autowired
    private NotificacionServicio notificacionServicio;
    
    @Autowired
    private UsuarioServicio usuarioServicio;

    /**
     * Obtener todas las notificaciones no leídas de un usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Notificacion>> obtenerNotificacionesUsuario(@PathVariable Long usuarioId) {
        Usuario usuario = usuarioServicio.obtenerUsuarioPorId(usuarioId);
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<Notificacion> notificaciones = notificacionServicio.obtenerNotificacionesNoLeidas(usuario);
        return new ResponseEntity<>(notificaciones, HttpStatus.OK);
    }

    /**
     * Marcar una notificación como leída
     */
    @PutMapping("/{notificacionId}/leer")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long notificacionId) {
        notificacionServicio.marcarComoLeida(notificacionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Endpoint para que un voluntario confirme o rechace una tarea asignada
     */
    @PostMapping("/responder-tarea")
    public ResponseEntity<Map<String, Object>> responderAsignacionTarea(
            @RequestBody Map<String, Object> request) {
        
        Long tareaId = Long.valueOf(request.get("tareaId").toString());
        Long voluntarioId = Long.valueOf(request.get("voluntarioId").toString());
        boolean aceptada = Boolean.valueOf(request.get("aceptada").toString());
        
        boolean resultado = notificacionServicio.responderAsignacionTarea(tareaId, voluntarioId, aceptada);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", resultado);
        response.put("message", resultado ? 
                (aceptada ? "Tarea aceptada correctamente" : "Tarea rechazada correctamente") : 
                "No se pudo procesar la respuesta");
        
        return new ResponseEntity<>(response, resultado ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    /**
     * Eliminar una notificación
     */
    @DeleteMapping("/{notificacionId}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long notificacionId) {
        notificacionServicio.eliminarNotificacion(notificacionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}