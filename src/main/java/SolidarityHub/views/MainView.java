package SolidarityHub.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.button.Button;
import org.springframework.beans.factory.annotation.Autowired;
import SolidarityHub.services.UsuarioServicio;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import SolidarityHub.models.Afectado;

@Route("main")
public class MainView extends VerticalLayout {
    private final UsuarioServicio usuarioServicio;

    @Autowired
    public MainView(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
        
        Usuario usuarioActual = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");
        
        if (usuarioActual != null) {
            // Main container
            Div mainContainer = new Div();
            mainContainer.getStyle()
                .set("padding", "20px")
                .set("max-width", "800px")
                .set("margin", "0 auto");

            // Welcome section
            H1 welcome = new H1("Bienvenido/a a SolidarityHub");
            welcome.getStyle().set("color", "#2196F3");

            // User info card
            Div userCard = new Div();
            userCard.getStyle()
                .set("background-color", "#f5f5f5")
                .set("padding", "20px")
                .set("border-radius", "8px")
                .set("margin", "20px 0");

            H2 userInfo = new H2("Información del Usuario");
            
            // Agregar información común al usuario
            userCard.add(userInfo);
            
            // Agregar información específica según el tipo de usuario
            if (usuarioActual instanceof Voluntario) {
                Voluntario voluntario = (Voluntario) usuarioActual;
                userCard.add(
                    new Paragraph("Tipo de Usuario: Voluntario"),
                    new Paragraph("Email: " + voluntario.getEmail()),
                    new Paragraph("Nombre: " + voluntario.getNombre()),
                    new Paragraph("Teléfono: " + voluntario.getTelefono()),
                    // Suponiendo que el método getCapacidades() devuelve un String o lista
                    new Paragraph("Habilidades: " + voluntario.getHabilidades())
                );
            } else if (usuarioActual instanceof Afectado) {
                Afectado afectado = (Afectado) usuarioActual;
                userCard.add(
                    new Paragraph("Tipo de Usuario: Afectado"),
                    new Paragraph("Email: " + afectado.getEmail()),
                    new Paragraph("Nombre: " + afectado.getNombre()),
                    new Paragraph("Teléfono: " + afectado.getTelefono()),
                    // Suponiendo que el método getNecesidades() devuelve un String o lista
                    new Paragraph("Necesidades: " + afectado.getNecesidades())
                );
            }

            // Botón de cerrar sesión
            Button logoutBtn = new Button("Cerrar Sesión", event -> {
                VaadinSession.getCurrent().close();
                UI.getCurrent().navigate("/");
            });
            logoutBtn.getStyle()
                .set("margin-top", "20px")
                .set("background-color", "#f44336")
                .set("color", "white");

            // Agregar todos los componentes al contenedor principal
            mainContainer.add(welcome, userCard, logoutBtn);
            add(mainContainer);

            // Configuraciones de layout
            setSizeFull();
            setAlignItems(Alignment.CENTER);
            setJustifyContentMode(JustifyContentMode.START);
        } else {
            UI.getCurrent().navigate("login");
        }
    }
}
