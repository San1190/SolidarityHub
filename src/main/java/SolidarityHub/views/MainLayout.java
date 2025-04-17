package SolidarityHub.views;

import SolidarityHub.models.Usuario;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import SolidarityHub.components.NotificacionesComponent;
import SolidarityHub.services.NotificacionServicio;

public class MainLayout extends AppLayout implements RouterLayout {

    protected Usuario usuario;
    private NotificacionServicio notificacionServicio;
    private Dialog notificacionesDialog;
    private NotificacionesComponent notificacionesComponent;

    public MainLayout(NotificacionServicio notificacionServicio) {
        this.notificacionServicio = notificacionServicio;
        this.usuario = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");
        if (usuario == null) {
            UI.getCurrent().navigate("/");
            return;
        }

        // NAVBAR
        DrawerToggle toggle = new DrawerToggle();
        H1 title = new H1("SolidarityHub");
        title.getStyle().set("margin", "0").setColor("green");
        Avatar avatar = new Avatar(usuario.getNombre() + " " + usuario.getApellidos());
        avatar.setImage("/api/usuarios/" + usuario.getId() + "/foto");

        Button notificacionesBtn = new Button(new Icon(VaadinIcon.BELL));
        notificacionesBtn.addClickListener(e -> mostrarNotificaciones());

        HorizontalLayout navbar = new HorizontalLayout(toggle, title, notificacionesBtn, avatar);

        notificacionesDialog = new Dialog();
        notificacionesDialog.setWidth("400px");
        notificacionesComponent = new NotificacionesComponent(notificacionServicio);
        notificacionesDialog.add(notificacionesComponent);
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
        } else if ("recursos".equals(currentRoute)) {
            recursosItem.getElement().getThemeList().add("primary");
        }

        nav.addItem(mainItem);
        if (usuario.getTipoUsuario().equals("afectado")) {
             nav.addItem(necesidadesItem);
        }
        nav.addItem(tareasItem, recursosItem, configItem, logoutItem);
        nav.addItem(tareasItem, recursosItem, configItem, logoutItem);

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
    }

    private void mostrarNotificaciones() {
        notificacionesComponent.actualizarNotificaciones();
        notificacionesDialog.open();
    }
}
