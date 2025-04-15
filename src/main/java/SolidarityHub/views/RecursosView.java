package SolidarityHub.views;

import SolidarityHub.models.Recursos;
import SolidarityHub.models.Recursos.TipoRecurso;
import SolidarityHub.models.Usuario;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
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
        titulo.getStyle().set("text-align", "center").set("color", "#3498db");
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
        saveButton.getStyle().set("background-color", "#3498db").set("color", "white");
        HorizontalLayout botonLayout = new HorizontalLayout(saveButton);
        botonLayout.setWidthFull();
        botonLayout.setJustifyContentMode(JustifyContentMode.START);
        
        // Añadir campos al formulario
        formLayout.add(tipoRecursoField, descripcionField, botonLayout);
        return formLayout;
    }
    
    private VerticalLayout createGridLayout() {
        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setSizeFull();
        
        // Configurar el grid
        grid.addColumn(recursos -> recursos.getTipoRecurso().name()).setHeader("Tipo de Recurso");
        grid.addColumn(Recursos::getDescripcion).setHeader("Descripción");
        grid.addColumn(Recursos::getId).setHeader("ID");
        
        // Añadir botones de acción para cada fila
        grid.addComponentColumn(recurso -> {
            HorizontalLayout actions = new HorizontalLayout();
            
            Button editButton = new Button("Editar", e -> {
                // Cargar el recurso en el formulario para edición
                binder.readBean(recurso);
                Notification.show("Editando recurso: " + recurso.getDescripcion());
            });
            
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