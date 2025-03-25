package SolidarityHub.views;

import SolidarityHub.controllers.UsuarioControlador;
import SolidarityHub.models.Afectado;
import SolidarityHub.models.Habilidad;
import SolidarityHub.models.Voluntario;
import SolidarityHub.models.Necesidad;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Route("registro")
@PageTitle("Registro | SolidarityHub")
public class RegistroView extends VerticalLayout {
    
    private final UsuarioControlador usuarioControlador;
    private final Binder<Voluntario> voluntarioBinder = new Binder<>(Voluntario.class);
    private final Binder<Afectado> afectadoBinder = new Binder<>(Afectado.class);
    
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
    private ComboBox<String> necesidadField;
    
    // Foto de perfil
    private byte[] fotoBytes;
    private MemoryBuffer buffer;
    private Upload upload;
    
    public RegistroView(UsuarioControlador usuarioControlador) {
        this.usuarioControlador = usuarioControlador;
        
        addClassName("registro-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        add(
            createTitle(),
            createTipoUsuarioSelection(),
            createCommonFields(),
            createVoluntarioFields(),
            createAfectadoFields(),
            createButtonLayout()
        );
        
        configureBinders();
        configureVisibility();
    }
    
    private Component createTitle() {
        H1 title = new H1("Crear una cuenta");
        title.addClassNames(
            LumoUtility.Margin.NONE,
            LumoUtility.FontSize.XXXLARGE
        );
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
        formLayout.setMaxWidth("600px");
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        dniField = new TextField("DNI");
        nombreField = new TextField("Nombre");
        apellidosField = new TextField("Apellidos");
        emailField = new EmailField("Email");
        passwordField = new PasswordField("Contraseña");
        confirmPasswordField = new PasswordField("Confirmar contraseña");
        telefonoField = new TextField("Teléfono");
        direccionField = new TextField("Dirección");
        
        // Configurar upload de foto
        buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        upload.setMaxFileSize(5 * 1024 * 1024); // 5MB
        upload.setDropLabel(new Span("Arrastra y suelta tu foto de perfil aquí"));
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
            upload
        );
        
