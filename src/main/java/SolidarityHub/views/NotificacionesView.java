package SolidarityHub.views;

import SolidarityHub.models.Notificacion;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "notificaciones", layout = MainLayout.class)
@PageTitle("Notificaciones | SolidarityHub")
public class NotificacionesView extends VerticalLayout {

    private final Usuario usuarioActual;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
   
    private Dialog detallesTareaDialog;
    private List<Notificacion> notificaciones;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080/api/notificaciones";
    
    public NotificacionesView() {
        this.usuarioActual = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");

        // Configuración del layout principal
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        
        // Añadir título a la vista
        H3 titulo = new H3("Mis Notificaciones");
        titulo.getStyle().set("margin-top", "0");
        add(titulo);
        
        // Inicializar el diálogo de detalles de tarea
        detallesTareaDialog = new Dialog();
        detallesTareaDialog.setWidth("500px");
        detallesTareaDialog.setCloseOnEsc(true);
        detallesTareaDialog.setCloseOnOutsideClick(true);

        // Cargar las notificaciones
        cargarNotificaciones();
    }

    private void cargarNotificaciones() {
        // Eliminar contenido anterior excepto el título
        getChildren().forEach(component -> {
            if (!(component instanceof H3)) {
                remove(component);
            }
        });
        
        // Obtener notificaciones del usuario actual
        notificaciones = notificacionServicio.findByVoluntarioAndEstado(
                usuarioActual, Notificacion.EstadoNotificacion.PENDIENTE);

        // Contenedor para las tarjetas de notificaciones
        VerticalLayout contenedorNotificaciones = new VerticalLayout();
        contenedorNotificaciones.setSpacing(true);
        contenedorNotificaciones.setPadding(false);
        contenedorNotificaciones.setWidthFull();
        
        if (notificaciones.isEmpty()) {
            contenedorNotificaciones.add(crearMensajeVacio());
        } else {
            notificaciones.forEach(notificacion -> {
                contenedorNotificaciones.add(crearTarjetaNotificacion(notificacion));
            });
        }
        
        add(contenedorNotificaciones);
    }

    private Component crearMensajeVacio() {
        Div mensajeVacio = new Div();
        mensajeVacio.setText("No tienes notificaciones pendientes");
        mensajeVacio.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-align", "center")
                .set("padding", "1em");
        return mensajeVacio;
    }

