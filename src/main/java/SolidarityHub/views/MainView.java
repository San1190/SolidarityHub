package SolidarityHub.views;

import SolidarityHub.services.UsuarioServicio;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route(value= "main" , layout = MainLayout.class)
@PageTitle("Main | SolidarityHub")
public class MainView extends VerticalLayout {

        private Image logo;

        public MainView(UsuarioServicio usuarioServicio) {
                setSizeFull();
                setSpacing(false);
                setPadding(true);
                setAlignItems(Alignment.CENTER);

                Div card = new Div();
                card.addClassName(LumoUtility.Background.BASE);
                card.addClassName(LumoUtility.BoxShadow.SMALL);
                card.addClassName(LumoUtility.BorderRadius.LARGE);
                card.addClassName(LumoUtility.Padding.LARGE);
                card.setMaxWidth("600px");
                card.setWidth("100%");

                H3 title = new H3("Bienvenido/a a SolidarityHub");
                title.addClassName(LumoUtility.FontSize.XXXLARGE);
                title.addClassName(LumoUtility.TextColor.SUCCESS);
                title.addClassName(LumoUtility.TextAlignment.CENTER);
                title.addClassName(LumoUtility.Margin.Bottom.LARGE);

                var usuario = usuarioServicio.obtenerUsuarioPorId(1L); // Obtener usuario actual

                H3 nombre = new H3("Nombre: " + usuario.getNombre() + " " + usuario.getApellidos());
                nombre.addClassName(LumoUtility.FontSize.MEDIUM);
                nombre.addClassName(LumoUtility.TextAlignment.CENTER);
                nombre.addClassName(LumoUtility.Margin.Bottom.SMALL);

                H3 email = new H3("Email: " + usuario.getEmail());
                email.addClassName(LumoUtility.FontSize.MEDIUM);
                email.addClassName(LumoUtility.TextAlignment.CENTER);
                email.addClassName(LumoUtility.Margin.Bottom.SMALL);

                H3 telefono = new H3("Teléfono: " + usuario.getTelefono());
                telefono.addClassName(LumoUtility.FontSize.MEDIUM);
                telefono.addClassName(LumoUtility.TextAlignment.CENTER);
                telefono.addClassName(LumoUtility.Margin.Bottom.SMALL);

                H3 rol = new H3("Rol: " + usuario.getTipoUsuario());
                rol.addClassName(LumoUtility.FontSize.MEDIUM);
                rol.addClassName(LumoUtility.TextAlignment.CENTER);
                rol.addClassName(LumoUtility.Margin.Bottom.LARGE);

                card.add(title, nombre, email, telefono, rol, crearLogo());
                add(card);
        }

        private Image crearLogo() {
                // Cargar el logo
                logo = new Image(
                                "https://cliente.tuneupprocess.com/ApiWeb/UploadFiles/7dcef7b2-6389-45f4-9961-8741a558c286.png/LogoSH-transparent.png",
                                "Solidarity Hub Logo");
                logo.setWidth("220px");
                logo.setHeight("auto");
                logo.getStyle().set("margin", "0 auto").set("display", "block").set("padding", "1rem")
                                .set("align-items", "center").set("justify-content", "center")
                                .set("margin-top", "2rem");
                return logo;
        }

}
