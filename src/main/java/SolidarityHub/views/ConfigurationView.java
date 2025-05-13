package SolidarityHub.views;

import SolidarityHub.models.*;
import SolidarityHub.repository.TareaRepositorio;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

import SolidarityHub.services.UsuarioServicio;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Stream;

@Route(value = "configuracion", layout = MainLayout.class)
@PageTitle("Configuración | SolidarityHub")
public class ConfigurationView extends VerticalLayout {

    private final UsuarioServicio usuarioServicio;
    private final TareaRepositorio tareaRepositorio;
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

    // Ya no necesitamos campos para afectados, se gestionan en otra vista

    public ConfigurationView(UsuarioServicio usuarioServicio, TareaRepositorio tareaRepositorio) {
        this.usuarioServicio = usuarioServicio;
        this.tareaRepositorio = tareaRepositorio;
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
        setHeight("auto");
        setWidth("auto");

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
                        "this.parentNode.style.background = 'white';");

        // Centrar todo el contenido
        getStyle().set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center");

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
                .set("margin", "2em auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("margin-top", "45em")
                .set("margin-bottom", "5em");
        formCard.setHeight("auto");

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
        panel.setAlignItems(Alignment.CENTER);
        panel.setJustifyContentMode(JustifyContentMode.CENTER);
        panel.getStyle().set("margin", "0 auto");
        panel.setHeight("auto");

        // Panel izquierdo: se muestra según el tipo de usuario
        VerticalLayout panelIzq = new VerticalLayout();
        panelIzq.setWidth("48%");
        panelIzq.setSpacing(true);
        panelIzq.setPadding(true);
        panelIzq.setAlignItems(Alignment.CENTER);
        panelIzq.getStyle()
                .set("background-color", "rgba(52, 152, 219, 0.05)")
                .set("border-radius", "8px")
                .set("padding", "1.5rem")
                .set("margin", "0 auto");
        panelIzq.setHeight("auto");

        // Si el usuario es voluntario, se muestran horarios y habilidades
        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().equalsIgnoreCase("voluntario")) {
            panelIzq.add(crearDiasHorario(), crearTurnoHorario(), crearHabilidades());
        }
        // Para usuarios afectados, mostramos información sobre la gestión de
        // necesidades
        else if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().equalsIgnoreCase("afectado")) {
            panelIzq.add(crearInfoNecesidades());
        }

        // Panel derecho: Avatar y formulario de datos personales
        VerticalLayout panelDer = new VerticalLayout();
        panelDer.setWidth("48%");
        panelDer.setSpacing(true);
        panelDer.setPadding(true);
        panelDer.setAlignItems(Alignment.CENTER);
        panelDer.getStyle()
                .set("background-color", "rgba(236, 240, 243, 0.5)")
                .set("border-radius", "8px")
                .set("padding", "1.5rem")
                .set("margin", "0 auto");
        panelDer.setHeight("auto");
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
        panelBtns.setHeight("auto");

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

        // Si el usuario es un voluntario, seleccionamos el turno que ya tiene
        // configurado
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

    // Métodos comunes

    // Métodos comunes

    private Component crearAvatar() {
        // Contenedor para centrar el avatar
        VerticalLayout avatarContainer = new VerticalLayout();
        avatarContainer.setAlignItems(Alignment.CENTER);
        avatarContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        avatarContainer.setPadding(true);
        avatarContainer.setSpacing(true);
        avatarContainer.setWidthFull();
        avatarContainer.getStyle().set("text-align", "center");

        Avatar avatar = new Avatar();
        // Se asigna la imagen del usuario recuperada vía endpoint con timestamp para
        // evitar caché
        avatar.setImage("/api/usuarios/" + usuario.getId() + "/foto?t=" + System.currentTimeMillis());
        avatar.setName(usuario.getNombre() + " " + usuario.getApellidos());
        avatar.setHeight("120px");
        avatar.setWidth("120px");
        avatar.getStyle()
                .set("margin", "0 auto")
                .set("display", "block");

        // Nombre del usuario debajo del avatar
        H1 nombreUsuario = new H1(usuario.getNombre() + " " + usuario.getApellidos());
        nombreUsuario.getStyle()
                .set("color", "#2c3e50")
                .set("font-size", "1.5em")
                .set("margin", "0.5em auto")
                .set("font-weight", "600")
                .set("text-align", "center");

        // Información del tipo de usuario
        Div tipoUsuarioDiv = new Div();
        tipoUsuarioDiv.setText("Tipo de usuario: " + usuario.getTipoUsuario());
        tipoUsuarioDiv.getStyle()
                .set("color", "#7f8c8d")
                .set("font-size", "1em")
                .set("margin", "0.5em auto 1.5em auto")
                .set("font-style", "italic")
                .set("text-align", "center");

        avatarContainer.add(avatar, nombreUsuario, tipoUsuarioDiv);
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

        // Reemplazar el campo de contraseña por un botón
        Button cambiarPasswordBtn = new Button("Cambiar Contraseña");
        cambiarPasswordBtn.getStyle()
                .set("margin-top", "1em")
                .set("background-color", "#3498db")
                .set("color", "white")
                .set("border-radius", "6px")
                .set("font-weight", "500")
                .set("box-shadow", "0 2px 4px rgba(52, 152, 219, 0.2)");
        
        cambiarPasswordBtn.addClickListener(event -> abrirDialogoCambioPassword());

        // Aplicar estilos consistentes a todos los campos
        Stream.of(nombreField, apellidosField, emailField)
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
                new FormLayout.ResponsiveStep("500px", 2));

        formLayout.add(nombreField, apellidosField, emailField, cambiarPasswordBtn);
        return formLayout;
    }

    // Método para abrir el diálogo de cambio de contraseña
    private void abrirDialogoCambioPassword() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setWidth("400px");
        
        // Título del diálogo
        H1 titulo = new H1("Cambiar Contraseña");
        titulo.getStyle()
                .set("color", "#2c3e50")
                .set("font-size", "1.5em")
                .set("margin", "0.5em 0")
                .set("font-weight", "600")
                .set("text-align", "center");
        
        // Campos para la nueva contraseña
        PasswordField nuevaPasswordField = new PasswordField("Nueva Contraseña");
        nuevaPasswordField.setWidthFull();
        nuevaPasswordField.getStyle()
                .set("border-radius", "6px")
                .set("--lumo-contrast-10pct", "rgba(44, 62, 80, 0.1)")
                .set("--lumo-primary-color", "#3498db")
                .set("margin-bottom", "1em");
        
        PasswordField confirmarPasswordField = new PasswordField("Confirmar Contraseña");
        confirmarPasswordField.setWidthFull();
        confirmarPasswordField.getStyle()
                .set("border-radius", "6px")
                .set("--lumo-contrast-10pct", "rgba(44, 62, 80, 0.1)")
                .set("--lumo-primary-color", "#3498db")
                .set("margin-bottom", "1em");
        
        // Botones de acción
        Button aceptarBtn = new Button("Aceptar", e -> {
            String nuevaPassword = nuevaPasswordField.getValue();
            String confirmarPassword = confirmarPasswordField.getValue();
            
            // Verificar que las contraseñas coincidan
            if (nuevaPassword == null || nuevaPassword.isEmpty()) {
                Notification.show("La contraseña no puede estar vacía", 
                        3000, Notification.Position.MIDDLE);
                return;
            }
            
            if (!nuevaPassword.equals(confirmarPassword)) {
                Notification.show("Las contraseñas no coinciden", 
                        3000, Notification.Position.MIDDLE);
                return;
            }
            
            try {
                // Actualizar la contraseña del usuario localmente
                usuario.setPassword(nuevaPassword);
                
                // Guardar la contraseña en la base de datos a través del servicio
                usuarioServicio.guardarUsuario(usuario);
                
                // Actualizar la sesión con el usuario actualizado
                VaadinSession.getCurrent().setAttribute("usuario", usuario);
                
                Notification.show("Contraseña actualizada correctamente", 
                        3000, Notification.Position.BOTTOM_CENTER);
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Error al actualizar la contraseña: " + ex.getMessage(), 
                        3000, Notification.Position.MIDDLE);
            }
        });
        aceptarBtn.getStyle()
                .set("background-color", "#3498db")
                .set("color", "white")
                .set("border-radius", "6px")
                .set("font-weight", "500");
        
        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());
        
        HorizontalLayout botonesLayout = new HorizontalLayout(aceptarBtn, cancelarBtn);
        botonesLayout.setWidthFull();
        botonesLayout.setJustifyContentMode(JustifyContentMode.END);
        botonesLayout.setSpacing(true);
        
        // Añadir componentes al diálogo
        VerticalLayout dialogLayout = new VerticalLayout(titulo, nuevaPasswordField, confirmarPasswordField, botonesLayout);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(Alignment.CENTER);
        
        dialog.add(dialogLayout);
        dialog.open();
    }
    
    private Component crearGuardarInfoBtn() {
        Button guardarBtn = new Button("Guardar Cambios");
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardarBtn.getStyle()
                .set("border-radius", "6px")
                .set("font-weight", "600")
                .set("box-shadow", "0 4px 6px rgba(52, 152, 219, 0.2)")
                .set("transition", "transform 0.1s ease-in-out")
                .set("padding", "0.5rem 1.5rem");
        guardarBtn.getElement().getThemeList().add("primary");
        guardarBtn.addClickShortcut(com.vaadin.flow.component.Key.ENTER);
        guardarBtn.addClickListener(event -> {
            try {
                // Actualizar el objeto usuario con los datos del formulario
                usuario.setNombre(nombreField.getValue());
                usuario.setApellidos(apellidosField.getValue());
                usuario.setEmail(emailField.getValue());
                // Ya no actualizamos la contraseña aquí, se hace en el diálogo
                
                // Si el usuario es un voluntario, actualizar sus días, turno y habilidades
                // disponibles
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
                            dessuscribirVoluntario(voluntario);
                            voluntario.getHabilidades().addAll(habilidadesSeleccionadas);
                            suscribirVoluntario(voluntario);
                        }
                    } catch (ClassCastException e) {
                        // Manejar el caso en que el usuario no sea un Voluntario
                        Notification.show("Error al actualizar los datos del voluntario: " + e.getMessage(),
                                3000, Notification.Position.BOTTOM_CENTER);
                    }
                }

                // Ya no actualizamos las necesidades aquí, se gestionan en la ventana
                // específica de necesidades

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

    private void dessuscribirVoluntario(Voluntario voluntario){
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/tareas/";

        Map<Necesidad.TipoNecesidad, Habilidad> mapeoHabilidades = new HashMap<>();
        mapeoHabilidades.put(Necesidad.TipoNecesidad.PRIMEROS_AUXILIOS, Habilidad.PRIMEROS_AUXILIOS);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.ALIMENTACION, Habilidad.COCINA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.ALIMENTACION_BEBE, Habilidad.COCINA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.SERVICIO_LIMPIEZA, Habilidad.LIMPIEZA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_PSICOLOGICA, Habilidad.AYUDA_PSICOLOGICA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_CARPINTERIA, Habilidad.CARPINTERIA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_ELECTRICIDAD, Habilidad.ELECTICISTA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_FONTANERIA, Habilidad.FONTANERIA);

        List<Tarea> listaTareas = tareaRepositorio.findAll();
        for (Tarea tarea : listaTareas) {
            Habilidad habilidadRequerida = mapeoHabilidades.get(tarea.getTipo());
            if (voluntario.getHabilidades().contains(habilidadRequerida)) {
                restTemplate.postForEntity(url + tarea.getId() + "/dessuscribir/" + voluntario.getId(), null, Void.class);
            }
        }
    }

    private void suscribirVoluntario(Voluntario voluntario) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/tareas/";

        Map<Necesidad.TipoNecesidad, Habilidad> mapeoHabilidades = new HashMap<>();
        mapeoHabilidades.put(Necesidad.TipoNecesidad.PRIMEROS_AUXILIOS, Habilidad.PRIMEROS_AUXILIOS);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.ALIMENTACION, Habilidad.COCINA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.ALIMENTACION_BEBE, Habilidad.COCINA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.SERVICIO_LIMPIEZA, Habilidad.LIMPIEZA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_PSICOLOGICA, Habilidad.AYUDA_PSICOLOGICA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_CARPINTERIA, Habilidad.CARPINTERIA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_ELECTRICIDAD, Habilidad.ELECTICISTA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_FONTANERIA, Habilidad.FONTANERIA);

        List<Tarea> listaTareas = tareaRepositorio.findAll();
        for (Tarea tarea : listaTareas) {
            Habilidad habilidadRequerida = mapeoHabilidades.get(tarea.getTipo());
            if (voluntario.getHabilidades().contains(habilidadRequerida)) {
                restTemplate.postForEntity(url + tarea.getId() + "/suscribir/" + voluntario.getId(), null, Void.class);
            }
        }
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

    // Método para crear el panel informativo sobre la gestión de necesidades
    private Component crearInfoNecesidades() {
        // Contenedor principal con estilo mejorado
        Div infoContainer = new Div();
        infoContainer.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("padding", "2rem")
                .set("width", "100%")
                .set("border", "1px solid rgba(52, 152, 219, 0.15)")
                .set("transition", "all 0.3s ease")
                .set("overflow", "hidden");

        // Título del panel con estilo mejorado
        H1 title = new H1("Gestión de Necesidades");
        title.getStyle()
                .set("color", "#2c3e50")
                .set("font-size", "1.8em")
                .set("margin-top", "0.5rem")
                .set("margin-bottom", "1.2rem")
                .set("font-weight", "700")
                .set("text-align", "center")
                .set("letter-spacing", "0.5px");

        // Contenedor para la línea vertical y el mensaje (lado a lado)
        HorizontalLayout contenidoLayout = new HorizontalLayout();
        contenidoLayout.setWidthFull();
        contenidoLayout.setSpacing(true);
        contenidoLayout.setPadding(false);
        contenidoLayout.setAlignItems(Alignment.CENTER);

        // Línea vertical decorativa mejorada (ahora a la izquierda del texto)
        Div lineaVertical = new Div();
        lineaVertical.getStyle()
                .set("width", "4px")
                .set("background", "linear-gradient(to bottom, #3498db, #2980b9)")
                .set("height", "70px")
                .set("border-radius", "4px")
                .set("flex-shrink", "0");

        // Mensaje informativo con estilo mejorado
        Div mensaje = new Div();
        mensaje.setText(
                "La gestión de necesidades ahora se realiza en una ventana específica. Por favor, utilice la opción 'Necesidades' en el menú principal para gestionar sus necesidades.");
        mensaje.getStyle()
                .set("color", "#34495e")
                .set("font-size", "1.05em")
                .set("line-height", "1.6")
                .set("margin-left", "1rem")
                .set("text-align", "center")
                .set("font-weight", "400");

        // Añadir línea vertical y mensaje al contenedor horizontal
        contenidoLayout.add(lineaVertical, mensaje);

        // Botón para ir a la vista de necesidades con estilo mejorado
        Button irANecesidadesBtn = new Button("Ir a Necesidades");
        irANecesidadesBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        irANecesidadesBtn.getStyle()
                .set("background", "linear-gradient(135deg, #3498db, #2980b9)")
                .set("color", "white")
                .set("border-radius", "30px")
                .set("font-weight", "600")
                .set("box-shadow", "0 4px 10px rgba(52, 152, 219, 0.3)")
                .set("transition", "all 0.2s ease-in-out")
                .set("padding", "0.7rem 2rem")
                .set("margin-top", "1.5rem")
                .set("border", "none")
                .set("cursor", "pointer");

        // Efecto hover para el botón
        irANecesidadesBtn.getElement().addEventListener("mouseover", event -> irANecesidadesBtn.getStyle()
                .set("transform", "translateY(-3px)")
                .set("box-shadow", "0 6px 12px rgba(52, 152, 219, 0.4)"));

        irANecesidadesBtn.getElement().addEventListener("mouseout", event -> irANecesidadesBtn.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 4px 10px rgba(52, 152, 219, 0.3)"));

        // Añadir evento de clic para navegar a la vista de necesidades
        irANecesidadesBtn.addClickListener(event -> UI.getCurrent().navigate("necesidades"));

        // Contenedor para centrar el botón
        Div buttonContainer = new Div(irANecesidadesBtn);
        buttonContainer.getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("width", "100%")
                .set("margin-top", "1rem");

        // Añadir componentes al contenedor principal
        infoContainer.add(title, contenidoLayout, buttonContainer);

        return infoContainer;
    }
}
