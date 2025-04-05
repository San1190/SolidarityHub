package SolidarityHub.views;

import SolidarityHub.services.UsuarioServicio;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin.Minus.Horizontal;

@Route("main")
@PageTitle("Main | SolidarityHub")
public class MainView extends MainLayout {

        public MainView(UsuarioServicio usuarioServicio) {
                // Con el super() ya se configura el navbar y drawer en MainLayout
                // Aquí agregamos el contenido específico de MainView.
                Div content = new Div();
                content.setSizeFull();
                setContent(content);

                // haz que ponga la info de usuario en el div content
                H3 title = new H3("Bienvenido/a a SolidarityHub");
                title.getStyle().set("font-size", "48px").set("font-weight", "bold").set("text-align", "center")
                                .set("color", "green");

                // Agregar contenido al cuadro
                H3 nombre = new H3("Nombre: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getNombre());
                nombre.getStyle().set("font-size", "18px").set("font-weight", "bold").set("text-align", "center");

                H3 apellidos = new H3(
                                "Apellidos: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getApellidos());
                apellidos.getStyle().set("font-size", "18px").set("font-weight", "bold").set("text-align", "center");

                H3 email = new H3("Email: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getEmail());
                email.getStyle().set("font-size", "18px").set("font-weight", "bold").set("text-align", "center");

                H3 telefono = new H3("Teléfono: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getTelefono());
                telefono.getStyle().set("font-size", "18px").set("font-weight", "bold").set("text-align", "center");

                H3 contraseña = new H3(
                                "Contraseña: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getPassword());
                contraseña.getStyle().set("font-size", "18px").set("font-weight", "bold").set("text-align", "center");

                H3 direccion = new H3(
                                "Dirección: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getDireccion());
                direccion.getStyle().set("font-size", "18px").set("font-weight", "bold").set("text-align", "center");

                H3 dni = new H3("DNI: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getDni());
                dni.getStyle().set("font-size", "18px").set("font-weight", "bold").set("text-align", "center");

                content.add(title, nombre, apellidos, email, telefono, contraseña, direccion, dni);

        }
}
