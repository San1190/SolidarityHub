package SolidarityHub.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route(value = "configuracion", layout = MainLayout.class)
public class ConfigurationView extends VerticalLayout {

    public ConfigurationView() {
        // Configuración general de la vista
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        // setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        // Título de la página
        H1 title = new H1("Configuración de Usuario");
        title.getStyle().set("color", "var(--lumo-primary-color)");

        // Panel contenedor para separar el contenido en dos columnas
        VerticalLayout panel = new VerticalLayout();
        panel.setWidth("80%");
        panel.setHeight("auto");
        panel.setSpacing(true);
        panel.setPadding(true);
        panel.setAlignItems(Alignment.CENTER);
        // panel.setJustifyContentMode(JustifyContentMode.CENTER);

        // Panel izquierdo: Horarios y lista de voluntariados
        VerticalLayout panelIzq = new VerticalLayout();
        panelIzq.setWidth("100%");
        panelIzq.setSpacing(true);
        panelIzq.setPadding(true);
        panelIzq.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "1rem");
        panelIzq.add(crearDiasHorario(), crearTurnoHorario(), crearListaVoluntariados());

        // Panel derecho: Avatar y formulario de información
        VerticalLayout panelDer = new VerticalLayout();
        panelDer.setWidth("100%");
        panelDer.setSpacing(true);
        panelDer.setPadding(true);
        panelDer.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "1rem");
        panelDer.add(crearAvatar(), crearFormInfo());

        HorizontalLayout panelBtns = new HorizontalLayout();
        panelBtns.setWidth("80%");
        panelBtns.setSpacing(true);
        panelBtns.setPadding(true);
        panelBtns.setJustifyContentMode(JustifyContentMode.CENTER);
        panelBtns.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "1rem")
                .set("background-color", "var(--lumo-base-color)");
        panelBtns.getStyle().set("margin-top", "1rem");
        panelBtns.add(crearGuardarInfoBtn(), crearCancelarBtn());

        panel.add(title, panelIzq, panelDer);

        // Se agregan los componentes principales a la vista
        add(panel, panelBtns);
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
        TextField apellidosField = new TextField("Apellidos:");
        TextField emailField = new TextField("Email:");
        PasswordField passwordField = new PasswordField("Contraseña:");

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
        guardarBtn.addClickListener(event -> {
            // Lógica para guardar cambios
            Notification.show("Cambios guardados con éxito.");
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
}
