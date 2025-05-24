package SolidarityHub.views;

import SolidarityHub.models.Notificacion;
import SolidarityHub.models.Usuario;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import SolidarityHub.services.NotificacionServicio;

import com.vaadin.flow.shared.Registration;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainLayout extends AppLayout implements RouterLayout {

    protected Usuario usuario;

    private Dialog notificacionesDialog;

    private Span notificacionesBadge;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080/api/notificaciones";
    private ScheduledExecutorService scheduler;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MainLayout() {

        this.usuario = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");
        if (usuario == null) {
            UI.getCurrent().navigate("/");
            return;
        }

        // NAVBAR
        DrawerToggle toggle = new DrawerToggle();
        H1 titulo = new H1("SolidarityHub");
        titulo.getStyle().set("margin", "0").setColor("green");
        Avatar avatar = new Avatar(usuario.getNombre() + " " + usuario.getApellidos());
        avatar.setImage("/api/usuarios/" + usuario.getId() + "/foto");
        avatar.getStyle()
                .set("cursor", "pointer")
                .set("width", "50px")
                .set("height", "50px")
                .set("border-radius", "50%")
                .set("box-shadow", "0 0 5px rgba(0, 0, 0, 0.2)");
        // haz un listener para q si presiona el avatar s va a cnfiguracion
        avatar.getElement().addEventListener("click", event -> {
            UI.getCurrent().navigate("configuracion");
        });

        // Badge para mostrar el número de notificaciones no leídas
        // Badge simulado con Span
        notificacionesBadge = new Span();
        notificacionesBadge.getStyle()
                .set("background-color", "red")
                .set("color", "white")
                .set("border-radius", "999px")
                .set("padding", "2px 6px")
                .set("font-size", "0.75em")
                .set("position", "absolute")
                .set("transform", "translate(50%, -50%)")
                .set("top", "0")
                .set("right", "0")
                .set("display", "none"); // se oculta inicialmente

        HorizontalLayout navbar = new HorizontalLayout();
        Div divisorIzq = new Div();
        Div divisorDer = new Div();

        divisorIzq.setWidthFull();
        divisorDer.setWidthFull();

        navbar.setFlexGrow(1, divisorIzq, divisorDer);
        navbar.setWidthFull();
        navbar.setPadding(true);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbar.add(toggle, titulo, avatar);
        addToNavbar(navbar);

        // DRAWER
        SideNav nav = new SideNav();
        nav.setWidthFull();

        // Crear ítems de navegación
        SideNavItem mainItem = new SideNavItem("Inicio", "main", VaadinIcon.MAP_MARKER.create());

        SideNavItem dashboardItem = new SideNavItem("Dashboard", "dashboard", VaadinIcon.DASHBOARD.create());

        SideNavItem necesidadesItem = new SideNavItem("Necesidades", "necesidades", VaadinIcon.HEART.create());

        SideNavItem tareasItem = new SideNavItem("Tareas", "tareas", VaadinIcon.TASKS.create());

        SideNavItem recursosItem = new SideNavItem("Inventario", "inventario", VaadinIcon.PACKAGE.create());

        SideNavItem configItem = new SideNavItem("Configuración", "configuracion", VaadinIcon.COG.create());
        // comprobar si el usuario es afectado para añadir una pestaña llamada
        // necesidades

        SideNavItem notificacionesItem = new SideNavItem("Notificaciones", "notificaciones", VaadinIcon.BELL.create());

        SideNavItem logoutItem = new SideNavItem("Cerrar Sesión", "", VaadinIcon.SIGN_OUT.create());
        logoutItem.getElement().addEventListener("click", event -> {
            VaadinSession.getCurrent().close();
            UI.getCurrent().navigate("/");
        });

        // Resaltar el ítem activo según la ruta actual
        String currentRoute = UI.getCurrent().getInternals().getActiveViewLocation().getPath();
        if ("main".equals(currentRoute)) {
            mainItem.getElement().getThemeList().add("primary");
        } else if ("dashboard".equals(currentRoute)) {
            dashboardItem.getElement().getThemeList().add("primary");
        } else if ("configuracion".equals(currentRoute)) {
            configItem.getElement().getThemeList().add("primary");
        } else if ("necesidades".equals(currentRoute)) {
            necesidadesItem.getElement().getThemeList().add("primary");
        } else if ("tareas".equals(currentRoute)) {
            tareasItem.getElement().getThemeList().add("primary");
        } else if ("recursos".equals(currentRoute)) {
            recursosItem.getElement().getThemeList().add("primary");
        } else if ("notificaciones".equals(currentRoute)) {
            notificacionesItem.getElement().getThemeList().add("primary");
            // Configurar el evento de clic para el ítem de notificaciones
            notificacionesItem.getElement().addEventListener("click", event -> {
                UI.getCurrent().navigate("notificaciones");
            });
        }

        nav.addItem(mainItem, dashboardItem);
        if (usuario.getTipoUsuario().equals("afectado")) {
            nav.addItem(necesidadesItem);
        }
        nav.addItem(tareasItem);

        nav.addItem(recursosItem, configItem, notificacionesItem, logoutItem);

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Iniciar el programador para verificar notificaciones cada 30 segundos
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            // Ejecutar en el hilo de la UI
            attachEvent.getUI().access(() -> {
                verificarNotificaciones();
            });
        }, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // Detener el programador cuando se desconecta el componente
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        super.onDetach(detachEvent);
    }

    private void verificarNotificaciones() {
        if (usuario == null)
            return;

        try {
            // Configurar el RestTemplate para manejar respuestas HTML
            if (restTemplate.getMessageConverters().stream()
                    .noneMatch(
                            converter -> converter instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter)) {
                org.springframework.http.converter.json.MappingJackson2HttpMessageConverter converter = new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter();
                List<org.springframework.http.MediaType> mediaTypes = new java.util.ArrayList<>();
                mediaTypes.add(org.springframework.http.MediaType.APPLICATION_JSON);
                mediaTypes.add(org.springframework.http.MediaType.TEXT_HTML);
                mediaTypes.add(org.springframework.http.MediaType.TEXT_PLAIN);
                converter.setSupportedMediaTypes(mediaTypes);
                restTemplate.getMessageConverters().add(converter);
            }

            // Obtener notificaciones no leídas del usuario usando la URL correcta
            ResponseEntity<List<Notificacion>> response = restTemplate.exchange(
                    "http://localhost:8080/api/usuarios/" + usuario.getId() + "/actualizar-notificaciones",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Notificacion>>() {
                    });

            List<Notificacion> notificacionesPendientes = response.getBody();

            // Actualizar el badge con el número de notificaciones pendientes
            if (notificacionesPendientes != null && !notificacionesPendientes.isEmpty()) {
                notificacionesBadge.setText(String.valueOf(notificacionesPendientes.size()));
                notificacionesBadge.getStyle().set("display", "block");
            } else {
                notificacionesBadge.getStyle().set("display", "none");
            }
        } catch (Exception e) {
            // Manejar silenciosamente los errores de conexión
            System.err.println("Error al verificar notificaciones: " + e.getMessage());
        }
    }

    private void configurarDialogoNotificaciones() {
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        H3 titulo = new H3("Notificaciones");
        titulo.getStyle().set("margin-top", "0");

        Button verTodasBtn = new Button("Ver todas", e -> {
            notificacionesDialog.close();
            UI.getCurrent().navigate("notificaciones");
        });
        verTodasBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(titulo, verTodasBtn);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        dialogLayout.add(header);

        // Contenedor para las notificaciones
        VerticalLayout notificacionesContainer = new VerticalLayout();
        notificacionesContainer.setPadding(false);
        notificacionesContainer.setSpacing(true);
        notificacionesContainer.setId("notificaciones-container");

        dialogLayout.add(notificacionesContainer);
        notificacionesDialog.add(dialogLayout);
    }

    private void actualizarDialogoNotificaciones() {
        if (usuario == null)
            return;

        // Obtener el contenedor de notificaciones
        VerticalLayout notificacionesContainer = (VerticalLayout) notificacionesDialog.getChildren()
                .filter(component -> component instanceof VerticalLayout)
                .findFirst()
                .orElse(null);

        if (notificacionesContainer == null)
            return;

        // Obtener el contenedor interno
        VerticalLayout innerContainer = (VerticalLayout) notificacionesContainer.getChildren()
                .filter(component -> component instanceof VerticalLayout)
                .filter(component -> "notificaciones-container".equals(component.getId().orElse("")))
                .findFirst()
                .orElse(null);

        if (innerContainer == null)
            return;

        // Limpiar el contenedor
        innerContainer.removeAll();

        try {
            // Obtener las últimas 5 notificaciones del usuario
            ResponseEntity<List<Notificacion>> response = restTemplate.exchange(
                    apiUrl + "/usuario/" + usuario.getId() + "/recientes?limit=5",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Notificacion>>() {
                    });

            List<Notificacion> notificacionesRecientes = response.getBody();

            if (notificacionesRecientes == null || notificacionesRecientes.isEmpty()) {
                Paragraph mensaje = new Paragraph("No tienes notificaciones recientes");
                mensaje.getStyle().set("color", "var(--lumo-secondary-text-color)");
                innerContainer.add(mensaje);
            } else {
                // Añadir cada notificación al contenedor
                for (Notificacion notificacion : notificacionesRecientes) {
                    innerContainer.add(crearTarjetaNotificacionResumida(notificacion));
                }
            }
        } catch (Exception e) {
            Paragraph mensaje = new Paragraph("Error al cargar notificaciones");
            mensaje.getStyle().set("color", "var(--lumo-error-text-color)");
            innerContainer.add(mensaje);
        }
    }

    private Component crearTarjetaNotificacionResumida(Notificacion notificacion) {
        Div tarjeta = new Div();
        tarjeta.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("padding", "0.5em")
                .set("margin-bottom", "0.5em")
                .set("border-left",
                        notificacion.getEstado() == Notificacion.EstadoNotificacion.PENDIENTE
                                ? "3px solid var(--lumo-primary-color)"
                                : "3px solid var(--lumo-contrast-10pct)");

        // Título con icono
        HorizontalLayout encabezado = new HorizontalLayout();
        encabezado.setWidthFull();
        encabezado.setSpacing(true);
        encabezado.setPadding(false);
        encabezado.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon icono = VaadinIcon.BELL.create();
        icono.setColor("var(--lumo-primary-color)");

        Span titulo = new Span(notificacion.getTitulo());
        titulo.getStyle().set("font-weight", "bold");

        Span fecha = new Span(notificacion.getFechaCreacion().format(formatter));
        fecha.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-xs)");

        encabezado.add(icono, titulo, fecha);
        encabezado.setFlexGrow(1, titulo);

        // Mensaje resumido
        Paragraph mensaje = new Paragraph(notificacion.getMensaje());
        mensaje.getStyle()
                .set("margin", "0.25em 0")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis");

        // Botón para ver detalles
        Button verDetallesBtn = new Button("Ver", e -> {
            notificacionesDialog.close();
            UI.getCurrent().navigate("notificaciones");
        });
        verDetallesBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        tarjeta.add(encabezado, mensaje, verDetallesBtn);
        return tarjeta;
    }
}
