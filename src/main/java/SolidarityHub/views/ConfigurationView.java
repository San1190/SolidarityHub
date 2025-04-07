package SolidarityHub.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Afectado;
import SolidarityHub.models.Habilidad;
import SolidarityHub.models.Necesidad;
import SolidarityHub.services.UsuarioServicio;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;


import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@Route(value = "configuracion", layout = MainLayout.class)
@PageTitle("Configuración | SolidarityHub")
public class ConfigurationView extends VerticalLayout {

    private final UsuarioServicio usuarioServicio;
    private Usuario usuario;

    // Campos del formulario de datos personales (comunes)
    private TextField nombreField;
    private TextField apellidosField;
    private TextField emailField;
    private PasswordField passwordField;

    // Campos para voluntarios (habilidades, días y turnos)
    private CheckboxGroup<Habilidad> habilidadesGroup;
    private CheckboxGroup<String> diasDisponiblesGroup;
    private ComboBox<String> turnoDisponibilidadCombo;

    // Campos para afectados (necesidades)
    private CheckboxGroup<String> necesidadesGroup;

    public ConfigurationView(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
        
        // Recuperar el usuario actual desde la sesión
        usuario = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");
        if (usuario == null) {
            UI.getCurrent().navigate("/");
            return;
        }
        // Obtener el usuario actualizado de la BD
        usuario = usuarioServicio.obtenerUsuarioPorId(usuario.getId());

        // Configuración general de la vista
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        // Estilos consistentes con RegistroView y LoginView
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

        // Contenedor principal con estilo consistente
        Div formCard = new Div();
        formCard.addClassName("form-card");
        formCard.getStyle()
                .set("background-color", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 8px 24px rgba(0,0,0,0.1)")
                .set("padding", "2em")
                .set("max-width", "800px")
                .set("width", "90%")
                .set("margin", "2em auto");

        // Título de la página con estilo mejorado
        H1 title = new H1("Configuración de Usuario");
        title.addClassNames(
                LumoUtility.Margin.NONE,
                LumoUtility.FontSize.XXXLARGE);
        title.getStyle()
                .set("color", "#2c3e50")
                .set("text-align", "center")
                .set("margin-bottom", "1em")
                .set("font-weight", "600");

        // Separador estilizado
        Hr separador = new Hr();
        separador.getStyle()
            .set("margin-top", "1em")
            .set("margin-bottom", "1.2em")
            .set("width", "100%")
            .set("border", "none")
            .set("height", "2px")
            .set("background-color", "rgba(52, 152, 219, 0.3)");

        // Panel contenedor para separar el contenido en dos columnas
        HorizontalLayout panel = new HorizontalLayout();
        panel.setWidthFull();
        panel.setHeight("auto");
        panel.setSpacing(true);
        panel.setPadding(true);
        panel.setAlignItems(Alignment.START);

        // Panel izquierdo: se muestra según el tipo de usuario
        VerticalLayout panelIzq = new VerticalLayout();
        panelIzq.setWidthFull();
        panelIzq.setSpacing(true);
        panelIzq.setPadding(true);
        panelIzq.getStyle()
                .set("background-color", "rgba(52, 152, 219, 0.05)")
                .set("border-radius", "8px")
                .set("padding", "1.5rem");

        // Si el usuario es voluntario, se muestran horarios y habilidades
        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().equalsIgnoreCase("voluntario")) {
            panelIzq.add(crearDiasHorario(), crearTurnoHorario(), crearHabilidades());
        }
        // Si el usuario es afectado, se muestra la selección de necesidades
        else if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().equalsIgnoreCase("afectado")) {
            panelIzq.add(crearNecesidades(), crearListaNecesidades());
        }

        // Panel derecho: Avatar y formulario de datos personales
        VerticalLayout panelDer = new VerticalLayout();
        panelDer.setWidthFull();
        panelDer.setSpacing(true);
        panelDer.setPadding(true);
        panelDer.getStyle()
                .set("background-color", "rgba(236, 240, 243, 0.5)")
                .set("border-radius", "8px")
                .set("padding", "1.5rem");
        panelDer.add(crearAvatar(), crearFormInfo());

