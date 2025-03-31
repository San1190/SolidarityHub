package SolidarityHub.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.web.client.RestTemplate;
import SolidarityHub.models.Voluntario;
import SolidarityHub.models.Afectado;
import SolidarityHub.models.Usuario;

@Route("/") // Define la ruta accesible en la URL
public class LoginView extends VerticalLayout {

    private VerticalLayout panelIzq;
    private VerticalLayout panelDer;

    private Image logo;

    private RadioButtonGroup<String> tipoUsuario;

    private EmailField emailField;
    private PasswordField contraseñaField;

    private Anchor contraseñaOlvidadaLink;

    private Button iniciarSesionBtn;
    private Button registrarBtn;

    public LoginView() {
        // Configuración del diseño principal
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        // Panel izquierdo: Logo y nombre
        panelIzq = new VerticalLayout();
        panelIzq.setWidth("50%");
        panelIzq.setAlignItems(Alignment.CENTER);
        panelIzq.setJustifyContentMode(JustifyContentMode.CENTER);

        // Agregar logo al panel izquierdo
        panelIzq.add(crearLogo());

        // Panel derecho: Formulario de inicio de sesión
        panelDer = new VerticalLayout();
        panelDer.setWidth("50%");
        panelDer.setAlignItems(Alignment.CENTER);
        panelDer.setJustifyContentMode(JustifyContentMode.CENTER);

        // Agregar componentes al panel derecho
        panelDer.add(crearTipoUsuario(), crearEmailField(), crearContraseñaField(), crearIniciarSesionBtn(),
                crearContraseñaOlvidadaLink(), crearRegistrarBtn());

        // Agregar paneles al diseño principal
        add(panelIzq, panelDer);
    }

    private Component crearLogo() {
        // Cargar el logo
        logo = new Image(
                "https://cliente.tuneupprocess.com/ApiWeb/UploadFiles/be802ceb-49c7-493f-945a-078ed3b6bb4d.jpg/LogoSH.jpg",
                "Solidarity Hub Logo");
        logo.setWidth("150px");
        return logo;
    }

    private Component crearTipoUsuario() {
        // Grupo de botones para seleccionar tipo de usuario
        tipoUsuario = new RadioButtonGroup<>();
        tipoUsuario.setItems("Voluntario", "Afectado");
        tipoUsuario.setValue("Voluntario"); // Valor predeterminado
        return tipoUsuario;
    }

    private Component crearEmailField() {
        emailField = new EmailField("Email");
        emailField.setPlaceholder("ejemplo@correo.com");
        return emailField;
    }

    private Component crearContraseñaField() {
        contraseñaField = new PasswordField("Contraseña");
        return contraseñaField;
    }

    private Component crearIniciarSesionBtn() {
        // Botón: Iniciar Sesión
        iniciarSesionBtn = new Button("Iniciar Sesión", event -> {
            // Crear el objeto de usuario dependiendo del tipo seleccionado
            String tipoUsuarioSeleccionado = tipoUsuario.getValue();
            Usuario usuario = null;

            // Comprobar si los campos de email y contraseña no están vacíos
            if (emailField.getValue().isEmpty() || contraseñaField.getValue().isEmpty()) {
                Notification.show("Por favor ingrese todos los campos.");
                return;
            }

            if ("Voluntario".equals(tipoUsuarioSeleccionado)) {
                // Crear un Voluntario
                usuario = new Voluntario();
                ((Voluntario) usuario).setEmail(emailField.getValue());
                ((Voluntario) usuario).setPassword(contraseñaField.getValue());
            } else if ("Afectado".equals(tipoUsuarioSeleccionado)) {
                // Crear un Afectado
                usuario = new Afectado();
                ((Afectado) usuario).setEmail(emailField.getValue());
                ((Afectado) usuario).setPassword(contraseñaField.getValue());
            }

            // Ahora autenticamos el usuario (llamada al backend)
            autenticarUsuario(usuario);
        });
        iniciarSesionBtn.getStyle().set("background-color", "black").set("color", "white");
        return iniciarSesionBtn;
    }

    private void autenticarUsuario(Usuario usuario) {
        // Crear RestTemplate para hacer la solicitud HTTP POST al backend
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/usuarios/login";  // Cambiar por la URL de tu backend

        try {
            // Hacer la solicitud POST con los datos del usuario
            String response = restTemplate.postForObject(url, usuario, String.class);

            if (response.equals("Usuario autenticado correctamente")) {
                VaadinSession.getCurrent().setAttribute("usuario", usuario);
                UI.getCurrent().navigate("main");
            } else {
                // Si las credenciales son incorrectas, mostrar error
                Notification.show("Credenciales incorrectas");
            }
        } catch (Exception e) {
            Notification.show("Error al autenticar el usuario: " + e.getMessage());
        }
    }

    private Component crearRegistrarBtn() {
        // Botón para registrarse si no tiene cuenta
        registrarBtn = new Button("¿No tienes cuenta? Regístrate", event -> {
            // Aquí iría la lógica para abrir la vista de registro
        });
        registrarBtn.getStyle().set("background-color", "white").set("color", "black");
        return registrarBtn;
    }

    private Component crearContraseñaOlvidadaLink() {
        // Link de contraseña olvidada
        contraseñaOlvidadaLink = new Anchor("#", "¿Ha olvidado su contraseña?");
        contraseñaOlvidadaLink.getStyle().set("font-size", "12px").set("color", "#000");
        return contraseñaOlvidadaLink;
    }
}
