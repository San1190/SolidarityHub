package SolidarityHub.views;

import SolidarityHub.models.Afectado;
import SolidarityHub.models.Necesidad;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Necesidad.Urgencia;
import com.vaadin.flow.component.datepicker.DatePicker;
import SolidarityHub.services.GeolocalizacionServicio;
import SolidarityHub.services.NecesidadServicio;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "necesidades", layout = MainLayout.class) 
@PageTitle("Necesidades | SolidarityHub")
public class NecesidadesView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080/api/necesidades";
    private final Grid<Necesidad> grid = new Grid<>(Necesidad.class, false);
    private final Binder<Necesidad> binder = new Binder<>(Necesidad.class);
    private final GeolocalizacionServicio geolocalizacionServicio;

    @Autowired
    public NecesidadesView(GeolocalizacionServicio geolocalizacionServicio) {
        this.geolocalizacionServicio = geolocalizacionServicio;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setHeight("auto");

        add(crearTitulo());

        // Filtros
        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setWidthFull();
        filtrosLayout.setSpacing(true);
        filtrosLayout.setAlignItems(Alignment.CENTER);
        filtrosLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        filtrosLayout.getStyle()
            .set("background-color", "#f3f3f3")
            .set("border-radius", "10px")
            .set("padding", "18px 12px 12px 12px")
            .set("margin-bottom", "18px")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.04)");

        ComboBox<TipoNecesidad> filtroTipo = new ComboBox<>("Tipo de Necesidad");
        filtroTipo.setItems(TipoNecesidad.values());
        filtroTipo.setClearButtonVisible(true);
        filtroTipo.setPlaceholder("Todos");
        filtroTipo.setWidth("180px");

        ComboBox<Urgencia> filtroUrgencia = new ComboBox<>("Urgencia");
        filtroUrgencia.setItems(Urgencia.values());
        filtroUrgencia.setClearButtonVisible(true);
        filtroUrgencia.setPlaceholder("Todas");
        filtroUrgencia.setWidth("150px");

        TextField filtroUbicacion = new TextField("Ubicación");
        filtroUbicacion.setPlaceholder("Cualquier ubicación");
        filtroUbicacion.setWidth("180px");

        Button btnFiltrar = new Button("Filtrar", e -> refreshGridFiltrado(filtroTipo.getValue(), filtroUrgencia.getValue(), filtroUbicacion.getValue()));
        btnFiltrar.getElement().getThemeList().add("primary");
        btnFiltrar.getStyle()
            .set("margin-left", "8px")
            .set("margin-right", "8px")
            .set("font-weight", "bold");

        Button btnLimpiar = new Button("Limpiar", e -> {
            filtroTipo.clear();
            filtroUrgencia.clear();
            filtroUbicacion.clear();
            refreshGrid();
        });
        btnLimpiar.getStyle()
            .set("margin-right", "8px")
            .set("font-weight", "bold");

        HorizontalLayout botonesLayout = new HorizontalLayout(btnFiltrar, btnLimpiar);
        botonesLayout.setAlignItems(Alignment.CENTER);
        botonesLayout.setSpacing(true);
        botonesLayout.getStyle().set("margin-left", "12px");

        filtrosLayout.add(filtroTipo, filtroUrgencia, filtroUbicacion, botonesLayout);
        add(filtrosLayout);
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

        //Añadir los campos de fecha de inicio y fecha de fin
        DatePicker fechaInicioField = new DatePicker("Fecha de Inicio");
        binder.forField(fechaInicioField)
              .asRequired("La fecha de inicio es obligatoria")
              .withConverter(
                  date -> date != null ? date.atStartOfDay() : null,
                  dateTime -> dateTime != null ? dateTime.toLocalDate() : null
              )
              .bind(Necesidad::getFechaInicio, Necesidad::setFechaInicio);

        // Campo de ubicación
        TextField ubicacionField = new TextField("Ubicación");
        ubicacionField.setPlaceholder("Escriba la dirección (ej: Calle Mayor 123, Valencia)");
        ubicacionField.setRequired(true);
        ubicacionField.setWidthFull();
        
        binder.forField(ubicacionField).asRequired("La ubicación es obligatoria")
                .bind(Necesidad::getUbicacion, Necesidad::setUbicacion);

        Button saveButton = new Button("Guardar", event -> {
            if (binder.isValid()) {
                Necesidad nuevaNecesidad = new Necesidad();
                binder.writeBeanIfValid(nuevaNecesidad);
                nuevaNecesidad.setEstadoNecesidad(Necesidad.EstadoNecesidad.REGISTRADA);
                nuevaNecesidad.setFechaCreacion(LocalDateTime.now());
                nuevaNecesidad.setCreador((Afectado) VaadinSession.getCurrent().getAttribute("usuario"));

                restTemplate.postForObject(apiUrl + "/crear", nuevaNecesidad, Necesidad.class);
                Notification.show("Necesidad guardada correctamente");
                refreshGrid();
                binder.readBean(new Necesidad());
            } else {
                Notification.show("Por favor, complete todos los campos obligatorios");
            }
        });
        saveButton.getElement().getThemeList().add("primary");
        HorizontalLayout botonLayout = new HorizontalLayout(saveButton);
        botonLayout.setWidthFull();
        botonLayout.setJustifyContentMode(JustifyContentMode.END);

        // Añadir campos al formulario en el nuevo orden
        formLayout.add(tipoNecesidadField, descripcionField, urgenciaField, fechaInicioField, ubicacionField, botonLayout);

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
        grid.addColumn(necesidad -> necesidad.getFechaInicio() != null ? necesidad.getFechaInicio().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "").setHeader("Fecha de Inicio").setAutoWidth(true).setFlexGrow(0);

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

    private void refreshGridFiltrado(TipoNecesidad tipo, Urgencia urgencia, String ubicacion) {
        List<Necesidad> necesidades = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Necesidad>>() {
                }).getBody();
        if (necesidades != null) {
            if (tipo != null) {
                necesidades = necesidades.stream().filter(n -> n.getTipoNecesidad() == tipo).toList();
            }
            if (urgencia != null) {
                necesidades = necesidades.stream().filter(n -> n.getUrgencia() == urgencia).toList();
            }
            if (ubicacion != null && !ubicacion.isEmpty()) {
                necesidades = necesidades.stream().filter(n -> n.getUbicacion() != null && n.getUbicacion().toLowerCase().contains(ubicacion.toLowerCase())).toList();
            }
            grid.setItems(necesidades);
        } else {
            Notification.show("No se pudieron cargar las necesidades");
        }
    }
    private Component crearTitulo() {
        H3 titulo = new H3("Añada su Necesidad:");
        titulo.getStyle().set("text-align", "center").set("color", "#1676F3");
        return titulo;
    }
}