        // Panel de botones con estilo mejorado
        HorizontalLayout panelBtns = new HorizontalLayout();
        panelBtns.setWidthFull();
        panelBtns.setSpacing(true);
        panelBtns.setPadding(true);
        panelBtns.setJustifyContentMode(JustifyContentMode.CENTER);
        panelBtns.getStyle()
                .set("margin-top", "2em");
        panelBtns.add(crearCancelarBtn(), crearGuardarInfoBtn());

        panel.add(panelIzq, panelDer);
        formCard.add(title, separador, panel, panelBtns);
        add(formCard);
    }

    // Métodos para usuarios voluntarios

    private Component crearDiasHorario() {
        diasDisponiblesGroup = new CheckboxGroup<>();
        diasDisponiblesGroup.setLabel("Días de la semana disponible:");
        diasDisponiblesGroup.setItems("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo");
        diasDisponiblesGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        
        // Estilo mejorado para el grupo de checkboxes
        diasDisponiblesGroup.getStyle()
            .set("background-color", "rgba(236, 240, 243, 0.5)")
            .set("padding", "1em")
            .set("border-radius", "8px");
        
        // Si el usuario es un voluntario, marcamos los días que ya tiene seleccionados
        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().equalsIgnoreCase("voluntario")) {
            try {
                // Intentamos hacer un cast a Voluntario
                SolidarityHub.models.Voluntario voluntario = (SolidarityHub.models.Voluntario) usuario;
                if (voluntario.getDiasDisponibles() != null && !voluntario.getDiasDisponibles().isEmpty()) {
                    // Convertimos los días del voluntario a un Set para marcarlos en el checkbox
                    Set<String> diasSeleccionados = new HashSet<>(voluntario.getDiasDisponibles());
                    // Establecer los días seleccionados en el grupo de checkboxes
                    if (!diasSeleccionados.isEmpty()) {
                        diasDisponiblesGroup.setValue(diasSeleccionados);
                    }
                }
            } catch (ClassCastException e) {
                // Manejar el caso en que el usuario no sea un Voluntario
                Notification.show("Error al cargar los días disponibles", 
                    3000, Notification.Position.BOTTOM_CENTER);
            }
        }
        
        return diasDisponiblesGroup;
    }

    private Component crearTurnoHorario() {
        turnoDisponibilidadCombo = new ComboBox<>("Turno de disponibilidad:");
        turnoDisponibilidadCombo.setItems("Mañana", "Tarde", "Día Entero");
        turnoDisponibilidadCombo.setPlaceholder("Selecciona un turno");
        turnoDisponibilidadCombo.setClearButtonVisible(true);
        turnoDisponibilidadCombo.setWidthFull();
        
        // Estilo mejorado para el combobox
        turnoDisponibilidadCombo.getStyle()
            .set("border-radius", "6px")
            .set("--lumo-contrast-10pct", "rgba(44, 62, 80, 0.1)")
            .set("--lumo-primary-color", "#3498db")
            .set("margin-top", "1em")
            .set("margin-bottom", "1em");
        
        // Si el usuario es un voluntario, seleccionamos el turno que ya tiene configurado
        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().equalsIgnoreCase("voluntario")) {
            try {
                // Intentamos hacer un cast a Voluntario
                SolidarityHub.models.Voluntario voluntario = (SolidarityHub.models.Voluntario) usuario;
                if (voluntario.getTurnoDisponibilidad() != null && !voluntario.getTurnoDisponibilidad().isEmpty()) {
                    // Establecer el turno seleccionado en el combobox
                    turnoDisponibilidadCombo.setValue(voluntario.getTurnoDisponibilidad());
                }
            } catch (ClassCastException e) {
                // Manejar el caso en que el usuario no sea un Voluntario
                Notification.show("Error al cargar el turno disponible", 
                    3000, Notification.Position.BOTTOM_CENTER);
            }
        }
        
        return turnoDisponibilidadCombo;
    }

    private Component crearHabilidades() {
        // Usamos un CheckboxGroup para que el usuario seleccione sus habilidades
        habilidadesGroup = new CheckboxGroup<>();
        habilidadesGroup.setLabel("Selecciona tus Habilidades:");
        
        // Usamos los valores del enum Habilidad
        habilidadesGroup.setItems(Habilidad.values());
        habilidadesGroup.setItemLabelGenerator(Habilidad::getNombre);
        
        // Estilo mejorado para el grupo de habilidades
        habilidadesGroup.getStyle()
            .set("background-color", "rgba(236, 240, 243, 0.5)")
            .set("padding", "1em")
            .set("border-radius", "8px")
            .set("margin-top", "1em");
        
        // Si el usuario es un voluntario, marcamos las habilidades que ya tiene
        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().equalsIgnoreCase("voluntario")) {
            try {
                // Intentamos hacer un cast a Voluntario
                SolidarityHub.models.Voluntario voluntario = (SolidarityHub.models.Voluntario) usuario;
                if (voluntario.getHabilidades() != null && !voluntario.getHabilidades().isEmpty()) {
                    // Establecer las habilidades seleccionadas en el grupo de checkboxes
                    Set<Habilidad> habilidadesSeleccionadas = new HashSet<>(voluntario.getHabilidades());
                    if (!habilidadesSeleccionadas.isEmpty()) {
                        habilidadesGroup.setValue(habilidadesSeleccionadas);
                    }
                }
            } catch (ClassCastException e) {
                // Manejar el caso en que el usuario no sea un Voluntario
                Notification.show("Error al cargar las habilidades", 
                    3000, Notification.Position.BOTTOM_CENTER);
            }
        }
        
        habilidadesGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        return habilidadesGroup;
    }

    // Métodos para usuarios afectados

    private Component crearNecesidades() {
        // Usamos un CheckboxGroup para que el usuario seleccione sus necesidades
        necesidadesGroup = new CheckboxGroup<>();
        necesidadesGroup.setLabel("Selecciona tus Necesidades:");
        
        // Usamos los valores del enum TipoNecesidad
        necesidadesGroup.setItems(
            SolidarityHub.models.Necesidad.TipoNecesidad.PRIMEROS_AUXILIOS.toString(),
            SolidarityHub.models.Necesidad.TipoNecesidad.MEDICAMENTOS.toString(),
            SolidarityHub.models.Necesidad.TipoNecesidad.ALIMENTACION.toString(),
            SolidarityHub.models.Necesidad.TipoNecesidad.ALIMENTACION_BEBE.toString(),
            SolidarityHub.models.Necesidad.TipoNecesidad.REFUGIO.toString(),
            SolidarityHub.models.Necesidad.TipoNecesidad.ROPA.toString(),
            SolidarityHub.models.Necesidad.TipoNecesidad.SERVICIO_LIMPIEZA.toString(),
            SolidarityHub.models.Necesidad.TipoNecesidad.AYUDA_PSICOLOGICA.toString(),
            SolidarityHub.models.Necesidad.TipoNecesidad.AYUDA_CARPINTERIA.toString(),
            SolidarityHub.models.Necesidad.TipoNecesidad.AYUDA_ELECTRICIDAD.toString(),
            SolidarityHub.models.Necesidad.TipoNecesidad.AYUDA_FONTANERIA.toString(),
            SolidarityHub.models.Necesidad.TipoNecesidad.MATERIAL_HIGENE.toString()
        );
        
        // Estilo mejorado para el grupo de necesidades
        necesidadesGroup.getStyle()
            .set("background-color", "rgba(236, 240, 243, 0.5)")
            .set("padding", "1em")
            .set("border-radius", "8px")
            .set("margin-top", "1em");
        
        // Si el usuario es un afectado, marcamos las necesidades que ya tiene
        if (usuario instanceof Afectado) {
            Afectado afectado = (Afectado) usuario;
            if (afectado.getNecesidades() != null && !afectado.getNecesidades().isEmpty()) {
                // Convertimos las necesidades del usuario a strings para marcarlas en el checkbox
                Set<String> necesidadesSeleccionadas = new HashSet<>();
                for (Necesidad necesidad : afectado.getNecesidades()) {
                    if (necesidad.getTipoNecesidad() != null) {
                        necesidadesSeleccionadas.add(necesidad.getTipoNecesidad().toString());
                    }
                }
                // Establecer las necesidades seleccionadas en el grupo de checkboxes
                if (!necesidadesSeleccionadas.isEmpty()) {
                    necesidadesGroup.setValue(necesidadesSeleccionadas);
                }
            }
        }
        
        necesidadesGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        return necesidadesGroup;
    }

    private Component crearListaNecesidades() {
        VerticalLayout lista = new VerticalLayout();
        lista.setWidthFull();
        lista.setSpacing(true);
        lista.setPadding(true);
        
        // Estilo mejorado para la lista de necesidades
        lista.getStyle()
            .set("background-color", "rgba(236, 240, 243, 0.5)")
            .set("border-radius", "8px")
            .set("margin-top", "1em")
            .set("padding", "1em");
        
        // Título de la sección con estilo mejorado
        H1 titulo = new H1("Lista de Necesidades Actuales");
        titulo.getStyle()
            .set("color", "#2c3e50")
            .set("font-size", "1.2em")
            .set("margin", "0 0 0.5em 0")
            .set("font-weight", "600");
        lista.add(titulo);
        
        // Si el usuario es un afectado, mostramos sus necesidades actuales
        if (usuario instanceof Afectado) {
            Afectado afectado = (Afectado) usuario;
            if (afectado.getNecesidades() != null && !afectado.getNecesidades().isEmpty()) {
                // Creamos una lista con las necesidades actuales
                for (Necesidad necesidad : afectado.getNecesidades()) {
                    if (necesidad.getTipoNecesidad() != null) {
                        String descripcionAdicional = necesidad.getDescripcion() != null && !necesidad.getDescripcion().isEmpty() 
                            ? " - " + necesidad.getDescripcion() : "";
                        String urgenciaInfo = necesidad.getUrgencia() != null ? " (Urgencia: " + necesidad.getUrgencia() + ")" : "";
                        
                        Div itemDiv = new Div();
                        itemDiv.setText(necesidad.getTipoNecesidad().toString() + descripcionAdicional + urgenciaInfo);
                        itemDiv.getStyle()
                            .set("padding", "0.5em 1em")
                            .set("margin-bottom", "0.5em")
                            .set("background-color", "rgba(255, 255, 255, 0.7)")
                            .set("border-radius", "4px")
                            .set("border-left", "3px solid #3498db");
                        lista.add(itemDiv);
                    }
                }
            } else {
                Div mensajeDiv = new Div();
                mensajeDiv.setText("No tienes necesidades registradas actualmente.");
                mensajeDiv.getStyle()
                    .set("padding", "1em")
                    .set("color", "#7f8c8d")
                    .set("font-style", "italic");
                lista.add(mensajeDiv);
            }
        } else {
            Div mensajeDiv = new Div();
            mensajeDiv.setText("No disponible para este tipo de usuario.");
            mensajeDiv.getStyle()
                .set("padding", "1em")
                .set("color", "#7f8c8d")
                .set("font-style", "italic");
            lista.add(mensajeDiv);
        }
        
        return lista;
    }

    // Métodos comunes

    private Component crearAvatar() {
        // Contenedor para centrar el avatar
        VerticalLayout avatarContainer = new VerticalLayout();
        avatarContainer.setAlignItems(Alignment.CENTER);
        avatarContainer.setPadding(true);
        avatarContainer.setSpacing(true);
        avatarContainer.setWidthFull();
        
        Avatar avatar = new Avatar();
        // Se asigna la imagen del usuario recuperada vía endpoint con timestamp para evitar caché
        avatar.setImage("/api/usuarios/" + usuario.getId() + "/foto?t=" + System.currentTimeMillis());
        avatar.setName(usuario.getNombre() + " " + usuario.getApellidos());
        avatar.setHeight("120px");
        avatar.setWidth("120px");
        avatar.getStyle().set("margin", "0 auto");
        
        // Nombre del usuario debajo del avatar
        H1 nombreUsuario = new H1(usuario.getNombre() + " " + usuario.getApellidos());
        nombreUsuario.getStyle()
            .set("color", "#2c3e50")
            .set("font-size", "1.5em")
            .set("margin", "0.5em 0")
            .set("font-weight", "600");
        
        // Componente para subir nueva foto
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFileSize(5 * 1024 * 1024); // 5MB
        upload.setDropLabel(new Span("Sube o arrastra tu nueva foto de perfil aquí"));
        upload.setUploadButton(new Button("Cambiar foto"));
        
        // Estilo para el componente de upload
        upload.getStyle()
            .set("border", "2px dashed #3498db")
            .set("border-radius", "8px")
            .set("padding", "1em")
            .set("background-color", "rgba(52, 152, 219, 0.05)")
            .set("max-width", "300px")
            .set("margin", "1em auto");
        
        // Botón para eliminar la foto actual
        Button eliminarFotoBtn = new Button("Eliminar foto");
        eliminarFotoBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        eliminarFotoBtn.getStyle()
            .set("margin-top", "0.5em");
        
        // Manejador para subir nueva foto
        upload.addSucceededListener(event -> {
            try {
                // Convertir InputStream a byte[]
                InputStream inputStream = buffer.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] bufferBytes = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(bufferBytes)) != -1) {
                    outputStream.write(bufferBytes, 0, bytesRead);
                }
                byte[] fotoBytes = outputStream.toByteArray();
                
                // Enviar la foto al servidor
                RestTemplate restTemplate = new RestTemplate();
                String url = "/api/usuarios/" + usuario.getId() + "/foto";
                
                // Crear MultiValueMap para enviar la foto
                org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
                body.add("foto", new org.springframework.core.io.ByteArrayResource(fotoBytes) {
                    @Override
                    public String getFilename() {
                        return "profile.jpg";
                    }
                });
                
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
                
                org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = 
                    new org.springframework.http.HttpEntity<>(body, headers);
                
                restTemplate.postForEntity(url, requestEntity, String.class);
                
                // Actualizar la imagen del avatar con un timestamp para evitar caché
                String timestamp = "?t=" + System.currentTimeMillis();
                avatar.setImage("/api/usuarios/" + usuario.getId() + "/foto" + timestamp);
                
                // Forzar actualización del componente
                avatar.getElement().executeJs("this.src = this.src.split('?')[0] + '" + timestamp + "';");
                
                Notification.show("Foto actualizada con éxito", 3000, Notification.Position.BOTTOM_CENTER);
            } catch (Exception ex) {
                Notification.show("Error al procesar la imagen: " + ex.getMessage(),
                        3000, Notification.Position.MIDDLE);
            }
        });
        
        // Manejador para eliminar la foto
        eliminarFotoBtn.addClickListener(e -> {
            try {
                // Enviar solicitud para eliminar la foto (establecer a la imagen por defecto)
                RestTemplate restTemplate = new RestTemplate();
                
                // Usar directamente la ruta relativa para la API
                String url = "/api/usuarios/" + usuario.getId() + "/foto/eliminar";
                
                // Realizar una solicitud DELETE para eliminar la foto
                restTemplate.delete(url);
                
                // Actualizar la imagen del avatar (forzar recarga con timestamp)
                String timestamp = "?t=" + System.currentTimeMillis();
                avatar.setImage("/api/usuarios/" + usuario.getId() + "/foto" + timestamp);
                
                // Forzar actualización del componente
                avatar.getElement().executeJs("this.src = this.src.split('?')[0] + '" + timestamp + "';");
                
                Notification.show("Foto eliminada", 3000, Notification.Position.BOTTOM_CENTER);
            } catch (Exception ex) {
                Notification.show("Error al eliminar la foto: " + ex.getMessage(),
                        3000, Notification.Position.MIDDLE);
            }
        });
        
        avatarContainer.add(avatar, nombreUsuario, upload, eliminarFotoBtn);
        return avatarContainer;
    }

    private Component crearFormInfo() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.getStyle().set("margin-top", "1em");

        // Inicializar y asignar los valores actuales del usuario
        nombreField = new TextField("Nombre:");
        nombreField.setValue(usuario.getNombre() != null ? usuario.getNombre() : "");

        apellidosField = new TextField("Apellidos:");
        apellidosField.setValue(usuario.getApellidos() != null ? usuario.getApellidos() : "");

        emailField = new TextField("Email:");
        emailField.setValue(usuario.getEmail() != null ? usuario.getEmail() : "");

        passwordField = new PasswordField("Contraseña:");
        passwordField.setValue(usuario.getPassword() != null ? usuario.getPassword() : "");

        // Aplicar estilos consistentes a todos los campos
        Stream.of(nombreField, apellidosField, emailField, passwordField)
            .forEach(field -> {
                field.setWidthFull();
                field.getStyle()
                    .set("border-radius", "6px")
                    .set("--lumo-contrast-10pct", "rgba(44, 62, 80, 0.1)")
                    .set("--lumo-primary-color", "#3498db")
                    .set("margin-bottom", "1em");
            });

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        formLayout.add(nombreField, apellidosField, emailField, passwordField);
        return formLayout;
    }

    private Component crearGuardarInfoBtn() {
        Button guardarBtn = new Button("Guardar Cambios");
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardarBtn.getStyle()
                .set("background-color", "#3498db")
                .set("color", "white")
                .set("border-radius", "6px")
                .set("font-weight", "600")
                .set("box-shadow", "0 4px 6px rgba(52, 152, 219, 0.2)")
                .set("transition", "transform 0.1s ease-in-out")
                .set("padding", "0.5rem 1.5rem");
        guardarBtn.addClickListener(event -> {
            try {
                // Actualizar el objeto usuario con los datos del formulario
                usuario.setNombre(nombreField.getValue());
                usuario.setApellidos(apellidosField.getValue());
                usuario.setEmail(emailField.getValue());
                usuario.setPassword(passwordField.getValue());
                
                // Si el usuario es un voluntario, actualizar sus días, turno y habilidades disponibles
                if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().equalsIgnoreCase("voluntario") && 
                    diasDisponiblesGroup != null && turnoDisponibilidadCombo != null && habilidadesGroup != null) {
                    try {
                        // Intentamos hacer un cast a Voluntario
                        SolidarityHub.models.Voluntario voluntario = (SolidarityHub.models.Voluntario) usuario;
                        
                        // Obtener los días seleccionados del CheckboxGroup
                        Set<String> diasSeleccionados = diasDisponiblesGroup.getValue();
                        
                        // Actualizar los días disponibles del voluntario
                        if (voluntario.getDiasDisponibles() == null) {
                            voluntario.setDiasDisponibles(new ArrayList<>());
                        } else {
                            voluntario.getDiasDisponibles().clear();
                        }
                        
                        // Agregar los días seleccionados a la lista del voluntario
                        if (diasSeleccionados != null && !diasSeleccionados.isEmpty()) {
                            voluntario.getDiasDisponibles().addAll(diasSeleccionados);
                        }
                        
                        // Actualizar el turno de disponibilidad
                        String turnoSeleccionado = turnoDisponibilidadCombo.getValue();
                        if (turnoSeleccionado != null && !turnoSeleccionado.isEmpty()) {
                            voluntario.setTurnoDisponibilidad(turnoSeleccionado);
                        }
                        
                        // Actualizar las habilidades del voluntario
                        Set<Habilidad> habilidadesSeleccionadas = habilidadesGroup.getValue();
                        
                        // Limpiar la lista actual de habilidades
                        if (voluntario.getHabilidades() == null) {
                            voluntario.setHabilidades(new ArrayList<>());
                        } else {
                            voluntario.getHabilidades().clear();
                        }
                        
                        // Agregar las habilidades seleccionadas a la lista del voluntario
                        if (habilidadesSeleccionadas != null && !habilidadesSeleccionadas.isEmpty()) {
                            voluntario.getHabilidades().addAll(habilidadesSeleccionadas);
                        }
                    } catch (ClassCastException e) {
                        // Manejar el caso en que el usuario no sea un Voluntario
                        Notification.show("Error al actualizar los datos del voluntario: " + e.getMessage(), 
                            3000, Notification.Position.BOTTOM_CENTER);
                    }
                }
                
                // Si el usuario es un afectado, actualizar sus necesidades
                if (usuario instanceof Afectado && necesidadesGroup != null) {
                    Afectado afectado = (Afectado) usuario;
                    
                    // Obtener las necesidades seleccionadas del CheckboxGroup
                    Set<String> necesidadesSeleccionadas = necesidadesGroup.getValue();
                    
                    // Limpiar la lista actual de necesidades
                    if (afectado.getNecesidades() == null) {
                        afectado.setNecesidades(new ArrayList<>());
                    } else {
                        afectado.getNecesidades().clear();
                    }
                    
                    // Crear nuevas necesidades basadas en las selecciones
                    for (String tipoNecesidadStr : necesidadesSeleccionadas) {
                        try {
                            // Convertir el string al enum TipoNecesidad
                            Necesidad.TipoNecesidad tipoNecesidad = Necesidad.TipoNecesidad.valueOf(tipoNecesidadStr);
                            
                            // Crear una nueva necesidad con valores predeterminados
                            Necesidad nuevaNecesidad = new Necesidad(
                                tipoNecesidad,
                                "Necesidad de " + tipoNecesidadStr,
                                Necesidad.EstadoNecesidad.REGISTRADA,
                                Necesidad.Urgencia.MEDIA,
                                afectado.getDireccion(), // Usar la dirección del afectado
                                LocalDateTime.now() // Fecha actual
                            );
                            
                            // Agregar la necesidad a la lista del afectado
                            afectado.getNecesidades().add(nuevaNecesidad);
                        } catch (IllegalArgumentException e) {
                            // Manejar el caso en que el string no coincida con ningún valor del enum
                            Notification.show("Error al procesar la necesidad: " + tipoNecesidadStr, 
                                3000, Notification.Position.BOTTOM_CENTER);
                        }
                    }
                }
                
                // Usar el servicio directamente en lugar de la API REST
                Usuario usuarioActualizado = usuarioServicio.guardarUsuario(usuario);
                
                // Actualizar el usuario en la sesión
                VaadinSession.getCurrent().setAttribute("usuario", usuarioActualizado);
                
                Notification.show("Cambios guardados con éxito.", 3000, Notification.Position.BOTTOM_CENTER);
                UI.getCurrent().navigate("main");
            } catch (Exception e) {
                Notification.show("Error al guardar los cambios: " + e.getMessage(), 
                    3000, Notification.Position.BOTTOM_CENTER);
            }
        });
        return guardarBtn;
    }

    private Component crearCancelarBtn() {
        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelarBtn.getStyle()
                .set("font-weight", "600")
                .set("border-radius", "6px")
                .set("padding", "0.5rem 1.5rem");
        cancelarBtn.addClickListener(event -> UI.getCurrent().navigate("main"));
        return cancelarBtn;
    }
}
