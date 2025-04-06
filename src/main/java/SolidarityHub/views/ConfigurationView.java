package SolidarityHub.views;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import SolidarityHub.models.Habilidad;
import SolidarityHub.models.Usuario;
import SolidarityHub.services.UsuarioServicio;

@Route(value = "configuracion", layout = MainLayout.class)
public class ConfigurationView extends VerticalLayout {

    private final UsuarioServicio usuarioServicio;

    private Div voluntarioInfo = new Div();

    boolean isVoluntario;

    private Usuario usuario;

    public ConfigurationView(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;

        // Usuario usuario = (Usuario)
        // UI.getCurrent().getSession().getAttribute("name");
        // usuario = usuarioServicio.obtenerUsuarioPorId(usuario.getId());

        // Configuración general de la vista
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        // setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        // Título de la página
        H1 title = new H1("Configuración de Usuario");
        title.getStyle().set("color", "var(--lumo-primary-color)").set("padding", "1rem").set("font-size", "2rem")
                .set("font-weight", "bold").set("text-align", "center");
        title.getStyle().set("margin-top", "1rem").set("margin-bottom", "1rem");

        // Panel contenedor para separar el contenido en dos columnas
        HorizontalLayout panel = new HorizontalLayout();
        panel.setSizeFull();
        panel.setWidth("80%");
        panel.setHeight("auto");
        panel.setSpacing(true);
        panel.setPadding(true);
        panel.setAlignItems(Alignment.CENTER);
        // panel.setJustifyContentMode(JustifyContentMode.CENTER);

        // Panel izquierdo: Horarios y lista de voluntariados
        VerticalLayout panelIzq = new VerticalLayout();
        panelIzq.setSizeFull();
        panelIzq.setWidth("100%");
        panelIzq.setSpacing(true);
        panelIzq.setPadding(true);
        panelIzq.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "1rem");
        // panelIzq.add(crearVoluntarioInfo());
        panelIzq.add(crearDiasHorario(), crearTurnoHorario(), crearListaVoluntariados());

        // Panel derecho: Avatar y formulario de información
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
                .set("background-color", "var(--lumo-base-color)");
        panelBtns.getStyle().set("margin-top", "1rem");
        panelBtns.add(crearGuardarInfoBtn(), crearCancelarBtn());

        panel.add(panelIzq, panelDer);

        formCard.add(panel);

        // Se agregan los componentes principales a la vista
        add(title, panel, panelBtns);

        // configureVisibility();
    }

    private Component crearDiasHorario() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setLabel("Días de la semana disponible:");
        checkboxGroup.setItems("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo");
        checkboxGroup.select("Order ID", "Customer");
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

    private Component crearAvatar() {
        Avatar user = new Avatar();
        // Se puede reemplazar la imagen por la del usuario
        user.setImage("https://via.placeholder.com/150");
        user.setName("Nombre de Usuario");
        return user;
    }

    private Component crearListaVoluntariados() {
        VerticalLayout lista = new VerticalLayout();
        lista.setWidthFull();
        lista.setSpacing(true);
        lista.getStyle().set("padding", "0.5rem");
        // Placeholder para lista de voluntariados, luego se puede integrar el AutoGrid
        lista.add("Lista de Voluntariados: ");
        return lista;
    }

    private Component crearFormInfo() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();

        TextField nombreField = new TextField("Nombre:");
        // nombreField.setValue(usuario.getNombre());

        TextField apellidosField = new TextField("Apellidos:");
        // apellidosField.setValue(usuario.getApellidos());

        TextField emailField = new TextField("Email:");
        // emailField.setValue(usuario.getEmail());

        PasswordField passwordField = new PasswordField("Contraseña:");
        // passwordField.setValue(usuario.getPassword());

        // Configuración de pasos responsivos para mejorar la disposición en diferentes
        // anchos
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

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
            // usuarioServicio.guardarUsuario(usuario); // Guardar cambios en el usuario
            Notification.show("Cambios guardados con éxito.");
            UI.getCurrent().navigate("main"); // Navegar a la vista principal
        });
        return guardarBtn;
    }

    private Component crearCancelarBtn() {
        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addClickListener(event -> {
            UI.getCurrent().navigate("main"); // Navegar a la vista principal
        });
        return cancelarBtn;
    }

    private void configureVisibility() {
        isVoluntario = "Voluntario".equals(usuario.getTipoUsuario());
        voluntarioInfo.setVisible(isVoluntario);
    }

    private Component crearVoluntarioInfo() {
        Div camposVoluntario = new Div();
        camposVoluntario.setWidth("100%");

        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setMaxWidth("700px");

        HorizontalLayout hlLayout = new HorizontalLayout();
        Component dias = crearDiasHorario();
        Component turno = crearTurnoHorario();
        hlLayout.add(dias, turno);
        hlLayout.setAlignItems(Alignment.CENTER);
        vLayout.add(hlLayout, crearListaVoluntariados());
        camposVoluntario.add(vLayout);
        return camposVoluntario;
    }
}
