package SolidarityHub.views;

import SolidarityHub.controllers.UsuarioControlador;
import SolidarityHub.factories.FabricaUsuario;
import SolidarityHub.models.*;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Route("registro")
@PageTitle("Registro | SolidarityHub")
public class RegistroView extends VerticalLayout {

    private final UsuarioControlador usuarioControlador;

    private RadioButtonGroup<String> tipoUsuarioRadio;

    private TextField dniField;
    private TextField nombreField;
    private TextField apellidosField;
    private EmailField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField telefonoField;
    private TextField direccionField;

    private Div camposVoluntario;
    private CheckboxGroup<String> habilidadesGroup;
    private TimePicker horaInicioField;
    private TimePicker horaFinField;

    private byte[] fotoBytes;
    private MemoryBuffer buffer;
    private Upload upload;

    // Logo
    private Image logo;

    public RegistroView(UsuarioControlador usuarioControlador) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        setAlignItems(Alignment.CENTER);
        
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
        
        this.usuarioControlador = usuarioControlador;

        Div formCard = new Div();
        formCard.addClassName("form-card");
        formCard.getStyle()
            .set("background-color", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 8px 24px rgba(0,0,0,0.1)")
            .set("padding", "2em")
            .set("max-width", "800px")
            .set("width", "100%")
            .set("margin", "2em auto");
            
        formCard.add(
            createLogo(),
            createTitle(),
            createTipoUsuarioSelection(),
            createCommonFields(),
            createVoluntarioFields(),
            createButtonLayout()
        );
        
        add(formCard);
        configureVisibility();
    }

    private Component createLogo() {
        // Cargar el logo
        logo = new Image(
                "https://cliente.tuneupprocess.com/ApiWeb/UploadFiles/be802ceb-49c7-493f-945a-078ed3b6bb4d.jpg/LogoSH.jpg",
                "Solidarity Hub Logo");
        logo.setWidth("150px");

        // Center the logo
        HorizontalLayout logoLayout = new HorizontalLayout(logo);
        logoLayout.setWidthFull();
        logoLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        return logoLayout;
    }

    private Component createTitle() {
        H1 title = new H1("Crear una cuenta");
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

    private Component createTipoUsuarioSelection() {
        tipoUsuarioRadio = new RadioButtonGroup<>();
        tipoUsuarioRadio.setLabel("Tipo de cuenta");
        tipoUsuarioRadio.setItems("Afectado", "Voluntario");
        tipoUsuarioRadio.setValue("Afectado");
        tipoUsuarioRadio.addValueChangeListener(_ -> {
            configureVisibility();
        });
        
        // Style the radio group
        tipoUsuarioRadio.getStyle()
            .set("margin-bottom", "2em")
            .set("border-radius", "8px")
            .set("padding", "2em 10em")
            .set("background-color", "rgba(52, 152, 219, 0.05)");
            
        return tipoUsuarioRadio;
    }

    private Component createCommonFields() {
        FormLayout formLayout = new FormLayout();
        formLayout.setMaxWidth("700px");
        formLayout.getStyle().set("margin-bottom", "2em");
        formLayout.getStyle().set("margin-top", "2em");
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2));

        dniField = new TextField("DNI");
        dniField.setRequired(true);
        dniField.setPattern("\\d{8}[A-Za-z]");
        dniField.setErrorMessage("DNI con letra.");

        nombreField = new TextField("Nombre");
        nombreField.setRequired(true);
        nombreField.setErrorMessage("Nombre no puede estar vacío.");

        apellidosField = new TextField("Apellidos");
        apellidosField.setRequired(true);
        apellidosField.setErrorMessage("Apellidos no pueden estar vacíos.");

        emailField = new EmailField("Email");
        emailField.setRequired(true);
        emailField.setPattern(".*@.*"); // Must contain '@'
        emailField.setErrorMessage("Email debe contener '@' y no puede estar vacío.");

        passwordField = new PasswordField("Contraseña");
        passwordField.setRequired(true);
        passwordField.setMinLength(8);
        passwordField.setErrorMessage("Contraseña debe tener al menos 8 caracteres.");

        confirmPasswordField = new PasswordField("Confirmar contraseña");
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setErrorMessage("Confirmar contraseña no puede estar vacía.");

