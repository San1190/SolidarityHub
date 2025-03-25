package SolidarityHub.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;


import aj.org.objectweb.asm.Label;

@Route("login") // Define la ruta accesible en la URL
public class LoginView extends VerticalLayout {

    public LoginView() {
        /*
        // Campos del formulario
        TextField email = new TextField("Email");
        PasswordField contraseña = new PasswordField("Contraseña");

        // Botón de envío
        Button iniciarSesionBtn = new Button("Iniciar Sesión");

        // Alinear los componentes verticalmente
        add(email, contraseña, iniciarSesionBtn);

        // Centrar el formulario en la vista
        setHorizontalComponentAlignment(Alignment.CENTER, email, contraseña, iniciarSesionBtn);
        */

        // Configuración del diseño principal
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        // Panel izquierdo: Logo y nombre
        VerticalLayout panelIzq = new VerticalLayout();
        panelIzq.setWidth("50%");
        panelIzq.setAlignItems(Alignment.CENTER);
        panelIzq.setJustifyContentMode(JustifyContentMode.CENTER);

        

        // Agregar logo al panel izquierdo
        panelIzq.add(crearLogo());

        // Panel derecho: Formulario de inicio de sesión
        VerticalLayout panelDer = new VerticalLayout();
        panelDer.setWidth("50%");
        panelDer.setAlignItems(Alignment.CENTER);
        panelDer.setJustifyContentMode(JustifyContentMode.CENTER);


        

        // Agregar componentes al panel derecho
        panelDer.add(crearTipoUsuario(), crearEmailField(), crearContraseñaField(), crearIniciarSesionBtn(), crearContraseñaOlvidadaLink(), crearRegistrarBtn());

        // Agregar paneles al diseño principal
        add(panelIzq, panelDer);
    }

    private Component crearLogo() {
        // Cargar el logo
        Image logo = new Image("https://cliente.tuneupprocess.com/ApiWeb/UploadFiles/be802ceb-49c7-493f-945a-078ed3b6bb4d.jpg/LogoSH.jpg", "Solidarity Hub Logo");
        logo.setWidth("150px");
        return logo;
    }

    private Component crearTipoUsuario() {
        // Grupo de botones para seleccionar tipo de usuario
        RadioButtonGroup<String> tipoUsuario = new RadioButtonGroup<>();
        tipoUsuario.setItems("Voluntario", "Afectado");
        tipoUsuario.setValue("Voluntario"); // Valor predeterminado
        return tipoUsuario;
    }

    private Component crearEmailField() {
        // Email
        EmailField emailField = new EmailField("Email");
        emailField.setPlaceholder("ejemplo@correo.com");
        return emailField;
    }

    private Component crearContraseñaField() {
        // Contraseña
        PasswordField contraseñaField = new PasswordField("Contraseña");
        return contraseñaField;
    }

    private Component crearIniciarSesionBtn() {
        // Botón: Iniciar Sesión
        Button iniciarSesionBtn = new Button("Iniciar Sesión", event -> {
            UI.getCurrent().navigate("MainView");
        });
        iniciarSesionBtn.getStyle().set("background-color", "black").set("color", "white");
        return iniciarSesionBtn;
    }

    private Component crearRegistrarBtn() {
        // Botón para registrarse si no tiene cuenta
        Button registrarBtn = new Button("¿No tienes cuenta? Regístrate", event -> {
            UI.getCurrent().navigate("RegistroView");
        });
        registrarBtn.getStyle().set("background-color", "white").set("color", "black");
        return registrarBtn;
    }

    private Component crearContraseñaOlvidadaLink() {
        // Link de contraseña olvidada
        Anchor contraseñaOlvidadaLink = new Anchor("#", "¿Ha olvidado su contraseña?");
        contraseñaOlvidadaLink.getStyle().set("font-size", "12px").set("color", "#000");
        return contraseñaOlvidadaLink;
    }
}