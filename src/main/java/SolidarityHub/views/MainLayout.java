package SolidarityHub.views;

import SolidarityHub.models.Notificacion;
import SolidarityHub.models.Usuario;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import SolidarityHub.components.NotificacionesComponent;
import SolidarityHub.services.NotificacionServicio;
import SolidarityHub.services.NotificacionBroadcaster;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.shared.communication.PushMode;

import java.util.List;

@Push(PushMode.AUTOMATIC)
public class MainLayout extends AppLayout implements RouterLayout {

    protected Usuario usuario;
    private NotificacionServicio notificacionServicio;
    private Dialog notificacionesDialog;
    private NotificacionesComponent notificacionesComponent;
    private Badge notificacionesBadge;
    private Registration broadcasterRegistration;

    public MainLayout(NotificacionServicio notificacionServicio) {
        this.notificacionServicio = notificacionServicio;
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

        // Botón de notificaciones con contador
        Button notificacionesBtn = new Button(new Icon(VaadinIcon.BELL));
        notificacionesBtn.addClickListener(e -> mostrarNotificaciones());
        
        // Badge para mostrar el número de notificaciones no leídas
        notificacionesBadge = new Badge();
        notificacionesBadge.getElement().getThemeList().add("error");
        notificacionesBadge.getStyle()
                .set("position", "absolute")
                .set("transform", "translate(50%, -50%)")
                .set("top", "0")
                .set("right", "0");
        
        // Contenedor para el botón y el badge
        Div notificacionesContainer = new Div(notificacionesBtn, notificacionesBadge);
        notificacionesContainer.getStyle().set("position", "relative");

        HorizontalLayout navbar = new HorizontalLayout();
        Div divisorIzq = new Div();
        Div divisorDer = new Div();

        divisorIzq.setWidthFull();
        divisorDer.setWidthFull();

        navbar.setFlexGrow(1, divisorIzq, divisorDer);

        notificacionesDialog = new Dialog();
        notificacionesDialog.setWidth("400px");
        notificacionesComponent = new NotificacionesComponent(notificacionServicio);
        notificacionesDialog.add(notificacionesComponent);
        navbar.add(toggle, divisorIzq, titulo, divisorDer, notificacionesContainer, avatar);
        navbar.setWidthFull();
        navbar.setPadding(true);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        addToNavbar(navbar);

        // DRAWER
        SideNav nav = new SideNav();
        nav.setWidthFull();

        // Crear ítems de navegación
        SideNavItem mainItem = new SideNavItem("Main", "main", VaadinIcon.MAP_MARKER.create());
        
        SideNavItem necesidadesItem = new SideNavItem("Necesidades", "necesidades", VaadinIcon.HEART.create());
        
        SideNavItem tareasItem = new SideNavItem("Tareas", "tareas", VaadinIcon.TASKS.create());
        
    
        
        SideNavItem recursosItem = new SideNavItem("Inventario", "inventario", VaadinIcon.PACKAGE.create());
            
        SideNavItem configItem = new SideNavItem("Configuración", "configuracion", VaadinIcon.COG.create());
        //comprobar si el usuario es afectado para añadir una pestaña llamada necesidades 
   
        SideNavItem logoutItem = new SideNavItem("Cerrar Sesión", "", VaadinIcon.SIGN_OUT.create());
        logoutItem.getElement().addEventListener("click", event -> {
        VaadinSession.getCurrent().close();
        UI.getCurrent().navigate("/");
        });
        

        // Resaltar el ítem activo según la ruta actual
        String currentRoute = UI.getCurrent().getInternals().getActiveViewLocation().getPath();
        if ("main".equals(currentRoute)) {
            mainItem.getElement().getThemeList().add("primary");
        } else if ("configuracion".equals(currentRoute)) {
            configItem.getElement().getThemeList().add("primary");
        } else if ("necesidades".equals(currentRoute)) {
            necesidadesItem.getElement().getThemeList().add("primary");
        } else if ("tareas".equals(currentRoute)) {
            tareasItem.getElement().getThemeList().add("primary");
        } else if ("recursos".equals(currentRoute)) {
            recursosItem.getElement().getThemeList().add("primary");
        }

        nav.addItem(mainItem);
        if (usuario.getTipoUsuario().equals("afectado")) {
             nav.addItem(necesidadesItem);
        }
        nav.addItem(tareasItem);

        
        nav.addItem(recursosItem, configItem, logoutItem);

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
    }

    private void mostrarNotificaciones() {
        notificacionesComponent.actualizarNotificaciones();
        notificacionesDialog.open();
    }
    
    /**
     * Actualiza el contador de notificaciones no leídas
     */
    public void actualizarContadorNotificaciones() {
        if (usuario != null) {
            List<Notificacion> notificacionesNoLeidas = notificacionServicio.obtenerNotificacionesNoLeidas(usuario);
            int cantidad = notificacionesNoLeidas.size();
            
            if (cantidad > 0) {
                notificacionesBadge.setText(String.valueOf(cantidad));
                notificacionesBadge.setVisible(true);
            } else {
                notificacionesBadge.setVisible(false);
            }
        }
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        
        // Actualizar contador de notificaciones
        actualizarContadorNotificaciones();
        
        // Registrar para recibir notificaciones en tiempo real
        if (usuario != null) {
            // Registrarse para recibir notificaciones usando el broadcaster de Vaadin
            broadcasterRegistration = NotificacionBroadcaster.register(usuario.getId(), notification -> {
                // Este código se ejecuta cuando se recibe una notificación
                ui.access(() -> {
                    recibirNotificacion();
                });
            });
        }
    }
    
    /**
     * Método llamado cuando se recibe una notificación
     */
    public void recibirNotificacion() {
        // Actualizar contador de notificaciones
        actualizarContadorNotificaciones();
        
        // Mostrar una notificación visual
        Notification notificacion = new Notification(
            "Tienes una nueva notificación", 3000, 
            Notification.Position.BOTTOM_END);
        notificacion.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        notificacion.open();
    }
    
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Cancelar la suscripción a notificaciones
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
    }
}