        telefonoField = new TextField("Teléfono");
        telefonoField.setRequired(true);
        telefonoField.setPattern("\\d{5,}$");
        telefonoField.setErrorMessage("Teléfono debe tener al menos 5 digitos.");

        direccionField = new TextField("Dirección");
        direccionField.setRequired(true);
        direccionField.setErrorMessage("La dirección no puede estar vacía.");

        // Configurar upload de foto
        buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFileSize(5 * 1024 * 1024); // 5MB
        upload.setDropLabel(new Span("Sube o arrastra tu foto de perfil aquí"));
        upload.setUploadButton(new Button("Subir foto"));

        upload.addSucceededListener(_ -> {
            try {
                // Convertir InputStream a byte[]
                InputStream inputStream = buffer.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                fotoBytes = outputStream.toByteArray();
                Notification.show("Foto subida con éxito", 3000, Notification.Position.BOTTOM_CENTER);
            } catch (IOException ex) {
                Notification.show("Error al procesar la imagen: " + ex.getMessage(),
                        3000, Notification.Position.MIDDLE);
            }
        });

        // Add styles to form fields
        Stream.of(dniField, nombreField, apellidosField, emailField, 
                  passwordField, confirmPasswordField, telefonoField, direccionField)
            .forEach(field -> {
                field.getStyle()
                    .set("border-radius", "6px")
                    .set("--lumo-contrast-10pct", "rgba(44, 62, 80, 0.1)")
                    .set("--lumo-primary-color", "#3498db");
            });
            
        // Style upload component
        upload.getStyle()
            .set("border", "2px dashed #3498db")
            .set("border-radius", "8px")
            .set("padding", "1em")
            .set("background-color", "rgba(52, 152, 219, 0.05)");

        formLayout.add(
                dniField, nombreField,
                apellidosField, emailField,
                telefonoField, direccionField,
                passwordField, confirmPasswordField,
                upload);

