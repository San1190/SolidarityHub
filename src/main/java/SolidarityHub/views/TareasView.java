package SolidarityHub.views;

import SolidarityHub.models.*;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea.EstadoTarea;
import SolidarityHub.models.Recursos.TipoRecurso;

import SolidarityHub.models.dtos.NotificacionDTO;
import SolidarityHub.services.NotificacionServicio;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ShortcutEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "tareas", layout = MainLayout.class)
@PageTitle("Tareas | SolidarityHub")
public class TareasView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080/api/tareas";
    private final Binder<Tarea> binder = new Binder<>(Tarea.class);
    private final NotificacionServicio notificacionServicio;
    private Dialog formDialog;
    private Tarea tareaActual;
    private Usuario usuarioActual;
    private VerticalLayout tareasContainer;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TareasView(NotificacionServicio notificacionServicio) {
        this.notificacionServicio = notificacionServicio;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setHeight("auto");

        // Obtener el usuario actual de la sesión
        usuarioActual = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");

        // Crear y añadir componentes principales
        add(crearEncabezado());
        add(crearContenedorPrincipal());

        // Crear el contenedor para las tarjetas de tareas
        tareasContainer = new VerticalLayout();
        tareasContainer.setWidthFull();
        tareasContainer.setPadding(false);
        tareasContainer.setSpacing(true);
        tareasContainer.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "16px")
                .set("box-shadow", "none");
        tareasContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        add(tareasContainer);

        refreshTareas();
    }

    private Component crearEncabezado() {
        // Contenedor principal del encabezado con título y botón de nueva tarea
        HorizontalLayout encabezado = new HorizontalLayout();
        encabezado.setWidthFull();
        encabezado.setPadding(false);
        encabezado.setAlignItems(FlexComponent.Alignment.CENTER);
        encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Título con estilo mejorado
        H3 titulo = new H3("Gestión de Tareas");
        titulo.getStyle()
                .set("margin", "0")
                .set("font-weight", "600")
                .set("color", "#1676F3");

        // Contenedor para el botón de nueva tarea (solo para voluntarios)
        HorizontalLayout botonesEncabezado = new HorizontalLayout();
        botonesEncabezado.setAlignItems(FlexComponent.Alignment.CENTER);
        botonesEncabezado.setSpacing(true);

        if (usuarioActual instanceof Voluntario) {
            Button nuevaTarea = new Button("Nueva Tarea", new Icon(VaadinIcon.PLUS));
            nuevaTarea.addClickListener(e -> abrirFormulario(new Tarea()));
            nuevaTarea.getElement().getThemeList().add("primary");
            nuevaTarea.getStyle()
                    .set("font-weight", "bold")
                    .set("border-radius", "4px");
            botonesEncabezado.add(nuevaTarea);
        }

        encabezado.add(titulo, botonesEncabezado);
        return encabezado;
    }

    private Component crearContenedorPrincipal() {
        // Contenedor principal para filtros y acciones
        Div contenedorPrincipal = new Div();
        contenedorPrincipal.setWidthFull();
        contenedorPrincipal.getStyle()
                .set("background-color", "#f8f9fa")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("margin-bottom", "-50px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.05)");

        // Crear el layout de filtros y acciones
        HorizontalLayout layoutFiltros = crearLayoutFiltros();
        contenedorPrincipal.add(layoutFiltros);

        return contenedorPrincipal;
    }

    private HorizontalLayout crearLayoutFiltros() {
        // Layout principal para filtros y acciones de usuario
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layout.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "16px");

        // Panel izquierdo: filtros generales
        HorizontalLayout filtrosGenerales = new HorizontalLayout();
        filtrosGenerales.setAlignItems(FlexComponent.Alignment.BASELINE);
        filtrosGenerales.setSpacing(true);
        filtrosGenerales.getStyle().set("flex-wrap", "wrap");

        // Filtro de estado
        ComboBox<EstadoTarea> filtroEstado = new ComboBox<>("Estado");
        filtroEstado.setItems(EstadoTarea.values());
        filtroEstado.setItemLabelGenerator(Enum::name);
        filtroEstado.setClearButtonVisible(true);
        filtroEstado.getStyle().set("width", "150px");

        // Filtro de tipo
        ComboBox<TipoNecesidad> filtroTipo = new ComboBox<>("Tipo");
        filtroTipo.setItems(TipoNecesidad.values());
        filtroTipo.setItemLabelGenerator(Enum::name);
        filtroTipo.setClearButtonVisible(true);
        filtroTipo.getStyle().set("width", "150px");

        // Botones de acción para filtros
        Button filtrarButton = new Button("Filtrar", e -> {
            filtrarTareas(filtroEstado.getValue(), filtroTipo.getValue());
        });
        filtrarButton.getElement().getThemeList().add("primary");
        filtrarButton.getStyle()
                .set("margin-left", "8px")
                .set("margin-right", "8px");

        Button limpiarButton = new Button("Limpiar", e -> {
            filtroEstado.clear();
            filtroTipo.clear();
            refreshTareas();
        });
        limpiarButton.getStyle().set("margin-right", "16px");

        filtrosGenerales.add(filtroEstado, filtroTipo, filtrarButton, limpiarButton);

        // Panel derecho: acciones específicas del usuario
        HorizontalLayout accionesUsuario = new HorizontalLayout();
        accionesUsuario.setAlignItems(FlexComponent.Alignment.CENTER);
        accionesUsuario.setSpacing(true);
        accionesUsuario.getStyle().set("flex-wrap", "wrap");
        accionesUsuario.getStyle().set("gap", "8px");

        // Añadir botones específicos según el tipo de usuario
        if (usuarioActual instanceof Voluntario) {
            Voluntario voluntario = (Voluntario) usuarioActual;
            Long voluntarioId = voluntario.getId();

            // Botón para tareas compatibles
            Button tareasCompatiblesButton = new Button("Mis Tareas Compatibles",
                    new Icon(VaadinIcon.CONNECT));
            tareasCompatiblesButton.addClickListener(e -> {
                cargarTareasCompatiblesConVoluntario(voluntarioId);
            });
            tareasCompatiblesButton.getElement().getThemeList().add("primary");
            tareasCompatiblesButton.getStyle()
                    .set("white-space", "nowrap");

            // Botón para tareas asignadas
            Button misTareasButton = new Button("Mis Tareas Asignadas",
                    new Icon(VaadinIcon.USER_CHECK));
            misTareasButton.addClickListener(e -> {
                cargarTareasAsignadasAVoluntario(voluntarioId);
            });
            misTareasButton.getElement().getThemeList().add("primary");
            misTareasButton.getStyle()
                    .set("white-space", "nowrap");

            accionesUsuario.add(tareasCompatiblesButton, misTareasButton);
            layout.add(accionesUsuario);
        } else if (usuarioActual != null && usuarioActual.getTipoUsuario().equals("gestor")) {
            // Para gestor, mostrar botón para asignar recursos manualmente
            Button asignarRecursosButton = new Button("Asignar Recursos a Tarea", e -> abrirDialogoAsignarRecursos());
            asignarRecursosButton.getElement().getThemeList().add("primary");
            accionesUsuario.add(asignarRecursosButton);
            layout.add(accionesUsuario);
            accionesUsuario.add(asignarRecursosButton);
        } else if (usuarioActual instanceof Afectado) {
            // Para afectados, solo mostrar todas las tareas disponibles
            Button todasLasTareasButton = new Button("Todas las Tareas",
                    new Icon(VaadinIcon.LIST));
            todasLasTareasButton.addClickListener(e -> {
                refreshTareas();
            });
            todasLasTareasButton.getElement().getThemeList().add("primary");
            accionesUsuario.add(todasLasTareasButton);
        }

        layout.add(filtrosGenerales, accionesUsuario);
        return layout;
    }

    private void abrirFormularioTarea(Tarea tarea) {
        tareaActual = tarea;
        formDialog = new Dialog();
        formDialog.setWidth("800px");

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setPadding(true);
        formLayout.setSpacing(true);

        // Título del formulario
        H3 titulo = new H3(tarea.getId() == null ? "Nueva Tarea" : "Editar Tarea");
        formLayout.add(titulo);

        // Campos del formulario
        FormLayout campos = new FormLayout();
        campos.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Campo: Nombre
        TextField nombreField = new TextField("Nombre");
        nombreField.setRequired(true);
        nombreField.setWidthFull();
        binder.forField(nombreField)
                .asRequired("El nombre es requerido")
                .bind(Tarea::getNombre, Tarea::setNombre);

        // Campo: Descripción
        TextArea descripcionField = new TextArea("Descripción");
        descripcionField.setRequired(true);
        descripcionField.setWidthFull();
        descripcionField.setHeight("150px");
        binder.forField(descripcionField)
                .asRequired("La descripción es requerida")
                .bind(Tarea::getDescripcion, Tarea::setDescripcion);

        // Campo: Tipo de necesidad
        ComboBox<TipoNecesidad> tipoField = new ComboBox<>("Tipo de necesidad");
        tipoField.setItems(TipoNecesidad.values());
        tipoField.setRequired(true);
        binder.forField(tipoField)
                .asRequired("El tipo de necesidad es requerido")
                .bind(Tarea::getTipo, Tarea::setTipo);

        // Campo: Estado
        ComboBox<EstadoTarea> estadoField = new ComboBox<>("Estado");
        estadoField.setItems(EstadoTarea.values());
        estadoField.setRequired(true);
        binder.forField(estadoField)
                .asRequired("El estado es requerido")
                .bind(Tarea::getEstado, Tarea::setEstado);

        // Campo: Localización
        TextField localizacionField = new TextField("Localización");
        localizacionField.setRequired(true);
        binder.forField(localizacionField)
                .asRequired("La localización es requerida")
                .bind(Tarea::getLocalizacion, Tarea::setLocalizacion);

        // Campo: Número de voluntarios
        IntegerField voluntariosField = new IntegerField("Número de voluntarios necesarios");
        voluntariosField.setMin(1);
        voluntariosField.setValue(1);
        voluntariosField.setRequired(true);
        binder.forField(voluntariosField)
                .asRequired("El número de voluntarios es requerido")
                .bind(Tarea::getNumeroVoluntariosNecesarios, Tarea::setNumeroVoluntariosNecesarios);

        // Campos de fecha
        DateTimePicker fechaInicioField = new DateTimePicker("Fecha de inicio");
        fechaInicioField.setRequiredIndicatorVisible(true);
        binder.forField(fechaInicioField)
                .asRequired("La fecha de inicio es requerida")
                .bind(Tarea::getFechaInicio, Tarea::setFechaInicio);

        DateTimePicker fechaFinField = new DateTimePicker("Fecha de fin");
        fechaFinField.setRequiredIndicatorVisible(true);
        binder.forField(fechaFinField)
                .asRequired("La fecha de fin es requerida")
                .bind(Tarea::getFechaFin, Tarea::setFechaFin);

        // Añadir campos al formulario
        campos.add(
                nombreField, descripcionField,
                tipoField, estadoField,
                localizacionField, voluntariosField,
                fechaInicioField, fechaFinField);

        formLayout.add(campos);

        // Botones de acción
        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        botonesLayout.setWidthFull();

        Button cancelarButton = new Button("Cancelar", e -> formDialog.close());

        Button guardarButton = new Button("Guardar", e -> {
            try {
                // Si es una nueva tarea, establecer el creador
                if (tareaActual.getId() == null && usuarioActual instanceof Voluntario) {
                    tareaActual.setCreador((Voluntario) usuarioActual);
                }

                // Validar y actualizar el objeto tarea
                if (binder.writeBeanIfValid(tareaActual)) {
                    if (tareaActual.getId() == null) {
                        // Crear nueva tarea
                        Tarea tareaAct = restTemplate.postForObject(apiUrl, tareaActual, Tarea.class);
                        NotificacionDTO notificacionDTO = new NotificacionDTO();
                        notificacionDTO.setTitulo("Nueva Tarea");
                        notificacionDTO.setMensaje("Se ha creado la tarea " + tareaActual.getNombre());
                        System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
                        restTemplate.postForEntity(apiUrl + "/" + tareaAct.getId() + "/notificar", notificacionDTO, Void.class);
                        Notification.show("Tarea creada correctamente", 3000, Position.BOTTOM_START);
                    } else {
                        // Actualizar tarea existente
                        restTemplate.put(apiUrl + "/" + tareaActual.getId(), tareaActual);
                        NotificacionDTO notificacionDTO = new NotificacionDTO();
                        notificacionDTO.setTitulo("Tarea actualizada");
                        notificacionDTO.setMensaje("Se ha actualizado la tarea " + tareaActual.getNombre());
                        restTemplate.postForEntity(apiUrl + "/" + tareaActual.getId() + "/notificar", notificacionDTO, Void.class);
                        System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
                        Notification.show("Tarea actualizada correctamente", 3000, Position.BOTTOM_START);
                    }
                    formDialog.close();
                    refreshTareas();
                } else {
                    Notification.show("Por favor, complete todos los campos requeridos", 3000, Position.BOTTOM_START);
                }
            } catch (Exception ex) {
                Notification.show("Error al guardar la tarea: " + ex.getMessage(), 3000, Position.BOTTOM_START);
            }
        });
        guardarButton.getElement().getThemeList().add("primary");

        botonesLayout.add(cancelarButton, guardarButton);
        formLayout.add(botonesLayout);

        formDialog.add(formLayout);
        binder.readBean(tareaActual);
        formDialog.open();
    }

    private Component crearTarjetaTarea(Tarea tarea) {
        // Contenedor principal
        Div tarjeta = new Div();
        tarjeta.setWidth("100%");
        tarjeta.getStyle()
               .set("box-shadow", "none")
               .set("background-color", "white")
               .set("transition", "all 0.3s ease")
               .set("margin-bottom", "-50px")
               .set("border", "transparent");
    
        // Efecto hover
        tarjeta.getElement().addEventListener("mouseover", e ->
            tarjeta.getStyle().set("transform", "translateY(-5px)")
        );
        tarjeta.getElement().addEventListener("mouseout", e ->
            tarjeta.getStyle().set("transform", "translateY(0)")
        );
    
        // Layout interno
        VerticalLayout contenido = new VerticalLayout();
        contenido.setPadding(false);
        contenido.setSpacing(false);
        contenido.setWidthFull();
    
        // 1) Cabecera: título, tipo y estado
        HorizontalLayout cabecera = new HorizontalLayout();
        cabecera.setWidthFull();
        cabecera.setPadding(true);
        cabecera.getStyle()
               .set("background-color", "#f8f9fa")
               .set("border-bottom", "1px solid #eaeaea")
               .set("padding", "18px 12px")
               .set("border-radius", "12px 12px 0 0");
    
        H4 titulo = new H4(tarea.getNombre());
        titulo.getStyle().set("margin", "0");
    
        Span tipoSpan = crearBadge(tarea.getTipo() != null ? tarea.getTipo().name() : "SIN TIPO",
                                   "#F3E5F5", "#6A1B9A", "1px solid #E1BEE7");
        Span estadoSpan = crearBadge(
            tarea.getEstado() != null ? tarea.getEstado().name() : "SIN ESTADO",
            tarea.getEstado() == EstadoTarea.PREPARADA ? "#FFF3CD" :
            tarea.getEstado() == EstadoTarea.EN_CURSO ? "#D1ECF1" :
            tarea.getEstado() == EstadoTarea.FINALIZADA ? "#D4EDDA" : "#E2E3E5",
            tarea.getEstado() == EstadoTarea.PREPARADA ? "#856404" :
            tarea.getEstado() == EstadoTarea.EN_CURSO ? "#0C5460" :
            tarea.getEstado() == EstadoTarea.FINALIZADA ? "#155724" : "#383D41",
            tarea.getEstado() == EstadoTarea.PREPARADA ? "1px solid #856404" :
            tarea.getEstado() == EstadoTarea.EN_CURSO ? "1px solid #0C5460" :
            tarea.getEstado() == EstadoTarea.FINALIZADA ? "1px solid #155724" : "1px solid #383D41"
        );
    
        Div etiquetas = new Div(tipoSpan, estadoSpan);
        etiquetas.getStyle().set("margin-left", "auto");
    
        cabecera.add(titulo, etiquetas);
    
        // 2) Cuerpo: descripción e info adicional
        VerticalLayout cuerpo = new VerticalLayout();
        cuerpo.setPadding(true);
        cuerpo.getStyle().set("background-color", "#E2E3E5");
    
        Paragraph desc = new Paragraph(truncar(tarea.getDescripcion(), 100));
        desc.getStyle().set("margin-bottom", "20px");
    
        Div infoGrid = new Div();
        infoGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "1fr 1fr")
                .set("gap", "16px 24px")
                .set("padding", "16px");
    
        infoGrid.add(
            crearInfoItem(VaadinIcon.MAP_MARKER, tarea.getLocalizacion()),
            crearInfoItem(VaadinIcon.CALENDAR, 
                "Inicio: " + (tarea.getFechaInicio() != null ? formatter.format(tarea.getFechaInicio()) : "No definida") + 
                "\nFin: " + (tarea.getFechaFin() != null ? formatter.format(tarea.getFechaFin()) : "No definida")),
            crearInfoItem(VaadinIcon.USERS, tarea.getNumeroVoluntariosNecesarios() + " voluntarios")
        );
        Div recursosContainer = new Div();
        recursosContainer.getStyle()            
                             .set("margin-top", "12px")
                             .set("margin-bottom", "8px");
        try {
                List<Recursos> recursosAsignados = tarea.getRecursosAsignados();
                if (recursosAsignados != null && !recursosAsignados.isEmpty()) {
                    VerticalLayout listaLayout = new VerticalLayout();
                    listaLayout.setSpacing(false);
                    listaLayout.setPadding(false);
                    
                    Span titulorecursos = new Span("Recursos Asignados");
                    titulorecursos.getStyle()
                                .set("font-weight", "bold")
                                .set("font-size", "14px");
                                listaLayout.add(titulorecursos);

                    VerticalLayout listaRecursos = new VerticalLayout();
                    for (Recursos recurso : recursosAsignados) {
                        Span descripcionRecurso = new Span("- " + recurso.getDescripcion());
                    
                        descripcionRecurso.getStyle()
                                        .set("font-size", "12px")
                                        .set("color", "#6c757d")
                                        .set("matgin-left", "8px");
                        listaRecursos.add(descripcionRecurso);  
                    }
                    listaLayout.add(listaRecursos);
                    recursosContainer.add(listaLayout);
                } else {
                    Span sinRecursos = new Span("No hay recursos asignados");
                    sinRecursos.getStyle()
                           .set("font-size", "12px")
                           .set("color", "#6c757d");
                    recursosContainer.add(sinRecursos);
                }
        } catch (Exception ex) {
                Span error = new Span("No hay recursos asignados");
                error.getStyle()
                          .set("font-size", "12px")
                          .set("color", "#red");
                recursosContainer.add(error);
        }
        infoGrid.add(recursosContainer);
    
        cuerpo.add(desc, infoGrid);
    
        // 3) Pie: botones
        HorizontalLayout pie = new HorizontalLayout();
        pie.setWidthFull();
        pie.setPadding(true);
        pie.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        pie.getStyle().set("border-top", "1px solid #eaeaea");
    
        Button detalles = new Button("Ver Detalles", new Icon(VaadinIcon.SEARCH));
        detalles.getElement().getThemeList().add("tertiary");
        detalles.addClickListener(e -> mostrarDetallesTarea(tarea));
        pie.add(detalles);
    
        if (usuarioActual instanceof Voluntario
            && esCreador(tarea, usuarioActual)) {
            Button editar = new Button("Editar", new Icon(VaadinIcon.EDIT));
            editar.addClickListener(e -> abrirFormularioTarea(tarea));
            Button eliminar = new Button("Eliminar", new Icon(VaadinIcon.TRASH));
            eliminar.getElement().getThemeList().add("error");
            eliminar.addClickListener(e -> eliminarTarea(tarea));
            pie.add(editar, eliminar);
        }
    
        // Ensamblar tarjeta
        contenido.add(cabecera, cuerpo, pie);
        tarjeta.add(contenido);
        return tarjeta;
    }
    
    // Métodos auxiliares:
    private Span crearBadge(String text, String bg, String color, String border) {
        Span badge = new Span(text);
        badge.getStyle()
             .set("padding", "4px 10px")
             .set("border-radius", "20px")
             .set("font-size", "12px")
             .set("font-weight", "600")
             .set("background-color", bg)
             .set("color", color)
             .set("border", border);
        return badge;
    }
    
    private Component crearInfoItem(VaadinIcon iconType, String text) {
        HorizontalLayout layout = new HorizontalLayout();
        Icon icon = iconType.create();
        icon.setSize("18px");
        layout.add(icon, new Span(text));
        return layout;
    }
    
    private String truncar(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
    
    private boolean esCreador(Tarea tarea, Usuario u) {
        return tarea.getCreador() != null && tarea.getCreador().getId().equals(u.getId());
    }
    
    /**
     * Muestra un diálogo con los detalles completos de una tarea
     * 
     * @param tarea La tarea a mostrar
     */
    /**
     * Obtiene los recursos asignados a una tarea
     * 
     * @param tarea La tarea para la que se buscan los recursos
     * @return Lista de recursos asignados a la tarea
     */
    private List<Recursos> obtenerRecursosAsignados(Tarea tarea) {
        // En un caso real, esto debería obtenerse del servicio de recursos
        // Aquí simulamos la obtención de recursos para la demostración
        try {
            return restTemplate.getForObject(
                    "http://localhost:8080/api/tareas/" + tarea.getId() + "/recursos",
                    List.class);
        } catch (Exception e) {
            // Si hay un error, devolver una lista vacía
            return Collections.emptyList();
        }
    }

    /**
     * Devuelve un color para cada tipo de recurso
     * 
     * @param tipoRecurso El tipo de recurso
     * @return Un color en formato hexadecimal
     */
    private String getColorForResourceType(TipoRecurso tipoRecurso) {
        switch (tipoRecurso) {
            case PRIMEROS_AUXILIOS:
                return "#dc3545"; // Rojo
            case MEDICAMENTOS:
                return "#fd7e14"; // Naranja
            case ALIMENTACION:
                return "#28a745"; // Verde
            case ALIMENTACION_BEBE:
                return "#20c997"; // Verde azulado
            case REFUGIO:
                return "#6610f2"; // Púrpura
            case ROPA:
                return "#e83e8c"; // Rosa
            case SERVICIO_LIMPIEZA:
                return "#17a2b8"; // Cian
            case AYUDA_PSICOLOGICA:
                return "#6f42c1"; // Violeta
            case AYUDA_CARPINTERIA:
                return "#795548"; // Marrón
            case AYUDA_ELECTRICIDAD:
                return "#ffc107"; // Amarillo
            case AYUDA_FONTANERIA:
                return "#007bff"; // Azul
            case MATERIAL_HIGENE:
                return "#87ceeb"; // Azul cielo
            default:
                return "#6c757d"; // Gris
        }
    }

    private void mostrarDetallesTarea(Tarea tarea) {
        Dialog detallesDialog = new Dialog();
        detallesDialog.setWidth("800px");

        VerticalLayout contenido = new VerticalLayout();
        contenido.setPadding(true);
        contenido.setSpacing(true);

        // Título con el nombre de la tarea
        H3 titulo = new H3(tarea.getNombre());

        // Estado y tipo
        HorizontalLayout infoBar = new HorizontalLayout();
        infoBar.setWidthFull();

        Span estadoSpan = new Span("Estado: " + (tarea.getEstado() != null ? tarea.getEstado().name() : "No definido"));
        estadoSpan.getStyle()
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("margin-right", "8px");

        // Asignar color según el estado
        if (tarea.getEstado() != null) {
            switch (tarea.getEstado()) {
                case PREPARADA:
                    estadoSpan.getStyle().set("background-color", "#FFF3CD").set("color", "#856404");
                    break;
                case EN_CURSO:
                    estadoSpan.getStyle().set("background-color", "#D1ECF1").set("color", "#0C5460");
                    break;
                case FINALIZADA:
                    estadoSpan.getStyle().set("background-color", "#D4EDDA").set("color", "#155724");
                    break;
                default:
                    estadoSpan.getStyle().set("background-color", "#E2E3E5").set("color", "#383D41");
            }
        } else {
            // Estilo para cuando el estado es nulo
            estadoSpan.getStyle().set("background-color", "#E2E3E5").set("color", "#383D41");
        }

        Span tipoSpan = new Span("Tipo: " + (tarea.getTipo() != null ? tarea.getTipo().name() : "No definido"));
        tipoSpan.getStyle()
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("background-color", "#E2E3E5")
                .set("color", "#383D41");

        infoBar.add(estadoSpan, tipoSpan);

        // Descripción completa
        Div descripcionContainer = new Div();
        descripcionContainer.setWidthFull();
        descripcionContainer.getStyle()
                .set("background-color", "#f8f9fa")
                .set("padding", "16px")
                .set("border-radius", "4px")
                .set("margin", "16px 0");

        Paragraph descripcion = new Paragraph(tarea.getDescripcion());
        descripcionContainer.add(descripcion);

        // Información detallada
        FormLayout infoLayout = new FormLayout();
        infoLayout.setWidthFull();
        infoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Localización
        Span localizacion = new Span(tarea.getLocalizacion());
        infoLayout.addFormItem(localizacion, "Localización");

        // Fechas
        String fechaInicio = tarea.getFechaInicio() != null ? formatter.format(tarea.getFechaInicio()) : "No definida";
        Span fechaInicioSpan = new Span(fechaInicio);
        infoLayout.addFormItem(fechaInicioSpan, "Fecha de inicio");

        String fechaFin = tarea.getFechaFin() != null ? formatter.format(tarea.getFechaFin()) : "No definida";
        Span fechaFinSpan = new Span(fechaFin);
        infoLayout.addFormItem(fechaFinSpan, "Fecha de fin");

        // Voluntarios necesarios
        Span voluntariosNecesarios = new Span(String.valueOf(tarea.getNumeroVoluntariosNecesarios()));
        infoLayout.addFormItem(voluntariosNecesarios, "Voluntarios necesarios");

        // Creador
        String creadorNombre = tarea.getCreador() != null
                ? tarea.getCreador().getNombre() + " " + tarea.getCreador().getApellidos()
                : "No asignado";
        Span creador = new Span(creadorNombre);
        infoLayout.addFormItem(creador, "Creador");

        // Voluntarios asignados
        String voluntariosAsignados = "Sin asignar";
        if (tarea.getVoluntariosAsignados() != null && !tarea.getVoluntariosAsignados().isEmpty()) {
            voluntariosAsignados = tarea.getVoluntariosAsignados().stream()
                    .map(vol -> vol.getNombre() + " " + vol.getApellidos())
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        Span voluntarios = new Span(voluntariosAsignados);
        infoLayout.addFormItem(voluntarios, "Voluntarios asignados");

        // Botones de acción
        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setWidthFull();
        botonesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // Añadir botones específicos según el tipo de usuario y el estado de la tarea
        if (usuarioActual instanceof Voluntario) {
            boolean esCreador = tarea.getCreador() != null && tarea.getCreador().getId().equals(usuarioActual.getId());
            boolean estaAsignado = tarea.getVoluntariosAsignados() != null &&
                    tarea.getVoluntariosAsignados().stream()
                            .anyMatch(vol -> vol.getId().equals(usuarioActual.getId()));

            if (esCreador) {
                Button editarButton = new Button("Editar", e -> {
                    detallesDialog.close();
                    abrirFormulario(tarea);
                });
                Button eliminarButton = new Button("Eliminar", e -> {
                    detallesDialog.close();
                    eliminarTarea(tarea);
                });
                eliminarButton.getElement().getThemeList().add("error");
                botonesLayout.add(editarButton, eliminarButton);
            } else if (!estaAsignado && tarea.getEstado() == EstadoTarea.PREPARADA) {
                Button postularseButton = new Button("Postularme", e -> {
                    // Aquí iría la lógica para postularse a la tarea
                    Notification.show("Funcionalidad de postulación no implementada", 3000, Position.BOTTOM_START);
                });
                postularseButton.getElement().getThemeList().add("success");
                botonesLayout.add(postularseButton);
            }
        }

        Button cerrarButton = new Button("Cerrar", e -> detallesDialog.close());
        botonesLayout.add(cerrarButton);

        // Añadir todos los componentes al diálogo
        contenido.add(titulo, infoBar, descripcionContainer, infoLayout, botonesLayout);
        detallesDialog.add(contenido);

        detallesDialog.open();
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
        binder.forField(descripcionField).asRequired("La descripción es obligatoria").bind(Tarea::getDescripcion,
                Tarea::setDescripcion);

        ComboBox<TipoNecesidad> tipoField = new ComboBox<>("Tipo");
        tipoField.setItems(TipoNecesidad.values());
        tipoField.setItemLabelGenerator(Enum::name);
        binder.forField(tipoField).asRequired("El tipo es obligatorio").bind(Tarea::getTipo, Tarea::setTipo);

        TextField localizacionField = new TextField("Localización");
        binder.forField(localizacionField).asRequired("La localización es obligatoria").bind(Tarea::getLocalizacion,
                Tarea::setLocalizacion);

        IntegerField voluntariosField = new IntegerField("Número de Voluntarios");
        voluntariosField.setMin(1);
        binder.forField(voluntariosField).asRequired("El número de voluntarios es obligatorio")
                .bind(Tarea::getNumeroVoluntariosNecesarios, Tarea::setNumeroVoluntariosNecesarios);

        DateTimePicker fechaInicioField = new DateTimePicker("Fecha de Inicio");
        binder.forField(fechaInicioField).asRequired("La fecha de inicio es obligatoria").bind(Tarea::getFechaInicio,
                Tarea::setFechaInicio);

        DateTimePicker fechaFinField = new DateTimePicker("Fecha de Fin");
        binder.forField(fechaFinField).asRequired("La fecha de fin es obligatoria").bind(Tarea::getFechaFin,
                Tarea::setFechaFin);

        ComboBox<EstadoTarea> estadoField = new ComboBox<>("Estado");
        estadoField.setItems(EstadoTarea.values());
        estadoField.setItemLabelGenerator(Enum::name);
        binder.forField(estadoField).asRequired("El estado es obligatorio").bind(Tarea::getEstado, Tarea::setEstado);

        formLayout.add(nombreField, descripcionField, tipoField, localizacionField, voluntariosField, fechaInicioField,
                fechaFinField, estadoField);

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

                // Obtener el usuario actual de la sesión
                if (tareaActual.getId() == null && tareaActual.getCreador() == null) {
                    tareaActual.setCreador(usuarioActual);
                }

                if (tareaActual.getId() == null) {
                    // Crear nueva tarea
                    Tarea tarea = restTemplate.postForObject(apiUrl + "/crear", tareaActual, Tarea.class);
                    if (usuarioActual instanceof Voluntario voluntario) {
                        suscribirVoluntario(voluntario, tarea);
                        tarea = restTemplate.getForObject(apiUrl + "/" + tarea.getId(), Tarea.class);
                        NotificacionDTO notificacionDTO = new NotificacionDTO();
                        notificacionDTO.setTitulo("Nueva Tarea");
                        notificacionDTO.setMensaje("Se ha creado la tarea " + tareaActual.getNombre());
                        restTemplate.postForEntity(apiUrl + "/" + tarea.getId() + "/notificar", notificacionDTO, Void.class);
                        Notification.show("Tarea creada correctamente", 3000, Position.BOTTOM_START);
                    }
                } else {
                    // Actualizar tarea existente
                    restTemplate.put(apiUrl + "/" + tareaActual.getId(), tareaActual);
                    NotificacionDTO notificacionDTO = new NotificacionDTO();
                    notificacionDTO.setTitulo("Tarea actualizada");
                    notificacionDTO.setMensaje("Se ha actualizado la tarea " + tareaActual.getNombre());
                    restTemplate.postForEntity(apiUrl + "/" + tareaActual.getId() + "/notificar", notificacionDTO, Void.class);
                    Notification.show("Tarea actualizada correctamente", 3000, Position.BOTTOM_START);
                }
                formDialog.close();
                refreshTareas();
            } catch (Exception ex) {
                Notification.show("Error al guardar la tarea: " + ex.getMessage(), 3000, Position.BOTTOM_START);
            }
        }
    }

    private void suscribirVoluntario(Voluntario voluntario, Tarea tarea) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/tareas/";

        Map<Necesidad.TipoNecesidad, Habilidad> mapeoHabilidades = new HashMap<>();
        mapeoHabilidades.put(Necesidad.TipoNecesidad.PRIMEROS_AUXILIOS, Habilidad.PRIMEROS_AUXILIOS);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.ALIMENTACION, Habilidad.COCINA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.ALIMENTACION_BEBE, Habilidad.COCINA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.SERVICIO_LIMPIEZA, Habilidad.LIMPIEZA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_PSICOLOGICA, Habilidad.AYUDA_PSICOLOGICA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_CARPINTERIA, Habilidad.CARPINTERIA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_ELECTRICIDAD, Habilidad.ELECTICISTA);
        mapeoHabilidades.put(Necesidad.TipoNecesidad.AYUDA_FONTANERIA, Habilidad.FONTANERIA);

        Habilidad habilidadRequerida = mapeoHabilidades.get(tarea.getTipo());

        if (voluntario.getHabilidades().contains(habilidadRequerida)) {
            restTemplate.postForEntity(url + tarea.getId() + "/suscribir/" + voluntario.getId(), null, Void.class);
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
                refreshTareas();
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

    private void refreshTareas() {
        try {
            // Usar un tipo más específico para la deserialización
            Tarea[] tareasArray = restTemplate.getForObject(apiUrl, Tarea[].class);
            if (tareasArray != null) {
                List<Tarea> tareas = Arrays.asList(tareasArray);

                // Validar y asegurar que no hay tareas con campos críticos nulos
                for (Tarea tarea : tareas) {
                    // Asegurar que el estado nunca sea nulo
                    if (tarea.getEstado() == null) {
                        tarea.setEstado(EstadoTarea.PREPARADA); // Valor por defecto
                    }

                    // Asegurar que otros campos críticos no sean nulos
                    if (tarea.getNombre() == null)
                        tarea.setNombre("Sin nombre");
                    if (tarea.getDescripcion() == null)
                        tarea.setDescripcion("Sin descripción");
                    if (tarea.getLocalizacion() == null)
                        tarea.setLocalizacion("Sin localización");
                }

                mostrarTareas(tareas);
            } else {
                mostrarTareas(Collections.emptyList());
                System.err.println("La respuesta del servidor fue nula al cargar tareas");
            }
        } catch (Exception ex) {
            Notification.show("Error al cargar las tareas: " + ex.getMessage(), 3000, Position.BOTTOM_START);
            System.err.println("Error completo al cargar tareas: " + ex);
            ex.printStackTrace();
            // Mostrar una lista vacía para evitar que la interfaz se rompa
            mostrarTareas(Collections.emptyList());
        }
    }

    /**
     * Muestra las tareas en el contenedor de tarjetas
     * 
     * @param tareas Lista de tareas a mostrar
     */
    private void mostrarTareas(List<Tarea> tareas) {
        // Limpiar el contenedor
        tareasContainer.removeAll();

        if (tareas.isEmpty()) {
            Div mensajeVacio = new Div();
            mensajeVacio.setText("No hay tareas disponibles");
            mensajeVacio.getStyle()
                    .set("padding", "20px")
                    .set("text-align", "center")
                    .set("width", "100%")
                    .set("color", "#6c757d");
            tareasContainer.add(mensajeVacio);
            return;
        }

        // Crear una tarjeta para cada tarea
        for (Tarea tarea : tareas) {
            tareasContainer.add(crearTarjetaTarea(tarea));
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

            // Usar un tipo más específico para la deserialización
            Tarea[] tareasArray = restTemplate.getForObject(url, Tarea[].class);
            if (tareasArray != null) {
                List<Tarea> tareas = Arrays.asList(tareasArray);

                // Validar y asegurar que no hay tareas con campos críticos nulos
                for (Tarea tarea : tareas) {
                    // Asegurar que el estado nunca sea nulo
                    if (tarea.getEstado() == null) {
                        tarea.setEstado(EstadoTarea.PREPARADA); // Valor por defecto
                    }

                    // Asegurar que otros campos críticos no sean nulos
                    if (tarea.getNombre() == null)
                        tarea.setNombre("Sin nombre");
                    if (tarea.getDescripcion() == null)
                        tarea.setDescripcion("Sin descripción");
                    if (tarea.getLocalizacion() == null)
                        tarea.setLocalizacion("Sin localización");
                }

                mostrarTareas(tareas);
            } else {
                mostrarTareas(Collections.emptyList());
                System.err.println("La respuesta del servidor fue nula al filtrar tareas");
            }
        } catch (Exception ex) {
            Notification.show("Error al filtrar las tareas: " + ex.getMessage(), 3000, Position.BOTTOM_START);
            System.err.println("Error completo al filtrar tareas: " + ex);
            ex.printStackTrace();
            // Mostrar una lista vacía para evitar que la interfaz se rompa
            mostrarTareas(Collections.emptyList());
        }
    }

    /**
     * Método para cargar las tareas compatibles con las habilidades de un
     * voluntario específico
     * 
     * @param voluntarioId ID del voluntario
     */
    private void cargarTareasCompatiblesConVoluntario(Long voluntarioId) {
        try {
            String url = apiUrl + "/compatibles/voluntario/" + voluntarioId;

            // Usar un tipo más específico para la deserialización
            Tarea[] tareasArray = restTemplate.getForObject(url, Tarea[].class);
            if (tareasArray != null) {
                List<Tarea> tareas = Arrays.asList(tareasArray);

                // Validar y asegurar que no hay tareas con campos críticos nulos
                for (Tarea tarea : tareas) {
                    // Asegurar que el estado nunca sea nulo
                    if (tarea.getEstado() == null) {
                        tarea.setEstado(EstadoTarea.PREPARADA); // Valor por defecto
                    }

                    // Asegurar que otros campos críticos no sean nulos
                    if (tarea.getNombre() == null)
                        tarea.setNombre("Sin nombre");
                    if (tarea.getDescripcion() == null)
                        tarea.setDescripcion("Sin descripción");
                    if (tarea.getLocalizacion() == null)
                        tarea.setLocalizacion("Sin localización");
                }

                mostrarTareas(tareas);
                Notification.show("Se han cargado " + tareas.size() + " tareas compatibles con tus habilidades",
                        3000, Position.BOTTOM_START);
            } else {
                mostrarTareas(Collections.emptyList());
                Notification.show("No se encontraron tareas compatibles con tus habilidades",
                        3000, Position.BOTTOM_START);
                System.err.println("La respuesta del servidor fue nula al cargar tareas compatibles");
            }
        } catch (Exception ex) {
            Notification.show("Error al cargar las tareas compatibles: " + ex.getMessage(), 3000,
                    Position.BOTTOM_START);
            System.err.println("Error completo al cargar tareas compatibles: " + ex);
            ex.printStackTrace();
            // Mostrar una lista vacía para evitar que la interfaz se rompa
            mostrarTareas(Collections.emptyList());
        }
    }

    /**
     * Método para cargar las tareas asignadas a un voluntario específico
     * 
     * @param voluntarioId ID del voluntario
     */
    private void cargarTareasAsignadasAVoluntario(Long voluntarioId) {
        try {
            String url = apiUrl + "/voluntario/" + voluntarioId;

            // Usar un tipo más específico para la deserialización
            Tarea[] tareasArray = restTemplate.getForObject(url, Tarea[].class);
            if (tareasArray != null) {
                List<Tarea> tareas = Arrays.asList(tareasArray);

                // Validar y asegurar que no hay tareas con campos críticos nulos
                for (Tarea tarea : tareas) {
                    // Asegurar que el estado nunca sea nulo
                    if (tarea.getEstado() == null) {
                        tarea.setEstado(EstadoTarea.PREPARADA); // Valor por defecto
                    }

                    // Asegurar que otros campos críticos no sean nulos
                    if (tarea.getNombre() == null)
                        tarea.setNombre("Sin nombre");
                    if (tarea.getDescripcion() == null)
                        tarea.setDescripcion("Sin descripción");
                    if (tarea.getLocalizacion() == null)
                        tarea.setLocalizacion("Sin localización");
                }

                mostrarTareas(tareas);
                Notification.show("Se han cargado " + tareas.size() + " tareas asignadas a ti",
                        3000, Position.BOTTOM_START);
            } else {
                mostrarTareas(Collections.emptyList());
                Notification.show("No tienes tareas asignadas actualmente",
                        3000, Position.BOTTOM_START);
                System.err.println("La respuesta del servidor fue nula al cargar tareas asignadas");
            }
        } catch (Exception ex) {
            Notification.show("Error al cargar las tareas asignadas: " + ex.getMessage(), 3000, Position.BOTTOM_START);
            System.err.println("Error completo al cargar tareas asignadas: " + ex);
            ex.printStackTrace();
            // Mostrar una lista vacía para evitar que la interfaz se rompa
            mostrarTareas(Collections.emptyList());
        }
    }

    // Al final de la clase, agregar el método para el gestor
    private void abrirDialogoAsignarRecursos() {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        H3 titulo = new H3("Asignar Recursos a Tarea");
        titulo.getStyle().set("margin-top", "0").set("color", "#3498db");

        FormLayout formLayout = new FormLayout();
        ComboBox<Tarea> tareaCombo = new ComboBox<>("Tarea");
        tareaCombo.setItems(obtenerTodasLasTareas());
        tareaCombo.setItemLabelGenerator(Tarea::getNombre);

        ComboBox<Recursos> recursoCombo = new ComboBox<>("Recurso");
        List<Recursos> recursosNoAsignados = obtenerRecursosNoAsignados();
        recursoCombo.setItems(recursosNoAsignados);
        recursoCombo.setItemLabelGenerator(Recursos::getDescripcion);

        // Verificar si hay recursos disponibles
        if (recursosNoAsignados.isEmpty()) {
            // Mostrar un mensaje en el propio diálogo
            Label mensaje = new Label("No hay recursos disponibles para asignar");
            mensaje.getStyle().set("color", "red");
            formLayout.add(mensaje);

            // Deshabilitar el comboBox y el botón de asignar
            recursoCombo.setEnabled(false);
        }

        formLayout.add(tareaCombo, recursoCombo);

        Button asignarButton = new Button("Asignar", event -> {
            Tarea tareaSeleccionada = tareaCombo.getValue();
            Recursos recursoSeleccionado = recursoCombo.getValue();
            if (tareaSeleccionada != null && recursoSeleccionado != null) {
                try {
                    recursoSeleccionado.setTareaAsignada(tareaSeleccionada);
                    recursoSeleccionado.setEstado(Recursos.EstadoRecurso.ASIGNADO); // Cambiar estado a 'asignado'
                    restTemplate.put("http://localhost:8080/api/recursos/" + recursoSeleccionado.getId(),
                            recursoSeleccionado);
                    Notification.show("Recurso asignado correctamente");
                    dialog.close();
                    refreshTareas();
                } catch (Exception ex) {
                    Notification.show("Error al asignar el recurso: " + ex.getMessage());
                }
            } else {
                Notification.show("Seleccione una tarea y un recurso");
            }
        });
        asignarButton.getStyle().set("background-color", "#3498db").set("color", "white");

        Button cancelarButton = new Button("Cancelar", event -> dialog.close());
        HorizontalLayout botonesLayout = new HorizontalLayout(asignarButton, cancelarButton);
        botonesLayout.setWidthFull();
        botonesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(titulo, formLayout, botonesLayout);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private List<Tarea> obtenerTodasLasTareas() {
        try {
            Tarea[] tareasArray = restTemplate.getForObject(apiUrl, Tarea[].class);
            return tareasArray != null ? Arrays.asList(tareasArray) : Collections.emptyList();
        } catch (Exception e) {
            Notification.show("Error al cargar las tareas: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Recursos> obtenerRecursosNoAsignados() {
        try {
            List<Recursos> recursos = restTemplate.exchange(
                    "http://localhost:8080/api/recursos",
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new org.springframework.core.ParameterizedTypeReference<List<Recursos>>() {
                    }).getBody();
            if (recursos != null) {
                return recursos.stream().filter(r -> r.getTareaAsignada() == null).collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            Notification.show("Error al cargar los recursos: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
