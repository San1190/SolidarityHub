package SolidarityHub.views;

import SolidarityHub.services.UsuarioServicio;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
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
        content.setText("Contenido principal de la vista Main.");
        content.setSizeFull();
        setContent(content);
        // haz que ponga la info de usuario en el div content
        // Agregar contenido al cuadro
        Paragraph nombre = new Paragraph("Nombre: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getNombre());
        Paragraph apellidos = new Paragraph(
                "Apellidos: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getApellidos());
        Paragraph email = new Paragraph("Email: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getEmail());
        Paragraph telefono = new Paragraph(
                "Teléfono: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getTelefono());
        Paragraph contraseña = new Paragraph(
                "Contraseña: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getPassword());
        Paragraph direccion = new Paragraph(
                "Dirección: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getDireccion());
        Paragraph dni = new Paragraph("DNI: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getDni());
        
        content.add(nombre, apellidos, email, telefono, contraseña, direccion, dni);

    }
}
