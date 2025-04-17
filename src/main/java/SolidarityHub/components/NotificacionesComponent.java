package SolidarityHub.components;

import SolidarityHub.models.Notificacion;
import SolidarityHub.models.Usuario;
import SolidarityHub.services.NotificacionServicio;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificacionesComponent extends VerticalLayout {

    private final NotificacionServicio notificacionServicio;
    private final Usuario usuarioActual;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public NotificacionesComponent(NotificacionServicio notificacionServicio) {
        this.notificacionServicio = notificacionServicio;
        this.usuarioActual = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");

        setSpacing(true);
        setPadding(true);
        setWidth("400px");

        actualizarNotificaciones();
    }

    public void actualizarNotificaciones() {
        removeAll();

        List<Notificacion> notificaciones = notificacionServicio.obtenerNotificacionesNoLeidas(usuarioActual);

        if (notificaciones.isEmpty()) {
            add(crearMensajeVacio());
        } else {
            notificaciones.forEach(this::crearTarjetaNotificacion);
        }
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

    private void crearTarjetaNotificacion(Notificacion notificacion) {
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

        Button aceptarBtn = new Button("Aceptar", new Icon(VaadinIcon.CHECK));
        aceptarBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        aceptarBtn.addClickListener(e -> {
            // Lógica para aceptar la tarea
            notificacionServicio.marcarComoLeida(notificacion.getId());
            actualizarNotificaciones();
            Notification.show("Has aceptado la tarea", 3000, Notification.Position.BOTTOM_CENTER);
        });

        Button rechazarBtn = new Button("Rechazar", new Icon(VaadinIcon.CLOSE_SMALL));
        rechazarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        rechazarBtn.addClickListener(e -> {
            notificacionServicio.eliminarNotificacion(notificacion.getId());
            actualizarNotificaciones();
            Notification.show("Has rechazado la tarea", 3000, Notification.Position.BOTTOM_CENTER);
        });

        acciones.add(aceptarBtn, rechazarBtn);

        tarjeta.add(encabezado, mensaje, acciones);
        add(tarjeta);
    }
}