    private Component crearTarjetaNotificacion(Notificacion notificacion) {
        Div tarjeta = new Div();
        tarjeta.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("padding", "1em")
                .set("margin-bottom", "0.5em");

        // Título y fecha
        HorizontalLayout encabezado = new HorizontalLayout();
        encabezado.setWidthFull();
        encabezado.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span titulo = new Span(notificacion.getTitulo());
        titulo.getStyle().set("font-weight", "bold");
        
        // Añadir icono según el tipo de notificación
        Icon icono = null;
        if (notificacion.getTitulo().contains("Nueva tarea")) {
            icono = VaadinIcon.BELL.create();
            icono.setColor("var(--lumo-primary-color)");
            titulo.getElement().insertChild(0, icono.getElement());
        } else if (notificacion.getTitulo().contains("Recordatorio")) {
            icono = VaadinIcon.CLOCK.create();
            icono.setColor("var(--lumo-primary-color)");
            titulo.getElement().insertChild(0, icono.getElement());
        }

        Span fecha = new Span(notificacion.getFechaCreacion().format(formatter));
        fecha.getStyle().set("color", "var(--lumo-secondary-text-color)");

        encabezado.add(titulo, fecha);

        // Mensaje
        Div mensaje = new Div();
        mensaje.setText(notificacion.getMensaje());
        mensaje.getStyle().set("margin", "0.5em 0");

        // Botones de acción
        HorizontalLayout acciones = new HorizontalLayout();
        acciones.setSpacing(true);
        acciones.setWidthFull();
        acciones.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // Verificar si es una notificación relacionada con una tarea
        Tarea tarea = notificacion.getTarea();
        boolean esTareaAsignada = tarea != null && 
                                 notificacion.getTitulo().contains("Nueva tarea disponible");
        boolean esRecordatorio = tarea != null && 
                               notificacion.getTitulo().contains("Recordatorio");

        if (esTareaAsignada) {
            // Botón para ver detalles de la tarea
            Button verDetallesBtn = new Button("Ver detalles", new Icon(VaadinIcon.INFO_CIRCLE));
            verDetallesBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            verDetallesBtn.addClickListener(e -> mostrarDetallesTarea(tarea));
            
            // Botones para aceptar o rechazar la tarea
            Button aceptarBtn = new Button("Aceptar", new Icon(VaadinIcon.CHECK));
            aceptarBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            aceptarBtn.addClickListener(e -> {
                // Lógica para aceptar la tarea
                boolean resultado = notificacionServicio.responderAsignacionTarea(
                        tarea.getId(), usuarioActual.getId(), Notificacion.EstadoNotificacion.ACEPTADA);
                if (resultado) {
                    notificacionServicio.marcarComoLeida(notificacion.getId());
                    cargarNotificaciones();
                    
                    Notification notif = new Notification("Has aceptado la tarea: " + tarea.getNombre());
                    notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notif.setPosition(Notification.Position.BOTTOM_CENTER);
                    notif.setDuration(3000);
                    notif.open();
                    
                    // Navegar a la vista de tareas
                    UI.getCurrent().navigate(TareasView.class);
                } else {
                    Notification notif = new Notification("No se pudo aceptar la tarea");
                    notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notif.setPosition(Notification.Position.BOTTOM_CENTER);
                    notif.setDuration(3000);
                    notif.open();
                }
            });

            Button rechazarBtn = new Button("Rechazar", new Icon(VaadinIcon.CLOSE_SMALL));
            rechazarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            rechazarBtn.addClickListener(e -> {
                // Lógica para rechazar la tarea
                boolean resultado = notificacionServicio.responderAsignacionTarea(
                        tarea.getId(), usuarioActual.getId(), Notificacion.EstadoNotificacion.RECHAZADA);
                if (resultado) {
                    notificacionServicio.eliminarNotificacion(notificacion.getId());
                    cargarNotificaciones();
                    
                    Notification notif = new Notification("Has rechazado la tarea: " + tarea.getNombre());
                    notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notif.setPosition(Notification.Position.BOTTOM_CENTER);
                    notif.setDuration(3000);
                    notif.open();
                } else {
                    Notification notif = new Notification("No se pudo rechazar la tarea");
                    notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notif.setPosition(Notification.Position.BOTTOM_CENTER);
                    notif.setDuration(3000);
                    notif.open();
                }
            });

            acciones.add(verDetallesBtn, aceptarBtn, rechazarBtn);
        } else if (esRecordatorio) {
            // Para recordatorios, mostrar botón para ver detalles
            Button verDetallesBtn = new Button("Ver detalles", new Icon(VaadinIcon.INFO_CIRCLE));
            verDetallesBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            verDetallesBtn.addClickListener(e -> {
                try {
                    // Marcar como leída usando API REST
                    restTemplate.delete(apiUrl + "/" + notificacion.getId());
                    cargarNotificaciones();
                    
                    // Navegar a la vista de tareas
                    UI.getCurrent().navigate(TareasView.class);
                } catch (Exception ex) {
                    Notification notif = new Notification("Error al marcar como leída: " + ex.getMessage());
                    notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notif.setPosition(Notification.Position.BOTTOM_CENTER);
                    notif.setDuration(3000);
                    notif.open();
                }
            });
            
            Button marcarLeidaBtn = new Button("Entendido", new Icon(VaadinIcon.CHECK));
            marcarLeidaBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            marcarLeidaBtn.addClickListener(e -> {
                try {
                    // Marcar como leída usando API REST
                    restTemplate.delete(apiUrl + "/" + notificacion.getId());
                    cargarNotificaciones();
                    
                    Notification notif = new Notification("Notificación marcada como leída");
                    notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notif.setPosition(Notification.Position.BOTTOM_CENTER);
                    notif.setDuration(3000);
                    notif.open();
                } catch (Exception ex) {
                    Notification notif = new Notification("Error al marcar como leída: " + ex.getMessage());
                    notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notif.setPosition(Notification.Position.BOTTOM_CENTER);
                    notif.setDuration(3000);
                    notif.open();
                }
            });
            
            acciones.add(verDetallesBtn, marcarLeidaBtn);
        } else {
            // Para otras notificaciones, solo mostrar botón para marcar como leída
            Button marcarLeidaBtn = new Button("Entendido", new Icon(VaadinIcon.CHECK));
            marcarLeidaBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            marcarLeidaBtn.addClickListener(e -> {
                try {
                    // Marcar como leída usando API REST
                    restTemplate.delete(apiUrl + "/" + notificacion.getId());
                    cargarNotificaciones();
                    
                    Notification notif = new Notification("Notificación marcada como leída");
                    notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notif.setPosition(Notification.Position.BOTTOM_CENTER);
                    notif.setDuration(3000);
                    notif.open();
                } catch (Exception ex) {
                    Notification notif = new Notification("Error al marcar como leída: " + ex.getMessage());
                    notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notif.setPosition(Notification.Position.BOTTOM_CENTER);
                    notif.setDuration(3000);
                    notif.open();
                }
            });
            
            acciones.add(marcarLeidaBtn);
        }

        tarjeta.add(encabezado, mensaje, acciones);
        return tarjeta;
    }
    
