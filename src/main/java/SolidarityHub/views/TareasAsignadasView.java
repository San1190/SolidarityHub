package SolidarityHub.views;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import SolidarityHub.services.NotificacionServicio;
import SolidarityHub.services.TareaServicio;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vista para que los voluntarios gestionen sus tareas asignadas.
 * Permite ver, aceptar o rechazar tareas propuestas.
 */
@Route(value = "tareas-asignadas", layout = MainLayout.class)
@PageTitle("Tareas Asignadas | SolidarityHub")
public class TareasAsignadasView extends VerticalLayout {

    private final TareaServicio tareaServicio;
    private final NotificacionServicio notificacionServicio;
    private final Usuario usuario;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private Grid<Tarea> gridTareasPendientes;
    private Grid<Tarea> gridTareasAceptadas;
    private VerticalLayout contenidoLayout;
    private Map<Tab, Component> tabsToPages = new HashMap<>();

    public TareasAsignadasView(TareaServicio tareaServicio, NotificacionServicio notificacionServicio) {
        this.tareaServicio = tareaServicio;
        this.notificacionServicio = notificacionServicio;
        this.usuario = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");
        
        if (usuario == null || !"voluntario".equals(usuario.getTipoUsuario())) {
            add(new H3("Acceso no autorizado"));
            return;
        }
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        H2 titulo = new H2("Gestión de Tareas Asignadas");
        titulo.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.XXLARGE);
        
        // Crear pestañas
        Tab tabPendientes = new Tab("Tareas Pendientes de Confirmación");
        Tab tabAceptadas = new Tab("Tareas Aceptadas");
        
        Tabs tabs = new Tabs(tabPendientes, tabAceptadas);
        tabs.setWidthFull();
        
        // Crear contenido para cada pestaña
        contenidoLayout = new VerticalLayout();
        contenidoLayout.setSizeFull();
        contenidoLayout.setSpacing(true);
        contenidoLayout.setPadding(true);
        
        // Configurar componentes para cada pestaña
        tabsToPages.put(tabPendientes, crearContenidoTareasPendientes());
        tabsToPages.put(tabAceptadas, crearContenidoTareasAceptadas());
        
        // Mostrar contenido inicial
        contenidoLayout.add(tabsToPages.get(tabs.getSelectedTab()));
        
        // Manejar cambio de pestañas
        tabs.addSelectedChangeListener(event -> {
            contenidoLayout.removeAll();
            contenidoLayout.add(tabsToPages.get(tabs.getSelectedTab()));
        });
        
        add(titulo, tabs, contenidoLayout);
    }
    
    private Component crearContenidoTareasPendientes() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        
        gridTareasPendientes = new Grid<>(Tarea.class, false);
        configurarGridTareas(gridTareasPendientes, true);
        
        // Cargar tareas pendientes de confirmación
        actualizarTareasPendientes();
        
        layout.add(new H3("Tareas pendientes de confirmación"), gridTareasPendientes);
        return layout;
    }
    
    private Component crearContenidoTareasAceptadas() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        
        gridTareasAceptadas = new Grid<>(Tarea.class, false);
        configurarGridTareas(gridTareasAceptadas, false);
        
        // Cargar tareas aceptadas
        actualizarTareasAceptadas();
        
        layout.add(new H3("Tareas aceptadas"), gridTareasAceptadas);
        return layout;
    }
    
    private void configurarGridTareas(Grid<Tarea> grid, boolean mostrarAcciones) {
        grid.addColumn(Tarea::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Tarea::getDescripcion).setHeader("Descripción").setAutoWidth(true);
        grid.addColumn(tarea -> tarea.getTipo().toString()).setHeader("Tipo").setAutoWidth(true);
        grid.addColumn(Tarea::getLocalizacion).setHeader("Localización").setAutoWidth(true);
        grid.addColumn(tarea -> tarea.getFechaInicio().format(formatter)).setHeader("Fecha Inicio").setAutoWidth(true);
        grid.addColumn(tarea -> tarea.getFechaFin().format(formatter)).setHeader("Fecha Fin").setAutoWidth(true);
        grid.addColumn(tarea -> tarea.getEstado().toString()).setHeader("Estado").setAutoWidth(true);
        
        if (mostrarAcciones) {
            grid.addComponentColumn(tarea -> {
                HorizontalLayout actions = new HorizontalLayout();
                
                Button aceptarBtn = new Button(new Icon(VaadinIcon.CHECK), e -> {
                    aceptarTarea(tarea);
                });
                aceptarBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
                aceptarBtn.getElement().setAttribute("title", "Aceptar tarea");
                
                Button rechazarBtn = new Button(new Icon(VaadinIcon.CLOSE_SMALL), e -> {
                    rechazarTarea(tarea);
                });
                rechazarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                rechazarBtn.getElement().setAttribute("title", "Rechazar tarea");
                
                actions.add(aceptarBtn, rechazarBtn);
                return actions;
            }).setHeader("Acciones").setAutoWidth(true);
        }
        
        grid.setItems(List.of());
        grid.setAllRowsVisible(true);
    }
    
    private void actualizarTareasPendientes() {
        // Aquí se obtendría la lista de tareas pendientes de confirmación
        // del servicio correspondiente
        List<Tarea> tareasPendientes = tareaServicio.obtenerTareasPendientesConfirmacion((Voluntario) usuario);
        gridTareasPendientes.setItems(tareasPendientes);
    }
    
    private void actualizarTareasAceptadas() {
        // Aquí se obtendría la lista de tareas aceptadas
        // del servicio correspondiente
        List<Tarea> tareasAceptadas = tareaServicio.obtenerTareasAceptadas((Voluntario) usuario);
        gridTareasAceptadas.setItems(tareasAceptadas);
    }
    
    private void aceptarTarea(Tarea tarea) {
        boolean resultado = notificacionServicio.responderAsignacionTarea(tarea.getId(), usuario.getId(), true);
        if (resultado) {
            Notification.show("Has aceptado la tarea: " + tarea.getNombre(), 3000, Notification.Position.BOTTOM_CENTER);
            actualizarTareasPendientes();
            actualizarTareasAceptadas();
        } else {
            Notification.show("No se pudo aceptar la tarea", 3000, Notification.Position.BOTTOM_CENTER);
        }
    }
    
    private void rechazarTarea(Tarea tarea) {
        boolean resultado = notificacionServicio.responderAsignacionTarea(tarea.getId(), usuario.getId(), false);
        if (resultado) {
            Notification.show("Has rechazado la tarea: " + tarea.getNombre(), 3000, Notification.Position.BOTTOM_CENTER);
            actualizarTareasPendientes();
        } else {
            Notification.show("No se pudo rechazar la tarea", 3000, Notification.Position.BOTTOM_CENTER);
        }
    }
}