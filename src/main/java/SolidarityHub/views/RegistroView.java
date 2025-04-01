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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Route("registro")
@PageTitle("Registro | SolidarityHub")
public class RegistroView extends VerticalLayout {

    private final UsuarioControlador usuarioControlador;

    // Selección de tipo de usuario
    private RadioButtonGroup<String> tipoUsuarioRadio;

    // Campos comunes
    private TextField dniField;
    private TextField nombreField;
    private TextField apellidosField;
    private EmailField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField telefonoField;
    private TextField direccionField;

    // Campos para voluntario
    private Div camposVoluntario;
    private CheckboxGroup<String> habilidadesGroup;
    private TimePicker horaInicioField;
    private TimePicker horaFinField;

    // Campos para afectado
    private Div camposAfectado;
    private CheckboxGroup<String> necesidadesGroup;

    // Foto de perfil
    private byte[] fotoBytes;
    private MemoryBuffer buffer;
    private Upload upload;

    // Logo
    private Image logo;

    public RegistroView(UsuarioControlador usuarioControlador) {
        // Configuración del diseño principal
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        this.usuarioControlador = usuarioControlador;

        addClassName("registro-view");
        setAlignItems(Alignment.STRETCH);
        setJustifyContentMode(JustifyContentMode.EVENLY);

        add(
                createLogo(),
                createTitle(),
                createTipoUsuarioSelection(),
                createCommonFields(),
                createVoluntarioFields(),
                createAfectadoFields(),
                createButtonLayout()
            );

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
        return title;
    }

    private Component createTipoUsuarioSelection() {
        tipoUsuarioRadio = new RadioButtonGroup<>();
        tipoUsuarioRadio.setLabel("Tipo de cuenta");
        tipoUsuarioRadio.setItems("Afectado", "Voluntario");
        tipoUsuarioRadio.setValue("Afectado"); // Valor por defecto
        tipoUsuarioRadio.addValueChangeListener(_ -> {
            configureVisibility();
        });

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

        FormLayout formLayout = new FormLayout();
        formLayout.setMaxWidth("700px");
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2));

        // Group title and subtitle in a Div
        Div headerDiv = new Div();
        headerDiv.setWidthFull();
        
        H3 titulo = new H3("Información adicional para voluntarios");
        Paragraph subtitulo = new Paragraph("Podras modificar estos datos más tarde");
        subtitulo.getStyle().set("font-size", "small");
        subtitulo.getStyle().set("margin-top", "1em");
        
        headerDiv.add(titulo, subtitulo);

        // Habilidades
        habilidadesGroup = new CheckboxGroup<>();
        habilidadesGroup.setLabel("Habilidades");
        
        List<String> habilidadesNombres = Arrays.stream(Habilidad.values())
                .map(Habilidad::getNombre)
                .collect(java.util.stream.Collectors.toList());
        
        habilidadesGroup.setItems(habilidadesNombres);

        HorizontalLayout hlLayout = new HorizontalLayout();
        horaInicioField = new TimePicker("Hora de inicio disponibilidad");
        horaFinField = new TimePicker("Hora de fin disponibilidad");

        formLayout.add(headerDiv, habilidadesGroup);
        formLayout.setColspan(headerDiv, 1); // Place header in the left column
        formLayout.setColspan(habilidadesGroup, 1); // Place checkboxes in the right column

        formLayout.add(horaInicioField, horaFinField);
        formLayout.setColspan(horaInicioField, 1);
        formLayout.setColspan(horaFinField, 1);

        camposVoluntario.add(formLayout);
        return camposVoluntario;
    }

    private Component createAfectadoFields() {
        camposAfectado = new Div();
        camposAfectado.setWidth("100%");

        FormLayout formLayout = new FormLayout();
        formLayout.setMaxWidth("700px");
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2));

        // Group title and subtitle in a Div
        Div headerDiv = new Div();
        headerDiv.setWidthFull();
        
        H3 titulo = new H3("Información adicional para afectados");
        Paragraph subtitulo = new Paragraph("Podras modificar estos datos más tarde");
        subtitulo.getStyle().set("font-size", "small");
        subtitulo.getStyle().set("margin-top", "1em");
        
        headerDiv.add(titulo, subtitulo);

        // Change ComboBox to CheckboxGroup
        necesidadesGroup = new CheckboxGroup<>();
        necesidadesGroup.setLabel("Necesidades");
        
        List<String> necesidadesNombres = Arrays.stream(Necesidad.TipoNecesidad.values())
                .map(tipo -> {
                    String name = tipo.name().replace("_", " ").toLowerCase();
                    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
                })
                .collect(java.util.stream.Collectors.toList());
        
        necesidadesGroup.setItems(necesidadesNombres);

        formLayout.add(headerDiv, necesidadesGroup);
        formLayout.setColspan(headerDiv, 1); // Place header in the left column
        formLayout.setColspan(necesidadesGroup, 1); // Place checkboxes in the right column

        camposAfectado.add(formLayout);
        return camposAfectado;
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

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelar, registrar);
        buttonLayout.setPadding(true);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        return buttonLayout;
    }

    private void configureVisibility() {
        camposVoluntario.setVisible("Voluntario".equals(tipoUsuarioRadio.getValue()));
    }

    private void onRegistrar() {
        // Validar que las contraseñas coinciden
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
                // Redirigir al login
                UI.getCurrent().navigate("login");
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

        return usuarioControlador.crearUsuario(voluntario) != null;
    }

    private boolean registrarAfectado(FabricaUsuario fabricaUsuario) {
        List<Necesidad> listaNecesidades = new ArrayList<>();

        if (necesidadesGroup.getValue() != null && !necesidadesGroup.getValue().isEmpty()) {
            for (String nombreNecesidad : necesidadesGroup.getValue()) {
                Necesidad necesidad = new Necesidad();
                
                // Convert selected name to enum
                Necesidad.TipoNecesidad tipoNecesidad = Necesidad.TipoNecesidad.valueOf(
                        nombreNecesidad.toUpperCase().replace(" ", "_"));
                necesidad.setTipoNecesidad(tipoNecesidad);

                // Set default values
                necesidad.setDescripcion("Necesidad registrada durante el registro de usuario");
                necesidad.setEstadoNecesidad(Necesidad.EstadoNecesidad.REGISTRADA);
                necesidad.setUrgencia(Necesidad.Urgencia.MEDIA);
                necesidad.setUbicacion(direccionField.getValue());
                necesidad.setFechaCreacion(LocalDateTime.now());

                listaNecesidades.add(necesidad);
            }
        }

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
                listaNecesidades.isEmpty() ? null : listaNecesidades, 
                null, 
                null, 
                null);
                
        return usuarioControlador.crearUsuario(afectado) != null;
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

        necesidadesGroup.clear();

        fotoBytes = null;
        upload.getElement().setProperty("files", null);
    }

}
