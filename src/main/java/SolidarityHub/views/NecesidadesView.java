package SolidarityHub.views;

import SolidarityHub.models.Afectado;
import SolidarityHub.models.Necesidad;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Necesidad.Urgencia;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.web.client.RestTemplate;

@Route(value = "necesidades", layout = MainLayout.class) 
@PageTitle("Necesidades | SolidarityHub")
public class NecesidadesView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080/api/necesidades";
    private final Grid<Necesidad> grid = new Grid<>(Necesidad.class, false);
    private final Binder<Necesidad> binder = new Binder<>(Necesidad.class);

    public NecesidadesView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setHeight("auto");

        add(crearTitulo());

        FormLayout formLayout = createFormLayout();
        add(formLayout);

        VerticalLayout gridLayout = createGridLayout();
        add(gridLayout);

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

        //Añadir los campos de fecha de inicio y fecha de fin
        DateTimePicker fechaInicioField = new DateTimePicker("Fecha de Inicio");
        binder.forField(fechaInicioField)
              .asRequired("La fecha de inicio es obligatoria")
              .bind(Necesidad::getFechaInicio, Necesidad::setFechaInicio);
            
        DateTimePicker fechaFinField = new DateTimePicker("Fecha de Fin");
        binder.forField(fechaFinField)
              .asRequired("La fecha de fin es obligatoria")
              .bind(Necesidad::getFechaFinalizacion, Necesidad::setFechaFinalizacion);
            

        //guardar el usuario que ha creado la necesidad
       

        
        Button saveButton = new Button("Guardar", event -> {
            if (binder.isValid()) {
                Necesidad nuevaNecesidad = new Necesidad();
                binder.writeBeanIfValid(nuevaNecesidad);
                nuevaNecesidad.setEstadoNecesidad(Necesidad.EstadoNecesidad.REGISTRADA);
                nuevaNecesidad.setFechaCreacion(LocalDateTime.now());
                nuevaNecesidad.setCreador((Afectado) VaadinSession.getCurrent().getAttribute("usuario")); // Obtener el usuario actual de la sesión

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
        botonLayout.setJustifyContentMode(JustifyContentMode.END);

        // Añadir campos al formulario
        formLayout.add(tipoNecesidadField, descripcionField, urgenciaField, ubicacionField, fechaInicioField, fechaFinField, botonLayout);
        return formLayout;
    }

    private VerticalLayout createGridLayout() {
        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setSizeFull();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        grid.addColumn(necesidad -> necesidad.getTipoNecesidad().name()).setHeader("Tipo de Necesidad").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Necesidad::getDescripcion).setHeader("Descripción").setFlexGrow(1);
        grid.addColumn(Necesidad::getUbicacion).setHeader("Ubicación");
        grid.addColumn(necesidad -> necesidad.getUrgencia().name()).setHeader("Urgencia").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(necesidad -> necesidad.getFechaCreacion().format(formatter)).setHeader("Fecha de Creación").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(necesidad -> necesidad.getFechaInicio().format(formatter)).setHeader("Fecha de Inicio").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(necesidad -> necesidad.getFechaFinalizacion().format(formatter)).setHeader("Fecha de Fin").setAutoWidth(true).setFlexGrow(0);

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