    /**
     * Muestra un diálogo con los detalles de la tarea
     * @param tarea La tarea a mostrar
     */
    private void mostrarDetallesTarea(Tarea tarea) {
        detallesTareaDialog.removeAll();
        
        VerticalLayout contenido = new VerticalLayout();
        contenido.setPadding(true);
        contenido.setSpacing(true);
        
        H4 titulo = new H4("Detalles de la tarea: " + tarea.getNombre());
        titulo.getStyle().set("margin-top", "0");
        
        Paragraph descripcion = new Paragraph(tarea.getDescripcion());
        
        // Información adicional de la tarea
        Div infoContainer = new Div();
        infoContainer.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "1em")
                .set("margin", "1em 0");
        
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);
        
        // Añadir detalles de la tarea
        infoLayout.add(crearInfoItem("Tipo", tarea.getTipoNecesidad() != null ? tarea.getTipoNecesidad().name() : "No especificado"));
        infoLayout.add(crearInfoItem("Ubicación", tarea.getLocalizacion()));
        infoLayout.add(crearInfoItem("Fecha de inicio", tarea.getFechaInicio() != null ? 
                tarea.getFechaInicio().format(formatter) : "No especificada"));
        infoLayout.add(crearInfoItem("Fecha de fin", tarea.getFechaFin() != null ? 
                tarea.getFechaFin().format(formatter) : "No especificada"));
        infoLayout.add(crearInfoItem("Estado", tarea.getEstadoTarea() != null ? 
                tarea.getEstadoTarea().name() : "No especificado"));
        infoLayout.add(crearInfoItem("Voluntarios necesarios", String.valueOf(tarea.getNumeroVoluntariosNecesarios())));
        
        infoContainer.add(infoLayout);
        
        // Botones de acción
        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setWidthFull();
        botonesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        Button cerrarBtn = new Button("Cerrar", e -> detallesTareaDialog.close());
        cerrarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        Button verEnTareasBtn = new Button("Ver en Tareas", e -> {
            detallesTareaDialog.close();
            UI.getCurrent().navigate(TareasView.class);
        });
        verEnTareasBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        botonesLayout.add(cerrarBtn, verEnTareasBtn);
        
        contenido.add(titulo, descripcion, infoContainer, botonesLayout);
        detallesTareaDialog.add(contenido);
        detallesTareaDialog.open();
    }
    
    /**
     * Crea un elemento de información para mostrar en el diálogo de detalles
     * @param etiqueta La etiqueta del elemento
     * @param valor El valor del elemento
     * @return El componente creado
     */
    private Component crearInfoItem(String etiqueta, String valor) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Span labelSpan = new Span(etiqueta + ":");
        labelSpan.getStyle()
                .set("font-weight", "bold")
                .set("color", "var(--lumo-secondary-text-color)");
        
        Span valueSpan = new Span(valor);
        
        layout.add(labelSpan, valueSpan);
        return layout;
    }
}