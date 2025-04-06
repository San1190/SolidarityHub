package SolidarityHub.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import SolidarityHub.models.Usuario;
import SolidarityHub.services.UsuarioServicio;

@Route(value = "configuracion", layout = MainLayout.class)
public class ConfigurationView extends VerticalLayout {

    private final UsuarioServicio usuarioServicio;
    private Usuario usuario;

    // Campos del formulario de datos personales (comunes)
    private TextField nombreField;
    private TextField apellidosField;
    private TextField emailField;
    private PasswordField passwordField;

    // Campos para voluntarios (habilidades)
    private TextField habilidadField;

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
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Título de la página
        H1 title = new H1("Configuración de Usuario");
        title.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("padding", "1rem")
                .set("font-size", "2rem")
                .set("font-weight", "bold")
                .set("text-align", "center")
                .set("margin-top", "1rem")
                .set("margin-bottom", "1rem");

        // Panel contenedor para separar el contenido en dos columnas
        HorizontalLayout panel = new HorizontalLayout();
        panel.setSizeFull();
        panel.setWidth("80%");
        panel.setHeight("auto");
        panel.setSpacing(true);
        panel.setPadding(true);
        panel.setAlignItems(Alignment.CENTER);

        // Panel izquierdo: se muestra según el tipo de usuario
        VerticalLayout panelIzq = new VerticalLayout();
        panelIzq.setSizeFull();
        panelIzq.setWidth("100%");
        panelIzq.setSpacing(true);
        panelIzq.setPadding(true);
        panelIzq.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "1rem");

        // Si el usuario es voluntario, se muestran horarios y habilidades
        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().equalsIgnoreCase("voluntario")) {
            panelIzq.add(crearDiasHorario(), crearTurnoHorario(), crearHabilidades(), crearListaHabilidades());
        }
        // Si el usuario es afectado, se muestra la selección de necesidades
        else if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().equalsIgnoreCase("afectado")) {
            panelIzq.add(crearNecesidades(), crearListaNecesidades());
        }

        // Panel derecho: Avatar y formulario de datos personales
        VerticalLayout panelDer = new VerticalLayout();
        panelDer.setSizeFull();
        panelDer.setWidth("100%");
        panelDer.setSpacing(true);
        panelDer.setPadding(true);
        panelDer.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "1rem");
        panelDer.add(crearAvatar(), crearFormInfo());

        // Contenedor principal
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

        // Panel de botones
        HorizontalLayout panelBtns = new HorizontalLayout();
        panelBtns.setSizeFull();
        panelBtns.setWidth("80%");
        panelBtns.setSpacing(true);
        panelBtns.setPadding(true);
        panelBtns.setJustifyContentMode(JustifyContentMode.END);
        panelBtns.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "1rem")
                .set("background-color", "var(--lumo-base-color)")
                .set("margin-top", "1rem");
        panelBtns.add(crearGuardarInfoBtn(), crearCancelarBtn());

        panel.add(panelIzq, panelDer);
        formCard.add(panel);
        add(title, formCard, panelBtns);
    }

    // Métodos para usuarios voluntarios

    private Component crearDiasHorario() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setLabel("Días de la semana disponible:");
        checkboxGroup.setItems("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo");
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        return checkboxGroup;
    }

    private Component crearTurnoHorario() {
        ComboBox<String> comboBox = new ComboBox<>("Turno de disponibilidad:");
        comboBox.setItems("Mañana", "Tarde", "Día Entero");
        comboBox.setPlaceholder("Selecciona un turno");
        comboBox.setClearButtonVisible(true);
        return comboBox;
    }

    private Component crearHabilidades() {
        VerticalLayout habilidadLayout = new VerticalLayout();
        habilidadField = new TextField("Añadir Habilidad:");
        Button agregarBtn = new Button("Agregar Habilidad");
        agregarBtn.addClickListener(event -> {
            String habilidad = habilidadField.getValue();
            if (!habilidad.isEmpty()) {
                // Aquí debes implementar la lógica para agregar la habilidad al usuario.
                // Por ejemplo: usuario.agregarHabilidad(new Habilidad(habilidad));
                Notification.show("Habilidad agregada: " + habilidad);
                habilidadField.clear();
                // Puedes actualizar la lista de habilidades si es necesario.
            } else {
                Notification.show("Ingrese una habilidad.");
            }
        });
        habilidadLayout.add(habilidadField, agregarBtn);
        return habilidadLayout;
    }

    private Component crearListaHabilidades() {
        VerticalLayout lista = new VerticalLayout();
        lista.setWidthFull();
        lista.setSpacing(true);
        lista.getStyle().set("padding", "0.5rem");
        // Aquí se mostraría la lista de habilidades del usuario.
        lista.add("Lista de Habilidades: ");
        // Por ejemplo, si el usuario tiene una lista de habilidades:
        // usuario.getHabilidades().forEach(h -> lista.add(h.getNombre()));
        return lista;
    }

    // Métodos para usuarios afectados

    private Component crearNecesidades() {
        // Usamos un CheckboxGroup para que el usuario seleccione sus necesidades
        necesidadesGroup = new CheckboxGroup<>();
        necesidadesGroup.setLabel("Selecciona tus Necesidades:");
        necesidadesGroup.setItems("Alimentación", "Vestimenta", "Alojamiento", "Asesoría Legal", "Salud");
        necesidadesGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        return necesidadesGroup;
    }

    private Component crearListaNecesidades() {
        VerticalLayout lista = new VerticalLayout();
        lista.setWidthFull();
        lista.setSpacing(true);
        lista.getStyle().set("padding", "0.5rem");
        // Aquí se mostraría la lista de necesidades seleccionadas del usuario.
        lista.add("Lista de Necesidades: ");
        // Por ejemplo, si el usuario tiene una lista de necesidades:
        // usuario.getNecesidades().forEach(n -> lista.add(n));
        return lista;
    }

    // Métodos comunes

    private Component crearAvatar() {
        Avatar avatar = new Avatar();
        // Se asigna la imagen del usuario recuperada vía endpoint
        avatar.setImage("/api/usuarios/" + usuario.getId() + "/foto");
        avatar.setName(usuario.getNombre() + " " + usuario.getApellidos());
        return avatar;
    }

    private Component crearFormInfo() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();

        // Inicializar y asignar los valores actuales del usuario
        nombreField = new TextField("Nombre:");
        nombreField.setValue(usuario.getNombre() != null ? usuario.getNombre() : "");

        apellidosField = new TextField("Apellidos:");
        apellidosField.setValue(usuario.getApellidos() != null ? usuario.getApellidos() : "");

        emailField = new TextField("Email:");
        emailField.setValue(usuario.getEmail() != null ? usuario.getEmail() : "");

        passwordField = new PasswordField("Contraseña:");
        passwordField.setValue(usuario.getPassword() != null ? usuario.getPassword() : "");

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        formLayout.add(nombreField, apellidosField, emailField, passwordField);
        return formLayout;
    }

    private Component crearGuardarInfoBtn() {
        Button guardarBtn = new Button("Guardar Cambios");
        guardarBtn.getStyle()
                .set("background-color", "var(--lumo-primary-color)")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("border-radius", "4px")
                .set("padding", "0.5rem 1rem");
        guardarBtn.addClickListener(event -> {
            // Actualizar el objeto usuario con los datos del formulario
            usuario.setNombre(nombreField.getValue());
            usuario.setApellidos(apellidosField.getValue());
            usuario.setEmail(emailField.getValue());
            usuario.setPassword(passwordField.getValue());
            
            // Aquí también podrías actualizar la lista de habilidades o necesidades según lo seleccionado.
            // Por ejemplo, actualizar usuario.setHabilidades(...) o usuario.setNecesidades(...)

            // Guardar el usuario actualizado en la base de datos
            usuarioServicio.guardarUsuario(usuario);
            
            // Actualizar el usuario en la sesión para reflejar los cambios
            VaadinSession.getCurrent().setAttribute("usuario", usuario);
            
            Notification.show("Cambios guardados con éxito.");
            UI.getCurrent().navigate("main"); // Navegar a la vista principal
        });
        return guardarBtn;
    }

    private Component crearCancelarBtn() {
        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addClickListener(event -> UI.getCurrent().navigate("main"));
        return cancelarBtn;
    }
}
