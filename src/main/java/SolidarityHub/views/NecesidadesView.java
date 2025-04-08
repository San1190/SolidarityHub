package SolidarityHub.views;

import SolidarityHub.models.Necesidad;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Necesidad.Urgencia;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import com.vaadin.flow.server.VaadinSession;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.client.RestTemplate;

@Route("necesidades")
@PageTitle("Necesidades | SolidarityHub")
public class NecesidadesView extends MainLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080/api/necesidades"; // URL base de la API REST
    private final Grid<Necesidad> grid = new Grid<>(Necesidad.class, false);
    private final Binder<Necesidad> binder = new Binder<>(Necesidad.class);

    public NecesidadesView() {
        // Layout principal
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        mainLayout.setHeight("auto");

        mainLayout.add(crearTitulo());

        // Layout para el formulario
        FormLayout formLayout = createFormLayout();
        mainLayout.add(formLayout);

        // Layout para el grid
        VerticalLayout gridLayout = createGridLayout();
        mainLayout.add(gridLayout);

        // Asignar el contenido principal al AppLayout
        setContent(mainLayout);
        refreshGrid();
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("100%");
        formLayout.setMaxWidth("800px");

        // Campos del formulario
        ComboBox<TipoNecesidad> tipoNecesidadField = new ComboBox<>("Tipo de Necesidad");
        tipoNecesidadField.setItems(TipoNecesidad.values());
        tipoNecesidadField.setItemLabelGenerator(Enum::name);
        binder.forField(tipoNecesidadField).asRequired("El tipo de necesidad es obligatorio")
                .bind(Necesidad::getTipoNecesidad, Necesidad::setTipoNecesidad);

        TextArea descripcionField = new TextArea("Descripción");
        descripcionField.setWidthFull();
        descripcionField.setHeight("100px");
        binder.forField(descripcionField).asRequired("La descripción es obligatoria").bind(Necesidad::getDescripcion,
                Necesidad::setDescripcion);

        ComboBox<Urgencia> urgenciaField = new ComboBox<>("Urgencia");
        urgenciaField.setItems(Urgencia.values());
        urgenciaField.setItemLabelGenerator(Enum::name);
        binder.forField(urgenciaField).asRequired("La urgencia es obligatoria").bind(Necesidad::getUrgencia,
                Necesidad::setUrgencia);

        TextField ubicacionField = new TextField("Ubicación");
        binder.forField(ubicacionField).asRequired("La ubicación es obligatoria").bind(Necesidad::getUbicacion,
                Necesidad::setUbicacion);

        // Botón para guardar
        Button saveButton = new Button("Guardar", event -> {
            if (binder.isValid()) {
                Necesidad nuevaNecesidad = new Necesidad();
                binder.writeBeanIfValid(nuevaNecesidad);
                nuevaNecesidad.setEstadoNecesidad(Necesidad.EstadoNecesidad.REGISTRADA);
                nuevaNecesidad.setFechaCreacion(LocalDateTime.now());

                // Guardar la necesidad a través de la API REST
                restTemplate.postForObject(apiUrl + "/crear", nuevaNecesidad, Necesidad.class);
                Notification.show("Necesidad guardada correctamente");
                refreshGrid();
                binder.readBean(new Necesidad()); // Limpiar el formulario
            } else {
                Notification.show("Por favor, complete todos los campos obligatorios");
            }
        });
        saveButton.getStyle().set("background-color", "#3498db").set("color", "white");
        HorizontalLayout botonLayout = new HorizontalLayout(saveButton);
        botonLayout.setWidthFull();
        botonLayout.setJustifyContentMode(JustifyContentMode.START);

        // Añadir campos al formulario
        formLayout.add(tipoNecesidadField, descripcionField, urgenciaField, ubicacionField, botonLayout);
        return formLayout;
    }

    private VerticalLayout createGridLayout() {
        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setSizeFull();

        // Configurar el grid
        grid.addColumn(necesidad -> necesidad.getTipoNecesidad().name()).setHeader("Tipo de Necesidad");
        grid.addColumn(Necesidad::getDescripcion).setHeader("Descripción");
        grid.addColumn(necesidad -> necesidad.getUrgencia().name()).setHeader("Urgencia");
        grid.addColumn(Necesidad::getUbicacion).setHeader("Ubicación");
        grid.addColumn(necesidad -> necesidad.getFechaCreacion().toString()).setHeader("Fecha de Creación");

        gridLayout.add(grid);
        return gridLayout;
    }

    private void refreshGrid() {
        // Obtener las necesidades a través de la API REST usando
        // ParameterizedTypeReference
        List<Necesidad> necesidades = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Necesidad>>() {
                }).getBody();

        if (necesidades != null) {
            grid.setItems(necesidades);
        } else {
            Notification.show("No se pudieron cargar las necesidades");
        }
    }

    private Component crearTitulo() {
        H3 titulo = new H3("Añada su Necesidad:");
        titulo.getStyle().set("text-align", "center").set("color", "#3498db");
        return titulo;
    }
}