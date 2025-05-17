package SolidarityHub.views;

import SolidarityHub.models.Afectado;
import SolidarityHub.models.Necesidad;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Necesidad.Urgencia;
import com.vaadin.flow.component.datepicker.DatePicker;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
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

// Importaciones para el mapa
import software.xdev.vaadin.maps.leaflet.MapContainer;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.raster.LTileLayer;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;
import software.xdev.vaadin.maps.leaflet.registry.LDefaultComponentManagementRegistry;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;

@Route(value = "necesidades", layout = MainLayout.class) 
@PageTitle("Necesidades | SolidarityHub")
public class NecesidadesView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080/api/necesidades";
    private final Grid<Necesidad> grid = new Grid<>(Necesidad.class, false);
    private final Binder<Necesidad> binder = new Binder<>(Necesidad.class);
    private final double[] selectedCoords = new double[2];

    public NecesidadesView() {
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

        // Campo de ubicación con botón para abrir el mapa
        HorizontalLayout ubicacionLayout = new HorizontalLayout();
        ubicacionLayout.setWidthFull();
        ubicacionLayout.setSpacing(true);
        
        TextField ubicacionField = new TextField("Ubicación");
        ubicacionField.setWidthFull();
        ubicacionField.setReadOnly(true);
        ubicacionField.setPlaceholder("Haga clic en el botón para seleccionar ubicación");
        
        Button btnSeleccionarUbicacion = new Button("Seleccionar en mapa");
        btnSeleccionarUbicacion.addClickListener(e -> mostrarMapaSeleccion(ubicacionField));
        
        ubicacionLayout.add(ubicacionField, btnSeleccionarUbicacion);
        
        binder.forField(ubicacionField).asRequired("La ubicación es obligatoria").bind(Necesidad::getUbicacion,
                Necesidad::setUbicacion);

        //Añadir los campos de fecha de inicio y fecha de fin
        DatePicker fechaInicioField = new DatePicker("Fecha de Inicio");
        binder.forField(fechaInicioField)
              .asRequired("La fecha de inicio es obligatoria")
              .withConverter(
                  date -> date != null ? date.atStartOfDay() : null,
                  dateTime -> dateTime != null ? dateTime.toLocalDate() : null
              )
              .bind(Necesidad::getFechaInicio, Necesidad::setFechaInicio);
            
        
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
        saveButton.getElement().getThemeList().add("primary");;
        HorizontalLayout botonLayout = new HorizontalLayout(saveButton);
        botonLayout.setWidthFull();
        botonLayout.setJustifyContentMode(JustifyContentMode.END);

        // Añadir campos al formulario
        formLayout.add(tipoNecesidadField, descripcionField, urgenciaField, ubicacionLayout, fechaInicioField, botonLayout);
        return formLayout;
    }

    private void mostrarMapaSeleccion(TextField ubicacionField) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Seleccionar ubicación");
        dialog.setWidth("600px");
        dialog.setHeight("400px");
        
        // Contenedor principal del mapa
        Div mapWrapper = new Div();
        mapWrapper.setWidthFull();
        mapWrapper.setHeight("300px");
        mapWrapper.getStyle().set("position", "relative");
        
        // Inicializar el mapa
        LComponentManagementRegistry registry = new LDefaultComponentManagementRegistry(this);
        MapContainer mapContainer = new MapContainer(registry);
        mapContainer.setSizeFull();
        
        // Obtener el mapa
        LMap map = mapContainer.getlMap();
        
        // Configurar el mapa
        map.addLayer(LTileLayer.createDefaultForOpenStreetMapTileServer(registry));
        map.setView(new LLatLng(registry, 39.4699, -0.3763), 13);
        
        // Añadir el mapa al contenedor
        mapWrapper.add(mapContainer);
        
        // ID para identificar este componente desde JavaScript
        mapWrapper.setId("necesidades-view");
        
        // Registrar evento de clic directamente en el mapa usando la API de Leaflet
        map.on("click", "e => document.getElementById('necesidades-view').$server.mapClicked(e.latlng.lat, e.latlng.lng)");
        
        // Método adicional para capturar clics usando el ID del contenedor del mapa
        UI.getCurrent().getPage().executeJs(
            "setTimeout(() => {" +
                "const mapContainer = document.querySelector('.leaflet-container');" +
                "if (mapContainer && mapContainer._leaflet_id) {" +
                    "const mapId = mapContainer._leaflet_id;" +
                    "if (L && L.map && L.map._instances && L.map._instances[mapId]) {" +
                        "console.log('Método directo: Mapa Leaflet encontrado');" +
                        "L.map._instances[mapId].on('click', function(e) {" +
                            "console.log('Método directo: Click en mapa', e.latlng);" +
                            "document.getElementById('necesidades-view').$server.mapClicked(e.latlng.lat, e.latlng.lng);" +
                        "});" +
                    "}" +
                "}" +
            "}, 1000);"
        );
        
        // Botones de acción
        HorizontalLayout actions = new HorizontalLayout();
        Button confirmButton = new Button("Confirmar ubicación", e -> {
            if (selectedCoords[0] != 0 && selectedCoords[1] != 0) {
                String ubicacion = String.format("%.6f, %.6f", selectedCoords[0], selectedCoords[1]);
                ubicacionField.setValue(ubicacion);
                dialog.close();
            } else {
                Notification.show("Por favor, seleccione una ubicación en el mapa", 3000, Notification.Position.MIDDLE);
            }
        });
        
        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        
        actions.add(confirmButton, cancelButton);
        
        // Añadir componentes al diálogo
        VerticalLayout dialogLayout = new VerticalLayout(mapWrapper, actions);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        
        dialog.add(dialogLayout);
        dialog.open();
    }

    @ClientCallable
    public void mapClicked(double lat, double lng) {
        // Guardar las coordenadas seleccionadas
        selectedCoords[0] = lat;
        selectedCoords[1] = lng;
        
        // Añadir marcador en el mapa
        UI.getCurrent().getPage().executeJs(
            "const map = document.querySelector('.leaflet-container')._leaflet_map;" +
            "// Eliminar marcadores anteriores" +
            "map.eachLayer(function(layer) {" +
                "if (layer instanceof L.Marker) {" +
                    "map.removeLayer(layer);" +
                "}" +
            "});" +
            // Crear nuevo marcador
            "const marker = L.marker([" + lat + ", " + lng + "]).addTo(map);" +
            "marker.bindPopup('Ubicación seleccionada: " + lat + ", " + lng + "').openPopup();"
        );
        
        // Mostrar notificación
        Notification.show(
            String.format("Ubicación seleccionada: %.6f, %.6f", lat, lng),
            2000,
            Notification.Position.BOTTOM_CENTER
        ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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