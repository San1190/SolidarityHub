package SolidarityHub.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.UI;
import SolidarityHub.services.UsuarioServicio;  // Importa el servicio de usuario
import javax.servlet.http.HttpSession;  // Importa la clase HttpSession

@Route("main") // Aquí definimos la ruta de la vista principal
public class MainView extends VerticalLayout {

    private Div saludoDiv;
    private UsuarioServicio usuarioServicio;  // El servicio que proporciona datos del usuario

    public MainView(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;

        // Intentamos obtener el nombre del usuario desde la sesión
        String nombreUsuario = obtenerNombreUsuario();

        // Si no hay nombre (es decir, si la sesión es nula o el usuario no está autenticado), redirigir a login
        if (nombreUsuario == null || nombreUsuario.isEmpty()) {
            UI.getCurrent().navigate("login");
            return;  // No renderizamos nada si no hay un usuario autenticado
        }

        // Crear un saludo para el usuario
        saludoDiv = new Div();
        saludoDiv.setText("Bienvenido, " + nombreUsuario);

        // Agregar saludo a la vista
        add(saludoDiv);
    }

    private String obtenerNombreUsuario() {
        // Aquí puedes recuperar el nombre de usuario de la sesión HTTP
        // Usamos el servicio UsuarioServicio o directamente de la sesión si lo has guardado previamente
        HttpSession session = (HttpSession) UI.getCurrent().getSession().getSession();
        
        // Aquí podrías acceder al nombre del usuario desde la sesión.
        // Asegúrate de haber guardado esta información durante el login, en el backend o como un atributo en la sesión.
        String nombreUsuario = (String) session.getAttribute("nombreUsuario");

        // Si el nombre de usuario no está en la sesión, puede ser que haya algo mal con la autenticación
        return nombreUsuario;
    }
}
