package SolidarityHub.views;

import SolidarityHub.models.dtos.TareaPorMesDTO;
import SolidarityHub.services.TareaServicio;
import SolidarityHub.models.dtos.DashboardMetricasDTO;
import SolidarityHub.services.UsuarioServicio;
import SolidarityHub.strategy.ContextoMetricas;
import SolidarityHub.strategy.DashboardEstado;
import SolidarityHub.strategy.DashboardTipo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.combobox.ComboBox;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | SolidarityHub")
public class DashboardView extends VerticalLayout {

    private final TareaServicio tareaServicio;
    private final DashboardMetricasDTO metricasDashboard;
    private final ContextoMetricas contextoMetricas;

    public DashboardView(UsuarioServicio usuarioServicio, TareaServicio tareaServicio) {
        this.tareaServicio = tareaServicio;
        this.metricasDashboard = tareaServicio.obtenerMetricasDashboardEstado();
        this.contextoMetricas = new ContextoMetricas();

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        addClassName(LumoUtility.Background.CONTRAST_5);

        // Header section with title and subtitle
        createHeader();
    }

    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.addClassNames(
                LumoUtility.Background.PRIMARY,
                LumoUtility.TextColor.PRIMARY_CONTRAST,
                LumoUtility.Padding.Vertical.LARGE);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        VerticalLayout headerContent = new VerticalLayout();
        headerContent.setSpacing(false);
        headerContent.setPadding(false);
        headerContent.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 title = new H2("Panel de Control de SolidarityHub");
        title.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.Margin.NONE);

        Paragraph subtitle = new Paragraph("Resumen de actividades y métricas de tareas solidarias");
        subtitle.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.Top.SMALL,
                LumoUtility.TextColor.PRIMARY_CONTRAST);

        headerContent.add(title, subtitle);

        ComboBox<String> comboBox = new ComboBox<>("Filtrar por:", "Tipo", "Estado");
        comboBox.setValue("Tipo");

        header.add(headerContent, comboBox);
        add(header);

        contextoMetricas.setEstrategia(new DashboardTipo(metricasDashboard));
        add(contextoMetricas.ejecutarEstrategia(metricasDashboard));

        comboBox.addValueChangeListener(e -> {
            // Remove previous dashboard content
            removeAll();
            headerContent.add(title, subtitle);
            header.add(headerContent, comboBox);
            add(header);

            if (comboBox.getValue().equals("Tipo")) {
                contextoMetricas.setEstrategia(new DashboardTipo(metricasDashboard));
                add(contextoMetricas.ejecutarEstrategia(metricasDashboard));
            } else if (comboBox.getValue().equals("Estado")) {
                contextoMetricas.setEstrategia(new DashboardEstado(metricasDashboard));
                add(contextoMetricas.ejecutarEstrategia(metricasDashboard));
            }
        });
    }
}
