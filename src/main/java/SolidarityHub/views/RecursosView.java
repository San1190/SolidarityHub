package SolidarityHub.views;

import SolidarityHub.models.Recursos;
import SolidarityHub.models.Recursos.TipoRecurso;
import SolidarityHub.models.Recursos.EstadoRecurso;
import SolidarityHub.models.Usuario;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.vaadin.flow.server.VaadinSession;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.List;
import org.springframework.web.client.RestTemplate;

@Route(value = "inventario", layout = MainLayout.class)
@PageTitle("Inventario | SolidarityHub")
public class RecursosView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080/api/recursos";
    private final Grid<Recursos> grid = new Grid<>(Recursos.class, false);
    private final Binder<Recursos> binder = new Binder<>(Recursos.class);
    private Usuario usuario;
    private Recursos recursoActual;

    public RecursosView() {
        // Recuperar el usuario actual desde la sesión
        usuario = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");
        if (usuario == null) {
            UI.getCurrent().navigate("/");
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setHeight("auto");

        add(crearTitulo());

        // Layout para el formulario
        FormLayout formLayout = createFormLayout();
        add(formLayout);

        // Layout para el grid
        VerticalLayout gridLayout = createGridLayout();
        add(gridLayout);

        refreshGrid();
    }

    private Component crearTitulo() {
        H3 titulo = new H3("Gestión de Inventario");
        titulo.getStyle().set("text-align", "center").set("color", "#1676F3");
        return titulo;
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("100%");
        formLayout.setMaxWidth("800px");

        // Campos del formulario
        ComboBox<TipoRecurso> tipoRecursoField = new ComboBox<>("Tipo de Recurso");
        tipoRecursoField.setItems(TipoRecurso.values());
        tipoRecursoField.setItemLabelGenerator(Enum::name);
        binder.forField(tipoRecursoField).asRequired("El tipo de recurso es obligatorio")
                .bind(Recursos::getTipoRecurso, Recursos::setTipoRecurso);

        TextArea descripcionField = new TextArea("Descripción");
        descripcionField.setWidthFull();
        descripcionField.setHeight("100px");
        binder.forField(descripcionField).asRequired("La descripción es obligatoria")
                .bind(Recursos::getDescripcion, (recursos, value) -> recursos.setDescripcion(value));

        // Añadir campo para cantidad
        TextField cantidadField = new TextField("Cantidad");
        cantidadField.setWidthFull();
        binder.forField(cantidadField)
                .asRequired("La cantidad es obligatoria")
                .withConverter(
                        Integer::valueOf,
                        String::valueOf,
                        "Por favor ingrese un número válido")
                .bind(Recursos::getCantidad, Recursos::setCantidad);

        // Añadir campo para estado
        ComboBox<EstadoRecurso> estadoField = new ComboBox<>("Estado");
        estadoField.setItems(EstadoRecurso.values());
        estadoField.setItemLabelGenerator(Enum::name);
        binder.forField(estadoField)
                .bind(Recursos::getEstado, Recursos::setEstado);

        // Botón para guardar
        Button saveButton = new Button("Guardar", event -> {
            if (binder.isValid()) {
                Recursos nuevoRecurso = new Recursos();
                binder.writeBeanIfValid(nuevoRecurso);

                // Guardar el recurso a través de la API REST
                restTemplate.postForObject(apiUrl + "/crear", nuevoRecurso, Recursos.class);
                Notification.show("Recurso guardado correctamente");
                refreshGrid();
                binder.readBean(new Recursos()); // Limpiar el formulario
            } else {
                Notification.show("Por favor, complete todos los campos obligatorios");
            }
        });
        saveButton.getElement().getThemeList().add("primary");

        Button cancelButton = new Button("Cancelar", event -> {
            // Limpiar el formulario
            binder.readBean(new Recursos());
        });

        HorizontalLayout botonLayout = new HorizontalLayout(saveButton, cancelButton);
        botonLayout.setWidthFull();
        botonLayout.setJustifyContentMode(JustifyContentMode.START);

        // Añadir campos al formulario
        formLayout.add(tipoRecursoField, descripcionField, cantidadField, estadoField, botonLayout);
        return formLayout;
    }

    private VerticalLayout createGridLayout() {
        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setSizeFull();

        // Configurar el grid
        grid.addColumn(recursos -> recursos.getTipoRecurso().name()).setHeader("Tipo de Recurso");
        grid.addColumn(Recursos::getDescripcion).setHeader("Descripción");
        grid.addColumn(Recursos::getCantidad).setHeader("Cantidad");
        grid.addColumn(recursos -> recursos.getEstado().name()).setHeader("Estado");
        grid.addColumn(recursos -> {
            if (recursos.getTareaAsignada() != null) {
                return recursos.getTareaAsignada().getNombre();
            } else {
                return "No asignado";
            }
        }).setHeader("Tarea Asignada");
        grid.addColumn(Recursos::getId).setHeader("ID");

        // Añadir botones de acción para cada fila
        grid.addComponentColumn(recurso -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button editButton = new Button("Editar", e -> {
                // Abrir diálogo de edición en lugar de cargar en el formulario principal
                abrirDialogoEdicion(recurso);
            });
            editButton.getElement().getThemeList().add("primary");

            Button deleteButton = new Button("Eliminar", e -> {
                try {
                    // Eliminar el recurso a través de la API REST
                    restTemplate.delete(apiUrl + "/" + recurso.getId());
                    Notification.show("Recurso eliminado correctamente");
                    refreshGrid();
                } catch (Exception ex) {
                    Notification.show("Error al eliminar el recurso: " + ex.getMessage());
                }
            });

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Acciones");

        gridLayout.add(grid);
        return gridLayout;
    }

    private void abrirDialogoEdicion(Recursos recurso) {
        // Crear un nuevo diálogo
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        // Título del diálogo
        H3 titulo = new H3("Editar Recurso");
        titulo.getStyle().set("margin-top", "0").set("color", "#3498db");

        // Crear un nuevo binder para el diálogo
        Binder<Recursos> dialogBinder = new Binder<>(Recursos.class);

        // Crear formulario
        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("100%");

        // Campos del formulario
        ComboBox<TipoRecurso> tipoRecursoField = new ComboBox<>("Tipo de Recurso");
        tipoRecursoField.setItems(TipoRecurso.values());
        tipoRecursoField.setItemLabelGenerator(Enum::name);
        dialogBinder.forField(tipoRecursoField).asRequired("El tipo de recurso es obligatorio")
                .bind(Recursos::getTipoRecurso, Recursos::setTipoRecurso);

        TextArea descripcionField = new TextArea("Descripción");
        descripcionField.setWidthFull();
        descripcionField.setHeight("100px");
        dialogBinder.forField(descripcionField).asRequired("La descripción es obligatoria")
                .bind(Recursos::getDescripcion, Recursos::setDescripcion);

        TextField cantidadField = new TextField("Cantidad");
        cantidadField.setWidthFull();
        dialogBinder.forField(cantidadField)
                .asRequired("La cantidad es obligatoria")
                .withConverter(
                        Integer::valueOf,
                        String::valueOf,
                        "Por favor ingrese un número válido")
                .bind(Recursos::getCantidad, Recursos::setCantidad);

        ComboBox<EstadoRecurso> estadoField = new ComboBox<>("Estado");
        estadoField.setItems(EstadoRecurso.values());
        estadoField.setItemLabelGenerator(Enum::name);
        dialogBinder.forField(estadoField)
                .bind(Recursos::getEstado, Recursos::setEstado);

        // Cargar los datos del recurso en el formulario
        dialogBinder.readBean(recurso);

        // Botones de acción
        Button guardarButton = new Button("Actualizar", event -> {
            if (dialogBinder.isValid()) {
                try {
                    // Actualizar el bean con los valores del formulario
                    dialogBinder.writeBeanIfValid(recurso);

                    // Actualizar el recurso a través de la API REST
                    restTemplate.put(apiUrl + "/" + recurso.getId(), recurso);

                    Notification.show("Recurso actualizado correctamente");
                    dialog.close();
                    refreshGrid();
                } catch (Exception ex) {
                    Notification.show("Error al actualizar el recurso: " + ex.getMessage());
                }
            } else {
                Notification.show("Por favor, complete todos los campos obligatorios");
            }
        });
        guardarButton.getStyle().set("background-color", "#3498db").set("color", "white");

        Button cancelarButton = new Button("Cancelar", event -> dialog.close());

        HorizontalLayout botonesLayout = new HorizontalLayout(guardarButton, cancelarButton);
        botonesLayout.setWidthFull();
        botonesLayout.setJustifyContentMode(JustifyContentMode.END);

        // Añadir componentes al formulario
        formLayout.add(tipoRecursoField, descripcionField, cantidadField, estadoField);

        // Añadir componentes al diálogo
        VerticalLayout dialogLayout = new VerticalLayout(titulo, formLayout, botonesLayout);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void refreshGrid() {
        try {
            // Obtener los recursos a través de la API REST
            List<Recursos> recursos = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Recursos>>() {
                    }).getBody();

            if (recursos != null) {
                grid.setItems(recursos);
            } else {
                Notification.show("No se pudieron cargar los recursos");
            }
        } catch (Exception e) {
            Notification.show("Error al cargar los recursos: " + e.getMessage());
            e.printStackTrace(); // Add this to see more details in the console
        }
    }
}