package SolidarityHub.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Cursor;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsAreaspline;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import SolidarityHub.models.dtos.DashboardMetricasDTO;
import SolidarityHub.models.dtos.TareaPorMesDTO;

public class DashboardEstado extends VerticalLayout implements EstrategiaMetrica {

    private DashboardMetricasDTO metricasDashboard;

    // Theme colors for consistent styling
    private final String[] CHART_COLORS = {
            "#3366CC", "#DC3912", "#FF9900", "#109618", "#990099",
            "#0099C6", "#DD4477", "#66AA00", "#B82E2E", "#316395"
    };

    public DashboardEstado(DashboardMetricasDTO metricasDashboard) {
        this.metricasDashboard = metricasDashboard;
    }

    @Override
    public Component ejecutar(DashboardMetricasDTO metrica) {
        // Create a new instance with the provided metrics
        DashboardEstado dashboardEstado = new DashboardEstado(metrica);

        // Dashboard content wrapper
        Div contentWrapper = new Div();
        contentWrapper.addClassNames(
                LumoUtility.Padding.LARGE,
                LumoUtility.BoxSizing.BORDER);
        contentWrapper.setWidthFull();

        // KPI Cards section
        contentWrapper.add(dashboardEstado.createKPISection());

        // Main charts grid
        FlexLayout chartsContainer = dashboardEstado.createChartsContainer();

        // Add all chart components
        dashboardEstado.createAndAddCharts(chartsContainer);

        contentWrapper.add(chartsContainer);
        dashboardEstado.add(contentWrapper);

        return dashboardEstado;
    }

    private Component createKPISection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassNames(
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Margin.Vertical.MEDIUM);
        section.setSpacing(false);
        section.setPadding(false);
        section.getStyle().set("margin-bottom", "50px"); // Center the title text

