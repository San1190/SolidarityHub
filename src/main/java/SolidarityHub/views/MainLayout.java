package SolidarityHub.views;

import SolidarityHub.models.Notificacion;
import SolidarityHub.models.Usuario;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
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

import SolidarityHub.services.NotificacionServicio;

import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.shared.communication.PushMode;

import java.util.List;

@Push(PushMode.AUTOMATIC)
public class MainLayout extends AppLayout implements RouterLayout {

    protected Usuario usuario;
 
    private Dialog notificacionesDialog;
    
    private  Span notificacionesBadge;
    private Registration broadcasterRegistration;

    public MainLayout(NotificacionServicio notificacionServicio) {
       
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

        notificacionesDialog = new Dialog();
        notificacionesDialog.setWidth("400px");
       
    
        
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

 
  
}
