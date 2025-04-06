package SolidarityHub.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.theme.lumo.LumoUtility;

import org.springframework.web.client.RestTemplate;
import SolidarityHub.models.Voluntario;
import SolidarityHub.utils.handlerRegistrarBtn;
import SolidarityHub.models.Afectado;
import SolidarityHub.models.Usuario;

@Route("/")
@PageTitle("Login | SolidarityHub")
public class LoginView extends VerticalLayout {

    private Image logo;
    private RadioButtonGroup<String> tipoUsuario;
    private EmailField emailField;
    private PasswordField contraseñaField;
    private Anchor contraseñaOlvidadaLink;
    private Button iniciarSesionBtn;
    private Button registrarBtn;

    public LoginView() {
        // Configuración del diseño principal (similar a RegistroView)
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        // Estilos consistentes con RegistroView
        getElement().getStyle().set("background", "white");
        getElement().getStyle().set("height", "100%");
        getElement().getStyle().set("width", "100vw");
        getElement().getStyle().set("margin", "auto");
        getElement().getStyle().set("padding", "0");
        getElement().getStyle().set("box-shadow", "none");

        getElement().executeJs(
            "document.documentElement.style.background = 'white';" +
            "document.body.style.background = 'white';" +
            "this.parentNode.style.background = 'white';"
        );

        // Crear card contenedor similar a RegistroView
        Div formCard = new Div();
        formCard.addClassName("form-card");
        formCard.getStyle()
            .set("background-color", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 8px 24px rgba(0,0,0,0.1)")
            .set("padding", "2em")
            .set("max-width", "500px")
            .set("width", "90%")
            .set("margin", "2em auto");
        
        // Añadir componentes al card
        formCard.add(
            crearLogo(),
            crearTitulo(),
            crearTipoUsuario(),
            crearEmailField(),
            crearContraseñaField(),
            crearIniciarSesionBtn(),
            crearContraseñaOlvidadaLink(),
            crearRegistrarBtn()
        );
        
        // Añadir card al layout principal
        add(formCard);
    }

    private Component crearTitulo() {
        H1 title = new H1("Iniciar Sesión");
        title.addClassNames(
                LumoUtility.Margin.NONE,
                LumoUtility.FontSize.XXXLARGE);
        title.getStyle()
            .set("color", "#2c3e50")
            .set("text-align", "center")
            .set("margin-bottom", "1em")
            .set("margin-top", "1em")
            .set("font-weight", "600");
        return title;
    }

    private Component crearLogo() {
        // Cargar el logo
        logo = new Image(
                "https://cliente.tuneupprocess.com/ApiWeb/UploadFiles/be802ceb-49c7-493f-945a-078ed3b6bb4d.jpg/LogoSH.jpg",
                "Solidarity Hub Logo");
        logo.setWidth("200px");
        
        // Center the logo
        HorizontalLayout logoLayout = new HorizontalLayout(logo);
        logoLayout.setWidthFull();
        logoLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        
        return logoLayout;
    }

    private Component crearTipoUsuario() {
        // Grupo de botones para seleccionar tipo de usuario
        tipoUsuario = new RadioButtonGroup<>();
        tipoUsuario.setLabel("Tipo de cuenta");
        tipoUsuario.setItems("Afectado", "Voluntario");
        tipoUsuario.setValue("Afectado");
        
        // Style the radio group similar to RegistroView
        tipoUsuario.getStyle()
            .set("margin-bottom", "2em")
            .set("border-radius", "8px")
            .set("padding", "1.5em 4em")
            .set("background-color", "rgba(52, 152, 219, 0.05)");
            
        return tipoUsuario;
    }

    private Component crearEmailField() {
        emailField = new EmailField("Email");
        emailField.setPlaceholder("ejemplo@correo.com");
        emailField.setWidthFull();
        emailField.getStyle()
            .set("border-radius", "6px")
            .set("--lumo-contrast-10pct", "rgba(44, 62, 80, 0.1)")
            .set("--lumo-primary-color", "#3498db")
            .set("margin-bottom", "1em");
        return emailField;
    }

    private Component crearContraseñaField() {
        contraseñaField = new PasswordField("Contraseña");
        contraseñaField.setWidthFull();
        contraseñaField.getStyle()
            .set("border-radius", "6px")
            .set("--lumo-contrast-10pct", "rgba(44, 62, 80, 0.1)")
            .set("--lumo-primary-color", "#3498db")
            .set("margin-bottom", "1em");
        return contraseñaField;
    }

    private Component crearIniciarSesionBtn() {
        // Botón: Iniciar Sesión
        iniciarSesionBtn = new Button("Iniciar Sesión", _ -> {
            // Comprobar si los campos están vacíos
            if (emailField.getValue().isEmpty() || contraseñaField.getValue().isEmpty()) {
                mostrarError("Por favor ingrese todos los campos.");
                return;
            }

            // Crear el objeto de usuario
            String tipoUsuarioSeleccionado = tipoUsuario.getValue();
            Usuario usuario = null;

            if ("Voluntario".equals(tipoUsuarioSeleccionado)) {
                usuario = new Voluntario();
                ((Voluntario) usuario).setEmail(emailField.getValue());
                ((Voluntario) usuario).setPassword(contraseñaField.getValue());
            } else {
                usuario = new Afectado();
                ((Afectado) usuario).setEmail(emailField.getValue());
                ((Afectado) usuario).setPassword(contraseñaField.getValue());
            }

            // Autenticar usuario
            autenticarUsuario(usuario);
        });
        
        // Estilo consistente con RegistroView
        iniciarSesionBtn.getStyle()
            .set("background-color", "#3498db")
            .set("color", "white")
            .set("border-radius", "6px")
            .set("font-weight", "600")
            .set("margin-top", "1em")
            .set("margin-bottom", "1em")
            .set("box-shadow", "0 4px 6px rgba(52, 152, 219, 0.2)")
            .set("transition", "transform 0.1s ease-in-out")
            .set("width", "100%");
            
        iniciarSesionBtn.getElement().addEventListener("mouseover", _ -> 
            iniciarSesionBtn.getStyle().set("transform", "translateY(-2px)"));
            
        iniciarSesionBtn.getElement().addEventListener("mouseout", _ -> 
            iniciarSesionBtn.getStyle().set("transform", "translateY(0)"));
        
        return iniciarSesionBtn;
    }

    private Component crearRegistrarBtn() {
        // Botón para registrarse
        registrarBtn = new Button("¿No tienes cuenta? Regístrate", _ -> {
            handlerRegistrarBtn.pulsarRegistrarBtn();
        });
        
        // Estilo consistente
        registrarBtn.getStyle()
            .set("background-color", "white")
            .set("color", "#3498db")
            .set("border", "1px solid #3498db")
            .set("border-radius", "6px")
            .set("font-weight", "500")
            .set("width", "100%")
            .set("margin-top", "1.2em");
            
        return registrarBtn;
    }

    private Component crearContraseñaOlvidadaLink() {
        // Link de contraseña olvidada
        contraseñaOlvidadaLink = new Anchor("#", "¿Ha olvidado su contraseña?");
        contraseñaOlvidadaLink.getStyle()
            .set("font-size", "14px")
            .set("color", "#3498db")
            .set("text-align", "center")
            .set("display", "block")
            .set("margin-top", "0.5em");
        return contraseñaOlvidadaLink;
    }

    private void autenticarUsuario(Usuario usuario) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/usuarios/login";

        try {
            Usuario response = restTemplate.postForObject(url, usuario, Usuario.class);

            if (response != null) {
                VaadinSession.getCurrent().setAttribute("usuario", response);
                UI.getCurrent().navigate("main");
            } else {
                mostrarError("Credenciales incorrectas");
            }
        } catch (Exception e) {
            mostrarError("Las credenciales son incorrectas");
        }
    }
    
    private void mostrarError(String mensaje) {
        Notification notification = Notification.show(
                mensaje,
                3000,
                Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    // Getters para pruebas
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