        return formLayout;
    }

    private Component createVoluntarioFields() {
        camposVoluntario = new Div();
        camposVoluntario.setWidth("100%");

        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setMaxWidth("700px");

        // Group title and subtitle in a Div
        Div headerDiv = new Div();
        headerDiv.setWidthFull();

        H3 titulo = new H3("Información adicional para voluntarios");
        Paragraph subtitulo = new Paragraph("Podras modificar estos datos más tarde");
        subtitulo.getStyle().set("font-size", "small");
        subtitulo.getStyle().set("margin-top", "1em");

        headerDiv.add(titulo, subtitulo);
        
        titulo.getStyle()
            .set("color", "#2c3e50")
            .set("margin", "0");

        // Habilidades
        habilidadesGroup = new CheckboxGroup<>();
        habilidadesGroup.setLabel("Habilidades");

        List<String> habilidadesNombres = Arrays.stream(Habilidad.values())
                .map(Habilidad::getNombre)
                .collect(java.util.stream.Collectors.toList());

        habilidadesGroup.setItems(habilidadesNombres);

        habilidadesGroup.getStyle()
            .set("background-color", "rgba(236, 240, 243, 0.5)")
            .set("padding", "1em")
            .set("border-radius", "8px");

        HorizontalLayout hlLayout = new HorizontalLayout();
        horaInicioField = new TimePicker("Hora de inicio disponibilidad");
        horaFinField = new TimePicker("Hora de fin disponibilidad");
        horaInicioField.setStep(Duration.ofMinutes(30));
        horaFinField.setStep(Duration.ofMinutes(30));
        horaInicioField.setWidth("300px");
        horaFinField.setWidth("300px");

        Stream.of(horaInicioField, horaFinField)
            .forEach(field -> {
                field.getStyle()
                    .set("--lumo-primary-color", "#3498db");
            });

        hlLayout.add(horaInicioField, horaFinField);
        hlLayout.setAlignItems(Alignment.CENTER);
        vLayout.add(headerDiv, habilidadesGroup, hlLayout);
        camposVoluntario.add(vLayout);
        return camposVoluntario;
    }

    private Component createButtonLayout() {
        Button cancelar = new Button("Cancelar");
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelar.addClickListener(_ -> UI.getCurrent().navigate("/"));
        cancelar.addClickListener(_ -> UI.getCurrent().navigate("/"));

        Button registrar = new Button("Registrarse");
        registrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registrar.addClickShortcut(Key.ENTER);
        registrar.addClickListener(_ -> onRegistrar());

        registrar.getStyle()
            .set("background-color", "#3498db")
            .set("color", "white")
            .set("border-radius", "6px")
            .set("font-weight", "600")
            .set("box-shadow", "0 4px 6px rgba(52, 152, 219, 0.2)")
            .set("transition", "transform 0.1s ease-in-out");
            
        registrar.getElement().addEventListener("mouseover", _ -> 
            registrar.getStyle().set("transform", "translateY(-2px)"));
            
        registrar.getElement().addEventListener("mouseout", _ -> 
            registrar.getStyle().set("transform", "translateY(0)"));

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelar, registrar);
        buttonLayout.setPadding(true);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonLayout.getStyle()
            .set("margin-top", "2em");
            
        return buttonLayout;
    }

    private void configureVisibility() {
        boolean isVoluntario = "Voluntario".equals(tipoUsuarioRadio.getValue());
        camposVoluntario.setVisible(isVoluntario);
    }

    private void onRegistrar() {
        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            mostrarError("Las contraseñas no coinciden");
            return;
        }

        boolean registroExitoso = false;
        FabricaUsuario fabricaUsuario = new FabricaUsuario();

        try {
            if ("Voluntario".equals(tipoUsuarioRadio.getValue())) {
                registroExitoso = registrarVoluntario(fabricaUsuario);
            } else {
                registroExitoso = registrarAfectado(fabricaUsuario);
            }

            if (registroExitoso) {
                Notification.show("¡Registro exitoso! Puede iniciar sesión ahora.",
                        3000, Notification.Position.MIDDLE);
                clearForm();

                UI.getCurrent().navigate("/src");
            } else {
                mostrarError("Error al registrar: El email o DNI ya existe");
            }
        } catch (Exception e) {
            mostrarError("Error en el registro: " + e.getMessage());
        }
    }

    private boolean registrarVoluntario(FabricaUsuario fabricaUsuario) {
        List<Habilidad> listaHabilidades = new ArrayList<>();
        if (habilidadesGroup.getValue() != null) {
            for (String nombreHabilidad : habilidadesGroup.getValue()) {
                Habilidad habilidad = Habilidad.valueOf(nombreHabilidad.toUpperCase().replace(" ", "_"));
                listaHabilidades.add(habilidad);
            }
        }

        Usuario voluntario = fabricaUsuario.crearUsuario(
                tipoUsuarioRadio.getValue(),
                dniField.getValue(),
                nombreField.getValue(),
                apellidosField.getValue(),
                emailField.getValue(),
                passwordField.getValue(),
                telefonoField.getValue(),
                direccionField.getValue(),
                fotoBytes,
                null,
                listaHabilidades.isEmpty() ? null : listaHabilidades,
                horaInicioField.getValue(),
                horaFinField.getValue());

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/usuarios/registrar";

        Usuario voluntarioRegistrado = restTemplate.postForObject(url, voluntario, Usuario.class);
        return voluntarioRegistrado != null;
    }

    private boolean registrarAfectado(FabricaUsuario fabricaUsuario) {
        Usuario afectado = fabricaUsuario.crearUsuario(
                tipoUsuarioRadio.getValue(),
                dniField.getValue(),
                nombreField.getValue(),
                apellidosField.getValue(),
                emailField.getValue(),
                passwordField.getValue(),
                telefonoField.getValue(),
                direccionField.getValue(),
                fotoBytes,
                null,
                null,
                null,
                null);

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/usuarios/registrar";

        Usuario afectadoRegistrado = restTemplate.postForObject(url, afectado, Usuario.class);
        return usuarioControlador.crearUsuario(afectadoRegistrado) != null;
    }

    private void mostrarError(String mensaje) {
        Notification notification = Notification.show(
                mensaje,
                3000,
                Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void clearForm() {
        dniField.clear();
        nombreField.clear();
        apellidosField.clear();
        emailField.clear();
        telefonoField.clear();
        direccionField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        habilidadesGroup.clear();
        horaInicioField.clear();
        horaFinField.clear();
        fotoBytes = null;
        upload.getElement().setProperty("files", null);
    }

}
