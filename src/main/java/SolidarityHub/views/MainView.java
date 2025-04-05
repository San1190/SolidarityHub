package SolidarityHub.views;

import SolidarityHub.services.UsuarioServicio;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

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
        content.add("Nombre: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getNombre());
        content.add("Apellidos: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getApellidos());
        content.add("Email: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getEmail());
        content.add("Teléfono: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getTelefono());
        content.add("Contraseña: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getPassword());
        content.add("Dirección: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getDireccion());
        content.add("DNI: " + usuarioServicio.obtenerUsuarioPorId(usuario.getId()).getDni());
    }
}
