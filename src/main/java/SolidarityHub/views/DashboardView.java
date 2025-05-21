package SolidarityHub.views;

import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.dtos.TareaPorMesDTO;
import SolidarityHub.services.TareaServicio;
import SolidarityHub.services.UsuarioServicio;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | SolidarityHub")
public class DashboardView extends VerticalLayout {

    private final TareaServicio tareaServicio;
    private final TareaServicio.DashboardMetricasDTO metricasDashboard;

    public DashboardView(UsuarioServicio usuarioServicio, TareaServicio tareaServicio) {
        this.tareaServicio = tareaServicio;
        this.metricasDashboard = tareaServicio.obtenerMetricasDashboard(); // Fetch metrics once

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
        getStyle().set("gap", "2em");

        add(new H2("Panel de Control de SolidarityHub"));

        // KPI Section
        add(createKPIsLayout());

        // Layout para los gráficos
        FlexLayout chartsLayout = new FlexLayout();
        chartsLayout.setWidthFull();
        chartsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        chartsLayout.setFlexWrap(FlexWrap.WRAP);

        // Crear y añadir gráficos
        Chart tasksByMonthChart = createTasksByMonthChart();
        if (tasksByMonthChart != null) {
            VerticalLayout chart1Layout = new VerticalLayout(new H3("Tareas por Mes (Nombre)"), tasksByMonthChart);
            chart1Layout.setAlignItems(Alignment.CENTER);
            chartsLayout.add(chart1Layout);
        }

        Chart tasksByStatusChart = createTaskStatusDistributionChart();
        if (tasksByStatusChart != null) {
            VerticalLayout chart2Layout = new VerticalLayout(new H3("Distribución de Estados de Tareas"), tasksByStatusChart);
            chart2Layout.setAlignItems(Alignment.CENTER);
            chartsLayout.add(chart2Layout);
        }

        Chart tasksByTypeChart = createTaskTypeDistributionChart();
        if (tasksByTypeChart != null) {
            VerticalLayout chart3Layout = new VerticalLayout(new H3("Distribución de Tipos de Tareas"), tasksByTypeChart);
            chart3Layout.setAlignItems(Alignment.CENTER);
            chartsLayout.add(chart3Layout);
        }

        add(chartsLayout);
    }

    private HorizontalLayout createKPIsLayout() {
        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);
        kpiLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);
        kpiLayout.addClassName(LumoUtility.Padding.MEDIUM);
        kpiLayout.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-radius", "var(--lumo-border-radius-l)");

        kpiLayout.add(
                createKPICard("Total Tareas", String.valueOf(metricasDashboard.totalTareas)),
                createKPICard("Completadas", String.valueOf(metricasDashboard.tareasCompletadas)),
                createKPICard("En Curso", String.valueOf(metricasDashboard.tareasEnCurso)),
                createKPICard("Pendientes", String.valueOf(metricasDashboard.tareasPendientes)),
                createKPICard("Promedio/Mes", String.format("%.2f", metricasDashboard.promedioPorMes))
        );
        return kpiLayout;
    }

    private VerticalLayout createKPICard(String title, String value) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(Alignment.CENTER);
        card.setSpacing(false);
        card.setPadding(false);

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.SEMIBOLD);

        card.add(titleSpan, valueSpan);
        return card;
    }

    private Chart createTasksByMonthChart() {
        List<TareaPorMesDTO> metricas = metricasDashboard.datosPorMes;
        if (metricas == null || metricas.isEmpty()) {
            return null; // O un gráfico vacío con mensaje
        }

        Chart chart = new Chart(ChartType.COLUMN);
        Configuration conf = chart.getConfiguration();

        // Categorías (meses)
        String[] mesesNombres = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        List<String> categories = metricas.stream()
                .map(m -> mesesNombres[m.getMes() - 1])
                .distinct()
                .sorted((m1, m2) -> {
                    int index1 = List.of(mesesNombres).indexOf(m1);
                    int index2 = List.of(mesesNombres).indexOf(m2);
                    return Integer.compare(index1, index2);
                })
                .collect(Collectors.toList());
        conf.getxAxis().setCategories(categories.toArray(new String[0]));

        // Series (nombre de tarea)
        Map<String, List<TareaPorMesDTO>> tareasPorNombre = metricas.stream()
                .collect(Collectors.groupingBy(TareaPorMesDTO::getNombre));

        for (Map.Entry<String, List<TareaPorMesDTO>> entry : tareasPorNombre.entrySet()) {
            ListSeries series = new ListSeries(entry.getKey());
            Number[] data = new Number[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                String currentMonthCategory = categories.get(i);
                Optional<TareaPorMesDTO> metricForMonth = entry.getValue().stream()
                        .filter(m -> mesesNombres[m.getMes() - 1].equals(currentMonthCategory))
                        .findFirst();
                data[i] = metricForMonth.map(TareaPorMesDTO::getCantidad).orElse(0L);
            }
            series.setData(data);
            conf.addSeries(series);
        }

        conf.setTitle(""); // El título se maneja fuera
        conf.getyAxis().setTitle("Cantidad de Tareas");
        conf.getLegend().setEnabled(true);

        chart.setHeight("400px");
        chart.setWidth("600px");
        return chart;
    }

    private Chart createTaskStatusDistributionChart() {
        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("");

        DataSeries series = new DataSeries("Estados de Tareas");
        if (metricasDashboard.tareasCompletadas > 0) {
            series.add(new DataSeriesItem("Finalizada", metricasDashboard.tareasCompletadas));
        }
        if (metricasDashboard.tareasEnCurso > 0) {
            series.add(new DataSeriesItem("En Curso", metricasDashboard.tareasEnCurso));
        }
        if (metricasDashboard.tareasPendientes > 0) {
            series.add(new DataSeriesItem("Preparada", metricasDashboard.tareasPendientes));
        }
        
        if (series.getData().isEmpty()) return null; // No data to show

        conf.addSeries(series);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);
        conf.setPlotOptions(plotOptions);

        chart.setHeight("400px");
        chart.setWidth("450px");
        return chart;
    }

    private Chart createTaskTypeDistributionChart() {
        // This method still uses tareaServicio.listarTareas() as per original logic
        // If this needs to change to use DashboardMetricasDTO, further info on DTO structure is needed.
        List<Tarea> tareas = tareaServicio.listarTareas();
        if (tareas == null || tareas.isEmpty()) {
            return null;
        }

        Map<TipoNecesidad, Long> typeCounts = tareas.stream()
                .filter(t -> t.getTipo() != null) // Asegurarse de que el tipo no sea nulo
                .collect(Collectors.groupingBy(Tarea::getTipo, Collectors.counting()));
        
        if (typeCounts.isEmpty()) return null;

        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("");

        DataSeries series = new DataSeries("Tipos de Tareas");
        typeCounts.forEach((type, count) -> series.add(new DataSeriesItem(type.toString(), count)));
        conf.addSeries(series);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);
        conf.setPlotOptions(plotOptions);

        chart.setHeight("400px");
        chart.setWidth("450px");
        return chart;
    }
}