package SolidarityHub.views;

import SolidarityHub.models.Tarea;
import SolidarityHub.models.Usuario;
import SolidarityHub.models.Voluntario;
import SolidarityHub.models.Afectado;
import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea.EstadoTarea;
import SolidarityHub.models.Recursos;
import SolidarityHub.models.Recursos.TipoRecurso;

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
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "tareas", layout = MainLayout.class)
@PageTitle("Tareas | SolidarityHub")
public class TareasView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080/api/tareas";
    private final Binder<Tarea> binder = new Binder<>(Tarea.class);
    private Dialog formDialog;
    private Tarea tareaActual;
    private Usuario usuarioActual;
    private VerticalLayout tareasContainer;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TareasView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setHeight("auto");

        // Obtener el usuario actual de la sesión
        usuarioActual = (Usuario) VaadinSession.getCurrent().getAttribute("usuario");

        add(crearTitulo());
        add(crearFiltros());

        // Solo mostrar el botón de nueva tarea si el usuario es un voluntario
        if (usuarioActual instanceof Voluntario) {
            add(crearBotonNuevo());
        }

        // Crear el contenedor para las tarjetas de tareas
        tareasContainer = new VerticalLayout();
        tareasContainer.setWidthFull();
        tareasContainer.getStyle().set("flex-wrap", "wrap");
        tareasContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        tareasContainer.getStyle().set("flex-wrap", "wrap");
        tareasContainer.getStyle().set("gap", "16px");

        add(tareasContainer);

        refreshTareas();
    }

    private Component crearTitulo() {
        H3 titulo = new H3("Gestión de Tareas");
        titulo.getStyle().set("margin", "0");
        return titulo;
    }

    private Component crearFiltros() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);

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
        filtrarButton.getElement().getThemeList().add("primary");

        Button limpiarButton = new Button("Limpiar", e -> {
            filtroEstado.clear();
            filtroTipo.clear();
            refreshTareas();
        });

        layout.add(filtroEstado, filtroTipo, filtrarButton, limpiarButton);

        VerticalLayout layoutUsuario = new VerticalLayout();
        layoutUsuario.setWidthFull();
        layoutUsuario.setAlignItems(FlexComponent.Alignment.CENTER);
        layoutUsuario.setPadding(true);
        layoutUsuario.setHeight("20%");
        layoutUsuario.setWidth("20%");
        // Añadir botones específicos según el tipo de usuario
        if (usuarioActual instanceof Voluntario) {
            Voluntario voluntario = (Voluntario) usuarioActual;
            Long voluntarioId = voluntario.getId();

            // Botón para cargar tareas compatibles con las habilidades del voluntario actual
            Button tareasCompatiblesButton = new Button("Mis Tareas Compatibles", e -> {
                cargarTareasCompatiblesConVoluntario(voluntarioId);
            });
            tareasCompatiblesButton.getElement().getThemeList().add("primary");

            // Botón para cargar tareas ya asignadas al voluntario actual
            Button misTareasButton = new Button("Mis Tareas Asignadas", e -> {
                cargarTareasAsignadasAVoluntario(voluntarioId);
            });
            misTareasButton.getElement().getThemeList().add("primary");

            layoutUsuario.add(tareasCompatiblesButton, misTareasButton);
            layout.add(layoutUsuario);
        } else if (usuarioActual != null && usuarioActual.getTipoUsuario().equals("gestor")) {
            // Para gestor, mostrar botón para asignar recursos manualmente
            Button asignarRecursosButton = new Button("Asignar Recursos a Tarea", e -> abrirDialogoAsignarRecursos());
            asignarRecursosButton.getElement().getThemeList().add("primary");
            layoutUsuario.add(asignarRecursosButton);
            layout.add(layoutUsuario);
        }

        return layout;
    }

    private Button crearBotonNuevo() {
        Button nuevaTarea = new Button("Nueva Tarea", e -> abrirFormulario(new Tarea()));
        nuevaTarea.getElement().getThemeList().add("primary");
        return nuevaTarea;
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
                        restTemplate.postForObject(apiUrl, tareaActual, Tarea.class);
                        Notification.show("Tarea creada correctamente", 3000, Position.BOTTOM_START);
                    } else {
                        // Actualizar tarea existente
                        restTemplate.put(apiUrl + "/" + tareaActual.getId(), tareaActual);
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

    /**
     * Crea una tarjeta para mostrar una tarea en estilo Pinterest
     * 
     * @param tarea La tarea a mostrar
     * @return El componente de la tarjeta
     */
    private Component crearTarjetaTarea(Tarea tarea) {
        // Crear el contenedor principal de la tarjeta
        Div tarjeta = new Div();
        tarjeta.setWidthFull();
        tarjeta.getStyle()
                .set("justifyContentMode", "AUTO")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)")
                .set("overflow", "hidden")
                .set("background-color", "white")
                .set("transition", "transform 0.3s ease")
                .set("margin-bottom", "16px");

        // Añadir efecto hover
        tarjeta.getElement().addEventListener("mouseover",
                e -> tarjeta.getStyle().set("transform", "translateY(-5px)"));
        tarjeta.getElement().addEventListener("mouseout", e -> tarjeta.getStyle().set("transform", "translateY(0)"));

        // Contenedor para el contenido de la tarjeta
        VerticalLayout estructura = new VerticalLayout();
        estructura.setWidthFull();
        estructura.setSpacing(true);
        estructura.setPadding(true);

        H4 titulo = new H4(tarea.getNombre());
titulo.getStyle().set("margin", "0").set("margin-top", "8px").setWidth("700px");

        HorizontalLayout contenido = new HorizontalLayout();
        contenido.setPadding(true);
        contenido.setSpacing(true);
        contenido.setAlignItems(FlexComponent.Alignment.CENTER);
        contenido.setWidthFull();

        HorizontalLayout layoutTitulo = new HorizontalLayout();
        Div divisortitulo = new Div();
        divisortitulo.setWidthFull();
        layoutTitulo.setWidthFull();
        layoutTitulo.setFlexGrow(1, divisortitulo);
        layoutTitulo.setSpacing(true);

        // Mostrar el estado con un color distintivo
        Span estadoSpan = new Span(tarea.getEstado() != null ? tarea.getEstado().name() : "SIN ESTADO");
        estadoSpan.getStyle()
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("font-size", "12px")
                .set("font-weight", "bold");

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
            estadoSpan.getStyle().set("background-color", "#E2E3E5").set("color", "#383D41");
        }

        Span tipoSpan = new Span(tarea.getTipo() != null ? tarea.getTipo().name() : "SIN TIPO");
        tipoSpan.getStyle()
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("font-size", "12px")
                .set("background-color", "#E2E3E5")
                .set("color", "#383D41");

        layoutTitulo.add(titulo, divisortitulo,estadoSpan, tipoSpan);

        // Descripción de la tarea (limitada a 100 caracteres)
        String descripcionCorta = tarea.getDescripcion();
        if (descripcionCorta != null && descripcionCorta.length() > 100) {
            descripcionCorta = descripcionCorta.substring(0, 97) + "...";
        }
        Paragraph descripcion = new Paragraph(descripcionCorta);
        descripcion.getStyle().set("color", "#6c757d").set("margin", "8px 0");

        // Información adicional
        Div infoContainer = new Div();
        infoContainer.setWidthFull();
        infoContainer.getStyle().set("margin-top", "8px");

        // Localización
        HorizontalLayout localizacionLayout = new HorizontalLayout();
        localizacionLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        localizacionLayout.setSpacing(false);
        Icon locIcon = VaadinIcon.MAP_MARKER.create();
        locIcon.setSize("14px");
        locIcon.getStyle().set("color", "#6c757d");
        Span localizacion = new Span(tarea.getLocalizacion());
        localizacion.getStyle().set("font-size", "14px").set("color", "#6c757d");
        localizacionLayout.add(locIcon, localizacion);

        // Fechas
        HorizontalLayout fechasLayout = new HorizontalLayout();
        fechasLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        fechasLayout.setSpacing(false);
        Icon calendarIcon = VaadinIcon.CALENDAR.create();
        calendarIcon.setSize("14px");
        calendarIcon.getStyle().set("color", "#6c757d");

        String fechasTexto = "";
        if (tarea.getFechaInicio() != null) {
            fechasTexto = formatter.format(tarea.getFechaInicio());
            if (tarea.getFechaFin() != null) {
                fechasTexto += " - " + formatter.format(tarea.getFechaFin());
            }
        }

        Span fechas = new Span(fechasTexto);
        fechas.getStyle().set("font-size", "14px").set("color", "#6c757d");
        fechasLayout.add(calendarIcon, fechas);

        // Voluntarios necesarios
        HorizontalLayout voluntariosLayout = new HorizontalLayout();
        voluntariosLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        voluntariosLayout.setSpacing(false);
        Icon peopleIcon = VaadinIcon.USERS.create();
        peopleIcon.setSize("14px");
        peopleIcon.getStyle().set("color", "#6c757d");
        Span voluntarios = new Span("Voluntarios: " + tarea.getNumeroVoluntariosNecesarios());
        voluntarios.getStyle().set("font-size", "14px").set("color", "#6c757d");
        voluntariosLayout.add(peopleIcon, voluntarios);

        // Creador de la tarea
        HorizontalLayout creadorLayout = new HorizontalLayout();
        creadorLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        creadorLayout.setSpacing(false);
        Icon creadorIcon = VaadinIcon.USER.create();
        creadorIcon.setSize("14px");
        creadorIcon.getStyle().set("color", "#6c757d");
        String nombreCreador = tarea.getCreador() != null
                ? tarea.getCreador().getNombre() + " " + tarea.getCreador().getApellidos()
                : "No asignado";
        Span creador = new Span("Creador: " + nombreCreador);
        creador.getStyle().set("font-size", "14px").set("color", "#6c757d");
        creadorLayout.add(creadorIcon, creador);

        // Añadir la información al contenedor
        VerticalLayout infoLayout = new VerticalLayout(localizacionLayout, fechasLayout, voluntariosLayout,
                creadorLayout);
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);
        infoContainer.add(infoLayout);

        // Botones de acción
        VerticalLayout botonesLayout = new VerticalLayout();
        botonesLayout.setWidthFull();
        botonesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button verDetallesButton = new Button("Ver Detalles", e -> mostrarDetallesTarea(tarea));
        verDetallesButton.getElement().getThemeList().add("primary");

        botonesLayout.add(verDetallesButton);

        // Añadir botones específicos según el tipo de usuario y el estado de la tarea
        if (usuarioActual instanceof Voluntario) {
            // Si es el creador o está asignado, mostrar botones de edición/eliminación
            boolean esCreador = tarea.getCreador() != null && tarea.getCreador().getId().equals(usuarioActual.getId());
            boolean estaAsignado = tarea.getVoluntariosAsignados() != null &&
                    tarea.getVoluntariosAsignados().stream()
                            .anyMatch(vol -> vol.getId().equals(usuarioActual.getId()));

            if (esCreador) {
                Button editarButton = new Button("Editar", e -> abrirFormulario(tarea));
                Button eliminarButton = new Button("Eliminar", e -> eliminarTarea(tarea));
                eliminarButton.getElement().getThemeList().add("error");
                botonesLayout.add(editarButton, eliminarButton);
            } else if (!estaAsignado && tarea.getEstado() == EstadoTarea.PREPARADA) {
                // Si no está asignado y la tarea está pendiente, mostrar botón para postularse
                Button postularseButton = new Button("Postularme", e -> {
                    // Aquí iría la lógica para postularse a la tarea
                    Notification.show("Funcionalidad de postulación no implementada", 3000, Position.BOTTOM_START);
                });
                postularseButton.getElement().getThemeList().add("success");
                botonesLayout.add(postularseButton);
            }
        }

        // Añadir barra de progreso de recursos
        Div progressBarContainer = new Div();
        progressBarContainer.setWidthFull();
        progressBarContainer.getStyle()
                .set("margin-top", "12px")
                .set("margin-bottom", "8px");

        // Obtener los recursos asignados a esta tarea
        List<Recursos> recursosAsignados = obtenerRecursosAsignados(tarea);

        if (!recursosAsignados.isEmpty()) {
            // Agrupar recursos por tipo
            Map<TipoRecurso, Long> recursosPorTipo = recursosAsignados.stream()
                    .collect(Collectors.groupingBy(Recursos::getTipoRecurso, Collectors.counting()));

            // Crear una barra de progreso para cada tipo de recurso
            for (Map.Entry<TipoRecurso, Long> entry : recursosPorTipo.entrySet()) {
                String tipoRecurso = entry.getKey().name();
                long cantidad = entry.getValue();

                // Contenedor para la etiqueta y la barra
                HorizontalLayout progressLayout = new HorizontalLayout();
                progressLayout.setWidthFull();
                progressLayout.setSpacing(false);
                progressLayout.setPadding(false);
                progressLayout.setAlignItems(FlexComponent.Alignment.CENTER);

                // Etiqueta del tipo de recurso
                Span tipoLabel = new Span(tipoRecurso + ": " + cantidad);
                tipoLabel.getStyle()
                        .set("font-size", "12px")
                        .set("min-width", "120px");

                // Barra de progreso
                Div progressBar = new Div();
                progressBar.setWidthFull();
                progressBar.setHeight("8px");
                progressBar.getStyle()
                        .set("background-color", "#e9ecef")
                        .set("border-radius", "4px")
                        .set("overflow", "hidden");

                // Parte llena de la barra
                Div progressFill = new Div();
                progressFill.setHeight("100%");
                // Ancho basado en la cantidad (máximo 100%)
                int widthPercent = Math.min((int) (cantidad * 20), 100); // 20% por cada recurso, máximo 100%
                progressFill.setWidth(widthPercent + "%");
                progressFill.getStyle()
                        .set("background-color", getColorForResourceType(entry.getKey()))
                        .set("border-radius", "4px");

                progressBar.add(progressFill);
                progressLayout.add(tipoLabel, progressBar);
                progressBarContainer.add(progressLayout);
            }
        } else {
            // Si no hay recursos asignados, mostrar un mensaje
            Span noRecursosLabel = new Span("No hay recursos asignados");
            noRecursosLabel.getStyle()
                    .set("font-size", "12px")
                    .set("color", "#6c757d")
                    .set("display", "block")
                    .set("text-align", "center");
            progressBarContainer.add(noRecursosLabel);
        }

        // Añadir todos los componentes a la tarjeta
        contenido.add(descripcion, infoContainer, progressBarContainer, botonesLayout);
        estructura.add(layoutTitulo, contenido);
        tarjeta.add(estructura);

        return tarjeta;
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
                    restTemplate.postForObject(apiUrl + "/crear", tareaActual, Tarea.class);
                    Notification.show("Tarea creada correctamente", 3000, Position.BOTTOM_START);
                } else {
                    // Actualizar tarea existente
                    restTemplate.put(apiUrl + "/" + tareaActual.getId(), tareaActual);
                    Notification.show("Tarea actualizada correctamente", 3000, Position.BOTTOM_START);
                }
                formDialog.close();
                refreshTareas();
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
        recursoCombo.setItems(obtenerRecursosNoAsignados());
        recursoCombo.setItemLabelGenerator(Recursos::getDescripcion);
    
        formLayout.add(tareaCombo, recursoCombo);
    
        Button asignarButton = new Button("Asignar", event -> {
            Tarea tareaSeleccionada = tareaCombo.getValue();
            Recursos recursoSeleccionado = recursoCombo.getValue();
            if (tareaSeleccionada != null && recursoSeleccionado != null) {
                try {
                    recursoSeleccionado.setTareaAsignada(tareaSeleccionada);
                    recursoSeleccionado.setEstado(Recursos.EstadoRecurso.ASIGNADO); // Cambiar estado a 'asignado'
                    restTemplate.put("http://localhost:8080/api/recursos/" + recursoSeleccionado.getId(), recursoSeleccionado);
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
