package SolidarityHub.views;

import SolidarityHub.models.Usuario;
import SolidarityHub.services.UsuarioServicio;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("perfil")
@PageTitle("Perfil | SolidarityHub")
public class PerfilView extends AppLayout {
    private Usuario usuario;

    public PerfilView(UsuarioServicio usuarioServicio) {
        this.usuario = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");

        createNavBar();

        Avatar avatar = new Avatar(usuario.getNombre() + usuario.getApellidos());
        avatar.setImage("/api/usuarios/" + usuario.getId() + "/foto");

        DrawerToggle toggle = new DrawerToggle();
        SideNav nav = createNavBar();

        H1 title = new H1("SolidarityHub");
        title.getStyle().set("margin", "0").setColor("green");

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        Button logoutButton = new Button("Cerrar Sesión", event -> {
            VaadinSession.getCurrent().close();
            UI.getCurrent().navigate("/");
        });
        logoutButton.getStyle()
                .set("margin-top", "20px")
                .set("margin-bottom", "20px")
                .set("margin-left", "20px")
                .set("margin-right", "20px")
                .set("background-color", "#f44336")
                .set("color", "white");

        HorizontalLayout navbar = new HorizontalLayout(toggle, title, avatar);
        navbar.setWidthFull(); // Ocupa todo el ancho
        navbar.setPadding(true);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        addToDrawer(scroller,logoutButton);
        addToNavbar(navbar);
    }

    private SideNav createNavBar(){
        SideNav nav = new SideNav();
        nav.setWidthFull();
        return nav;
    }
    private void createDrawer(){

    }
}