        H3 sectionTitle = new H3("Indicadores Clave de Rendimiento");
        sectionTitle.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.Margin.Bottom.MEDIUM);
        sectionTitle.getStyle().set("margin-bottom", "30px"); // Center the title text

        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);
        kpiLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        kpiLayout.add(
                createKPICard("Total Tareas", String.valueOf(metricasDashboard.totalTareas),
                        "#000000"),
                createKPICard("Completadas", String.valueOf(metricasDashboard.tareasCompletadas), "#109618"),
                createKPICard("En Curso", String.valueOf(metricasDashboard.tareasEnCurso), "#FF9900"),
                createKPICard("Pendientes", String.valueOf(metricasDashboard.tareasPendientes), "#3366CC"),
                createKPICard("Promedio/Mes", String.format("%.1f", metricasDashboard.promedioPorMes), "#990099"));

        section.add(sectionTitle, kpiLayout);
        return section;
    }

    private Component createKPICard(String title, String value, String color) {
        Div card = new Div();
        card.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Width.MEDIUM);
        card.getStyle().set("border-top", "4px solid " + color);

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(FlexComponent.Alignment.CENTER);

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontWeight.MEDIUM);

        H3 valueH3 = new H3(value);
        valueH3.addClassNames(
                LumoUtility.FontSize.XXXLARGE,
                LumoUtility.Margin.Vertical.SMALL,
                LumoUtility.TextColor.PRIMARY);
        valueH3.getStyle().setColor(color);

        content.add(titleSpan, valueH3);
        card.add(content);
        return card;
    }

    private FlexLayout createChartsContainer() {
        FlexLayout chartsContainer = new FlexLayout();
        chartsContainer.setWidthFull();
        chartsContainer.setFlexWrap(FlexWrap.WRAP);
        chartsContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        chartsContainer.addClassName(LumoUtility.Gap.LARGE);
        return chartsContainer;
    }

    private void createAndAddCharts(FlexLayout container) {
        // Top row - full width chart
        Chart taskTrendChart = createTaskTrendChart();
        if (taskTrendChart != null) {
            Div chartCard = createChartCard("Tendencia de Tareas por Mes", taskTrendChart, true);
            container.add(chartCard);
            chartCard.getStyle().set("margin-bottom", "30px"); // Set width to 100% for the full-width char
        }

        // Middle row - two medium charts
        Chart statusChart = createTaskStatusDistributionChart();
        if (statusChart != null) {
            Div chartCard = createChartCard("Distribución por Estado", statusChart, false);
            container.add(chartCard);// Set width to 48% for the two charts
        }

        Chart monthlyTasksChart = createMonthlyTaskDistributionChart();
        if (monthlyTasksChart != null) {
            Div chartCard = createChartCard("Tareas por Mes", monthlyTasksChart, false);
            container.add(chartCard); // Set width to 48% for the two charts
        }

        // Bottom row - task details by name
        Chart taskByNameChart = createTasksByNameChart();
        if (taskByNameChart != null) {
            Div chartCard = createChartCard("Desglose de Tareas por Estado", taskByNameChart, true);
            container.add(chartCard);// Set width to 100% for the full-width char
        }
    }

    private Div createChartCard(String title, Component chart, boolean fullWidth) {
        Div card = new Div();
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Padding.MEDIUM);

        if (fullWidth) {
            card.setWidthFull();
        } else {
            card.setWidth("48%");
        }

        H4 chartTitle = new H4(title);
        chartTitle.addClassNames(
                LumoUtility.Margin.NONE,
                LumoUtility.Margin.Bottom.MEDIUM,
                LumoUtility.TextColor.SECONDARY);

        card.add(chartTitle, chart);
        return card;
    }

    // CHART 1: Timeline trend chart
    private Chart createTaskTrendChart() {
        List<TareaPorMesDTO> metricas = metricasDashboard.datosPorMes;
        if (metricas == null || metricas.isEmpty()) {
            return null;
        }

        Chart chart = new Chart(ChartType.AREASPLINE);
        Configuration conf = chart.getConfiguration();

        // Month names
        String[] monthNames = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };

        // Get unique months in order
        Set<Integer> months = metricas.stream()
                .map(TareaPorMesDTO::getMes)
                .collect(Collectors.toCollection(TreeSet::new));

        // Create month categories
        String[] categories = months.stream()
                .map(month -> monthNames[month - 1])
                .toArray(String[]::new);

        // Total tasks per month
        Map<Integer, Long> totalsByMonth = metricas.stream()
                .collect(Collectors.groupingBy(
                        TareaPorMesDTO::getMes,
                        Collectors.summingLong(TareaPorMesDTO::getCantidad)));

        DataSeries series = new DataSeries();
        series.setName("Total Tareas");

        for (Integer month : months) {
            DataSeriesItem item = new DataSeriesItem(monthNames[month - 1], totalsByMonth.get(month));
            series.add(item);
        }

        conf.getxAxis().setCategories(categories);

        YAxis yAxis = conf.getyAxis();
        yAxis.setTitle("Número de Tareas");
        yAxis.setMin(0);

        conf.addSeries(series);

        PlotOptionsAreaspline plotOptions = new PlotOptionsAreaspline();
        plotOptions.setFillOpacity(0.5);
        plotOptions.setColor(SolidColor.OLIVE);
        series.setPlotOptions(plotOptions);

        chart.setHeight("300px");
        return chart;
    }

    // CHART 2: Status distribution as a donut chart
    private Chart createTaskStatusDistributionChart() {
        if (metricasDashboard.totalTareas == 0) {
            return null;
        }

        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();

        DataSeries series = new DataSeries();

        // Add data points with specific colors
        DataSeriesItem completed = new DataSeriesItem("Completadas", metricasDashboard.tareasCompletadas);
        completed.setColor(SolidColor.GREEN);
        series.add(completed);

        DataSeriesItem inProgress = new DataSeriesItem("En Curso", metricasDashboard.tareasEnCurso);
        inProgress.setColor(SolidColor.ORANGE);
        series.add(inProgress);

        DataSeriesItem pending = new DataSeriesItem("Pendientes", metricasDashboard.tareasPendientes);
        pending.setColor(SolidColor.BLUE);
        series.add(pending);

        conf.addSeries(series);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setInnerSize("60%"); // Make it a donut chart
        plotOptions.setDataLabels(new DataLabels(true));
        plotOptions.getDataLabels().setFormat("<b>{point.name}</b>: {point.percentage:.1f}%");

        conf.setPlotOptions(plotOptions);

        chart.setHeight("300px");
        return chart;
    }

    // CHART 3: Monthly task distribution as a column chart
    private Chart createMonthlyTaskDistributionChart() {
        List<TareaPorMesDTO> metricas = metricasDashboard.datosPorMes;
        if (metricas == null || metricas.isEmpty()) {
            return null;
        }

        Chart chart = new Chart(ChartType.COLUMN);
        Configuration conf = chart.getConfiguration();

        // Get month names and unique months
        String[] monthNames = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };

        Map<Integer, Long> tasksByMonth = metricas.stream()
                .collect(Collectors.groupingBy(
                        TareaPorMesDTO::getMes,
                        Collectors.summingLong(TareaPorMesDTO::getCantidad)));

        // Sort months
        List<Integer> sortedMonths = new ArrayList<>(tasksByMonth.keySet());
        Collections.sort(sortedMonths);

        DataSeries series = new DataSeries();
        series.setName("Tareas");

        // Add data points with color
        for (Integer month : sortedMonths) {
            DataSeriesItem item = new DataSeriesItem(monthNames[month - 1], tasksByMonth.get(month));
            // Cycle through colors
            item.setColor(new SolidColor(CHART_COLORS[month % CHART_COLORS.length]));
            series.add(item);
        }

        conf.addSeries(series);

        XAxis xAxis = conf.getxAxis();
        xAxis.setTitle("Mes");

        YAxis yAxis = conf.getyAxis();
        yAxis.setTitle("Número de Tareas");
        yAxis.setMin(0);

        // Add plot options
        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        plotOptions.setBorderRadius(4);
        conf.setPlotOptions(plotOptions);

        chart.setHeight("300px");
        return chart;
    }

    // CHART 4: Tasks by name as a bar chart
    private Chart createTasksByNameChart() {
        List<TareaPorMesDTO> metricas = metricasDashboard.datosPorMes;
        if (metricas == null || metricas.isEmpty()) {
            return null;
        }

        Chart chart = new Chart(ChartType.BAR);
        Configuration conf = chart.getConfiguration();

        DataSeries series = new DataSeries();

        // Add data points with specific colors
        DataSeriesItem completed = new DataSeriesItem("Completadas", metricasDashboard.tareasCompletadas);
        completed.setColor(SolidColor.GREEN);
        series.add(completed);

        DataSeriesItem inProgress = new DataSeriesItem("En Curso", metricasDashboard.tareasEnCurso);
        inProgress.setColor(SolidColor.ORANGE);
        series.add(inProgress);

        DataSeriesItem pending = new DataSeriesItem("Pendientes", metricasDashboard.tareasPendientes);
        pending.setColor(SolidColor.BLUE);
        series.add(pending);

        conf.addSeries(series);

        XAxis xAxis = conf.getxAxis();
        xAxis.setTitle("Tipo de Tarea");

        YAxis yAxis = conf.getyAxis();
        yAxis.setTitle("Número de Tareas");
        yAxis.setMin(0);

        // Add plot options
        PlotOptionsBar plotOptions = new PlotOptionsBar();
        plotOptions.setBorderRadius(4);
        plotOptions.setColorByPoint(true);
        conf.setPlotOptions(plotOptions);

        chart.setHeight("400px");
        return chart;
    }
}
