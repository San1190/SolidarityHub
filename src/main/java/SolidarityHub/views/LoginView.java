package SolidarityHub.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.notification.Notification;

import java.util.Optional;

import org.springframework.web.client.RestTemplate;
import SolidarityHub.models.Voluntario;
import SolidarityHub.utils.handlerRegistrarBtn;
import SolidarityHub.models.Afectado;
import SolidarityHub.models.Usuario;

@Route("/") // Define la ruta accesible en la URL
public class LoginView extends VerticalLayout {

    private VerticalLayout panel;

    private LoginOverlay loginOverlay;

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

        // Panel Formulario de inicio de sesión
        panel = new VerticalLayout();
        panel.setWidth("50%");
        panel.setAlignItems(Alignment.CENTER);
        panel.setJustifyContentMode(JustifyContentMode.CENTER);

        // Agregar componentes al panel derecho
        panel.add(crearLogo(), crearTipoUsuario(), crearEmailField(), crearContraseñaField(),
                crearIniciarSesionBtn(),
                crearContraseñaOlvidadaLink(), crearRegistrarBtn());

        /*
         * loginOverlay = new LoginOverlay();
         * loginOverlay.setI18n(crearLogin());
         * loginOverlay.setOpened(true);
         * loginOverlay.setForgotPasswordButtonVisible(true);
         * 
         * panelDer.add(loginOverlay);
         */

        // Agregar paneles al diseño principal
        add(panel);

    }

    /*
     * private LoginI18n crearLogin() {
     * LoginI18n i18n = LoginI18n.createDefault();
     * 
     * LoginI18n.Header i18nHeader = new LoginI18n.Header();
     * i18nHeader.setTitle("Bienvenido a SolidarityHub");
     * i18n.setHeader(i18nHeader);
     * 
     * LoginI18n.Form i18nForm = i18n.getForm();
     * i18nForm.setUsername("Email");
     * i18nForm.setPassword("Contraseña");
     * i18nForm.setSubmit("Iniciar Sesión");
     * i18nForm.setForgotPassword("¿Aún no te has registrado? Regístrate aquí", e ->
     * {
     * handlerRegistrarBtn.pulsarRegistrarBtn();
     * });
     * i18n.setForm(i18nForm);
     * 
     * LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
     * i18nErrorMessage.setTitle("Error");
     * i18nErrorMessage.setMessage("Credenciales incorrectas");
     * i18n.setErrorMessage(i18nErrorMessage);
     * 
     * i18n.setAdditionalInformation("¿Olvidaste tu contraseña?");
     * 
     * return i18n;
     * 
     * }
     */

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
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/usuarios/login"; // Cambiar por la URL de tu backend

        try {
            // Hacer la solicitud POST con los datos del usuario
            Usuario response = restTemplate.postForObject(url, usuario, Usuario.class);

            if (response != null) {
                VaadinSession.getCurrent().setAttribute("usuario", response);
                UI.getCurrent().navigate("main");
            } else {
                // Si las credenciales son incorrectas, mostrar error
                Notification.show("Credenciales incorrectas");
            }
        } catch (Exception e) {
            Notification.show("Las credenciales son incorrectas ");
        }
    }

    private Component crearRegistrarBtn() {
        // Botón para registrarse si no tiene cuenta
        registrarBtn = new Button("¿No tienes cuenta? Regístrate", event -> {
            handlerRegistrarBtn.pulsarRegistrarBtn();
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

    public EmailField getEmailField() {
        return emailField;
    }

    public PasswordField getContraseñaField() {
        return contraseñaField;
    }

    public RadioButtonGroup<String> getTipoUsuario() {
        return tipoUsuario;
    }
}
