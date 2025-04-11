package SolidarityHub.views;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea.EstadoTarea;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Route(value = "tareas", layout = MainLayout.class)
@PageTitle("Tareas | SolidarityHub")
public class TareasView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080/api/tareas";
    private final Grid<Tarea> grid = new Grid<>(Tarea.class, false);
    private final Binder<Tarea> binder = new Binder<>(Tarea.class);
    private Dialog formDialog;
    private Tarea tareaActual;

    public TareasView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(crearTitulo());
        add(crearFiltros());
        add(crearBotonNuevo());
        add(configurarGrid());

        refreshGrid();
    }

    private Component crearTitulo() {
        H3 titulo = new H3("Gestión de Tareas");
        titulo.getStyle().set("margin", "0");
        return titulo;
    }

    private Component crearFiltros() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();

        ComboBox<EstadoTarea> filtroEstado = new ComboBox<>("Estado");
        filtroEstado.setItems(EstadoTarea.values());
        filtroEstado.setItemLabelGenerator(Enum::name);
        filtroEstado.setClearButtonVisible(true);

        ComboBox<TipoNecesidad> filtroTipo = new ComboBox<>("Tipo");
        filtroTipo.setItems(TipoNecesidad.values());
        filtroTipo.setItemLabelGenerator(Enum::name);
        filtroTipo.setClearButtonVisible(true);

        Button filtrarButton = new Button("Filtrar", e -> {
            filtrarTareas(filtroEstado.getValue(), filtroTipo.getValue());
        });

        Button limpiarButton = new Button("Limpiar", e -> {
            filtroEstado.clear();
            filtroTipo.clear();
            refreshGrid();
        });

        layout.add(filtroEstado, filtroTipo, filtrarButton, limpiarButton);
        return layout;
    }

    private Button crearBotonNuevo() {
        Button nuevaTarea = new Button("Nueva Tarea", e -> abrirFormulario(new Tarea()));
        return nuevaTarea;
    }

    private Component configurarGrid() {
        grid.addColumn(Tarea::getNombre).setHeader("Nombre").setSortable(true);
        grid.addColumn(Tarea::getDescripcion).setHeader("Descripción");
        grid.addColumn(tarea -> tarea.getTipo() != null ? tarea.getTipo().name() : "").setHeader("Tipo").setSortable(true);
        grid.addColumn(Tarea::getLocalizacion).setHeader("Localización");
        grid.addColumn(Tarea::getNumeroVoluntariosNecesarios).setHeader("Voluntarios Necesarios");
        grid.addColumn(tarea -> tarea.getFechaInicio() != null ? tarea.getFechaInicio().toString() : "").setHeader("Fecha Inicio");
        grid.addColumn(tarea -> tarea.getFechaFin() != null ? tarea.getFechaFin().toString() : "").setHeader("Fecha Fin");
        grid.addColumn(tarea -> tarea.getEstado() != null ? tarea.getEstado().name() : "").setHeader("Estado").setSortable(true);

        grid.addComponentColumn(tarea -> {
            HorizontalLayout buttons = new HorizontalLayout();
            Button editButton = new Button("Editar", e -> abrirFormulario(tarea));
            Button deleteButton = new Button("Eliminar", e -> eliminarTarea(tarea));
            buttons.add(editButton, deleteButton);
            return buttons;
        }).setHeader("Acciones");

        grid.setWidthFull();
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        return grid;
    }

    private void abrirFormulario(Tarea tarea) {
        tareaActual = tarea;
        formDialog = new Dialog();
        formDialog.setWidth("800px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        H3 titulo = new H3(tareaActual.getId() == null ? "Nueva Tarea" : "Editar Tarea");
        dialogLayout.add(titulo);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();

        TextField nombreField = new TextField("Nombre");
        binder.forField(nombreField).asRequired("El nombre es obligatorio").bind(Tarea::getNombre, Tarea::setNombre);

        TextArea descripcionField = new TextArea("Descripción");
        descripcionField.setWidthFull();
        descripcionField.setHeight("100px");
        binder.forField(descripcionField).asRequired("La descripción es obligatoria").bind(Tarea::getDescripcion, Tarea::setDescripcion);

        ComboBox<TipoNecesidad> tipoField = new ComboBox<>("Tipo");
        tipoField.setItems(TipoNecesidad.values());
        tipoField.setItemLabelGenerator(Enum::name);
        binder.forField(tipoField).asRequired("El tipo es obligatorio").bind(Tarea::getTipo, Tarea::setTipo);

        TextField localizacionField = new TextField("Localización");
        binder.forField(localizacionField).asRequired("La localización es obligatoria").bind(Tarea::getLocalizacion, Tarea::setLocalizacion);

        IntegerField voluntariosField = new IntegerField("Número de Voluntarios");
        voluntariosField.setMin(1);
        binder.forField(voluntariosField).asRequired("El número de voluntarios es obligatorio")
                .bind(Tarea::getNumeroVoluntariosNecesarios, Tarea::setNumeroVoluntariosNecesarios);

        DateTimePicker fechaInicioField = new DateTimePicker("Fecha de Inicio");
        binder.forField(fechaInicioField).asRequired("La fecha de inicio es obligatoria").bind(Tarea::getFechaInicio, Tarea::setFechaInicio);

        DateTimePicker fechaFinField = new DateTimePicker("Fecha de Fin");
        binder.forField(fechaFinField).asRequired("La fecha de fin es obligatoria").bind(Tarea::getFechaFin, Tarea::setFechaFin);

        ComboBox<EstadoTarea> estadoField = new ComboBox<>("Estado");
        estadoField.setItems(EstadoTarea.values());
        estadoField.setItemLabelGenerator(Enum::name);
        binder.forField(estadoField).asRequired("El estado es obligatorio").bind(Tarea::getEstado, Tarea::setEstado);

        formLayout.add(nombreField, descripcionField, tipoField, localizacionField, voluntariosField, fechaInicioField, fechaFinField, estadoField);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button saveButton = new Button("Guardar", e -> guardarTarea());
        Button cancelButton = new Button("Cancelar", e -> formDialog.close());
        buttonLayout.add(saveButton, cancelButton);

        dialogLayout.add(formLayout, buttonLayout);
        formDialog.add(dialogLayout);

        binder.readBean(tareaActual);
        formDialog.open();
    }

    private void guardarTarea() {
        if (binder.isValid()) {
            try {
                binder.writeBean(tareaActual);
                if (tareaActual.getId() == null) {
                    // Crear nueva tarea
                    restTemplate.postForObject(apiUrl + "/crear", tareaActual, Tarea.class);
                    Notification.show("Tarea creada correctamente", 3000, Position.BOTTOM_START);
                } else {
                    // Actualizar tarea existente
                    restTemplate.put(apiUrl + "/" + tareaActual.getId(), tareaActual);
                    Notification.show("Tarea actualizada correctamente", 3000, Position.BOTTOM_START);
                }
                formDialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Error al guardar la tarea: " + ex.getMessage(), 3000, Position.BOTTOM_START);
            }
        }
    }

    private void eliminarTarea(Tarea tarea) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3("Confirmar eliminación"));
        layout.add("¿Está seguro de que desea eliminar la tarea '" + tarea.getNombre() + "'?");

        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button confirmButton = new Button("Eliminar", e -> {
            try {
                restTemplate.delete(apiUrl + "/" + tarea.getId());
                Notification.show("Tarea eliminada correctamente", 3000, Position.BOTTOM_START);
                confirmDialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Error al eliminar la tarea: " + ex.getMessage(), 3000, Position.BOTTOM_START);
            }
        });
        Button cancelButton = new Button("Cancelar", e -> confirmDialog.close());
        buttonLayout.add(confirmButton, cancelButton);

        layout.add(buttonLayout);
        confirmDialog.add(layout);
        confirmDialog.open();
    }

    private void refreshGrid() {
        try {
            List<Tarea> tareas = restTemplate.getForObject(apiUrl, List.class);
            grid.setItems(tareas);
        } catch (Exception ex) {
            Notification.show("Error al cargar las tareas: " + ex.getMessage(), 3000, Position.BOTTOM_START);
        }
    }

    private void filtrarTareas(EstadoTarea estado, TipoNecesidad tipo) {
        try {
            String url = apiUrl + "/filtrar";
            boolean hasParams = false;

            if (estado != null) {
                url += "?estado=" + estado;
                hasParams = true;
            }

            if (tipo != null) {
                url += hasParams ? "&tipo=" + tipo : "?tipo=" + tipo;
            }

            List<Tarea> tareas = restTemplate.getForObject(url, List.class);
            grid.setItems(tareas);
        } catch (Exception ex) {
            Notification.show("Error al filtrar las tareas: " + ex.getMessage(), 3000, Position.BOTTOM_START);
        }
    }
}