        return formLayout;
    }
    
    private Component createVoluntarioFields() {
        camposVoluntario = new Div();
        camposVoluntario.setWidth("100%");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setMaxWidth("600px");
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        H3 titulo = new H3("Información adicional para voluntarios");
        
        // Habilidades
        habilidadesGroup = new CheckboxGroup<>();
        habilidadesGroup.setLabel("Habilidades");

        habilidadesGroup.setItems(Arrays.asList(
            "Limpieza", 
            "Cocina", 
            "Compra de Alimentos", 
            "Distribución de Alimentos",
            "Transporte de Alimentos",
            "Lavandería",
            "Psicología",
            "Terapia",
            "Consejería",
            "Transporte"
        ));
        
        // Disponibilidad horaria
        horaInicioField = new TimePicker("Hora de inicio disponibilidad");
        horaFinField = new TimePicker("Hora de fin disponibilidad");
        
        formLayout.add(
            titulo,
            habilidadesGroup,
            horaInicioField, horaFinField
        );
        
        camposVoluntario.add(formLayout);
        return camposVoluntario;
    }
    
    private Component createAfectadoFields() {
        camposAfectado = new Div();
        camposAfectado.setWidth("100%");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setMaxWidth("600px");
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        H3 titulo = new H3("Información adicional para afectados");
        
        // Situación
        necesidadField = new ComboBox<>();
        necesidadField.setLabel("Necesidades");
        necesidadField.setItems(Arrays.asList(
            "Alimentación", 
            "Salud", 
            "Refugio", 
            "Ropa",
            "Servicio de Limpieza",
            "Ayuda Psicológica"
        ));
        
        formLayout.add(
            titulo,
            necesidadField
        );
        
        camposAfectado.add(formLayout);
        return camposAfectado;
    }
    
    private Component createButtonLayout() {
        Button cancelar = new Button("Cancelar");
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelar.addClickListener(_ -> UI.getCurrent().navigate(""));
        
        Button registrar = new Button("Registrarse");
        registrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registrar.addClickShortcut(Key.ENTER);
        registrar.addClickListener(_ -> onRegistrar());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelar, registrar);
        buttonLayout.setPadding(true);
        
        return buttonLayout;
    }
    
    private void configureBinders() {
        // Configurar binder para Voluntario
        voluntarioBinder.forField(dniField)
            .withValidator(new StringLengthValidator(
                "El DNI es obligatorio", 1, null))
            .bind(Voluntario::getDni, Voluntario::setDni);
            
        voluntarioBinder.forField(nombreField)
            .withValidator(new StringLengthValidator(
                "El nombre es obligatorio", 1, null))
            .bind(Voluntario::getNombre, Voluntario::setNombre);
            
        voluntarioBinder.forField(apellidosField)
            .withValidator(new StringLengthValidator(
                "Los apellidos son obligatorios", 1, null))
            .bind(Voluntario::getApellidos, Voluntario::setApellidos);
            
        voluntarioBinder.forField(emailField)
            .withValidator(new EmailValidator(
                "Por favor ingrese un email válido"))
            .bind(Voluntario::getEmail, Voluntario::setEmail);
            
        voluntarioBinder.forField(telefonoField)
            .bind(Voluntario::getTelefono, Voluntario::setTelefono);
            
        voluntarioBinder.forField(direccionField)
            .bind(Voluntario::getDireccion, Voluntario::setDireccion);
            
        voluntarioBinder.forField(passwordField)
            .withValidator(new StringLengthValidator(
                "La contraseña debe tener al menos 8 caracteres", 8, null))
            .bind(Voluntario::getPassword, Voluntario::setPassword);
        
        // Configurar binder para Afectado
        afectadoBinder.forField(dniField)
            .withValidator(new StringLengthValidator(
                "El DNI es obligatorio", 1, null))
            .bind(Afectado::getDni, Afectado::setDni);
            
        afectadoBinder.forField(nombreField)
            .withValidator(new StringLengthValidator(
                "El nombre es obligatorio", 1, null))
            .bind(Afectado::getNombre, Afectado::setNombre);
            
        afectadoBinder.forField(apellidosField)
            .withValidator(new StringLengthValidator(
                "Los apellidos son obligatorios", 1, null))
            .bind(Afectado::getApellidos, Afectado::setApellidos);
            
        afectadoBinder.forField(emailField)
            .withValidator(new EmailValidator(
                "Por favor ingrese un email válido"))
            .bind(Afectado::getEmail, Afectado::setEmail);
            
        afectadoBinder.forField(telefonoField)
            .bind(Afectado::getTelefono, Afectado::setTelefono);
            
        afectadoBinder.forField(direccionField)
            .bind(Afectado::getDireccion, Afectado::setDireccion);
            
        afectadoBinder.forField(passwordField)
            .withValidator(new StringLengthValidator(
                "La contraseña debe tener al menos 8 caracteres", 8, null))
            .bind(Afectado::getPassword, Afectado::setPassword);
    }
    
    private void configureVisibility() {
        String tipoSeleccionado = tipoUsuarioRadio.getValue();
        
        if ("Voluntario".equals(tipoSeleccionado)) {
            camposVoluntario.setVisible(true);
            camposAfectado.setVisible(false);
        } else {
            camposVoluntario.setVisible(false);
            camposAfectado.setVisible(true);
        }
    }
    
    private void onRegistrar() {
        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            Notification notification = Notification.show(
                "Las contraseñas no coinciden",
                3000,
                Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        try {
            boolean registroExitoso = false;
            
            if ("Voluntario".equals(tipoUsuarioRadio.getValue())) {
                Voluntario voluntario = new Voluntario();
                voluntarioBinder.writeBean(voluntario);
                
                // Configurar datos específicos de voluntario
                if (fotoBytes != null) {
                    voluntario.setFoto(fotoBytes);
                }
                
                // Convertir habilidades seleccionadas a lista de Habilidad
                List<Habilidad> listaHabilidades = new ArrayList<>();
                if (habilidadesGroup.getValue() != null) {
                    for (String nombreHabilidad : habilidadesGroup.getValue()) {
                        Habilidad habilidad = Habilidad.valueOf(nombreHabilidad.toUpperCase().replace(" ", "_"));
                        listaHabilidades.add(habilidad);
                    }
                }
                voluntario.setHabilidades(listaHabilidades);
                
                voluntario.setHoraInicioTrabajo(horaInicioField.getValue());
                voluntario.setHoraFinTrabajo(horaFinField.getValue());
                
                registroExitoso = usuarioControlador.crearUsuario(voluntario) != null;
            } else {
                Afectado afectado = new Afectado();
                afectadoBinder.writeBean(afectado);
                
                // Configurar datos específicos de afectado
                if (fotoBytes != null) {
                    afectado.setFoto(fotoBytes);
                }
                
                if (necesidadField.getValue() != null) {
                    List<Necesidad> necesidades = new ArrayList<>();
                    
                    // Create a new Necesidad object
                    Necesidad necesidad = new Necesidad();
                    
                    // Convert the selected string to the appropriate TipoNecesidad enum value
                    String seleccion = necesidadField.getValue().toUpperCase().replace(" DE ", "_").replace(" ", "_");
                    try {
                        Necesidad.TipoNecesidad tipoNecesidad = Necesidad.TipoNecesidad.valueOf(seleccion);
                        necesidad.setTipoNecesidad(tipoNecesidad);
                        
                        // Set default values for other required fields
                        necesidad.setDescripcion("Necesidad registrada durante el registro de usuario");
                        necesidad.setEstadoNecesidad(Necesidad.EstadoNecesidad.REGISTRADA);
                        necesidad.setUrgencia(Necesidad.Urgencia.MEDIA);
                        necesidad.setUbicacion(direccionField.getValue());
                        necesidad.setFechaCreacion(LocalDateTime.now());
                        
                        necesidades.add(necesidad);
                        afectado.setNecesidades(necesidades);
                    } catch (IllegalArgumentException e) {
                        // Handle case where the string doesn't match any enum value
                        Notification.show(
                            "Error al procesar la necesidad seleccionada. Por favor, seleccione otra opción.",
                            3000, 
                            Notification.Position.MIDDLE
                        );
                    }
                }
                
                registroExitoso = usuarioControlador.crearUsuario(afectado) != null;
            }
            
            if (registroExitoso) {
                Notification.show("¡Registro exitoso! Puede iniciar sesión ahora.", 
                    3000, Notification.Position.MIDDLE);
                clearForm();
                // Redirigir al login
                UI.getCurrent().navigate("login");
            } else {
                Notification notification = Notification.show(
                    "Error al registrar: El email o DNI ya existe",
                    3000,
                    Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (ValidationException e) {
            Notification notification = Notification.show(
                "Por favor corrija los errores en el formulario: " + e.getMessage(),
                3000,
                Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
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
        
        necesidadField.clear();
        
        fotoBytes = null;
        upload.getElement().setProperty("files", null);
    }
}
