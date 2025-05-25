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

public class DashboardTipo extends VerticalLayout implements EstrategiaMetrica {
    private final DashboardMetricasDTO metricasDashboard;

    // Theme colors for consistent styling
    private final String[] CHART_COLORS = {
            "#3366CC", "#DC3912", "#FF9900", "#109618", "#990099",
            "#0099C6", "#DD4477", "#66AA00", "#B82E2E", "#316395"
    };

    public DashboardTipo(DashboardMetricasDTO metricasDashboard) {
        this.metricasDashboard = metricasDashboard;
    }

    @Override
    public Component ejecutar(DashboardMetricasDTO metrica) {
        // Create a new instance with the provided metrics
        DashboardTipo dashboardTipo = new DashboardTipo(metrica);

        // Dashboard content wrapper
        Div contentWrapper = new Div();
        contentWrapper.addClassNames(
                LumoUtility.Padding.LARGE,
                LumoUtility.BoxSizing.BORDER);
        contentWrapper.setWidthFull();

        // KPI Cards section arreglalo
        contentWrapper.add(dashboardTipo.createKPISection());

        // Main charts grid
        FlexLayout chartsContainer = dashboardTipo.createChartsContainer();

        // Add all chart components
        dashboardTipo.createAndAddCharts(chartsContainer);

        contentWrapper.add(chartsContainer);
        dashboardTipo.add(contentWrapper);

        return dashboardTipo;
    }

    private Component createKPISection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassNames(
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Margin.Vertical.MEDIUM);
        section.setSpacing(false);
        section.setPadding(false);
        section.getStyle().set("margin-bottom", "50px");

        H3 sectionTitle = new H3("Indicadores Clave de Rendimiento");
        sectionTitle.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.Margin.Bottom.MEDIUM);
        sectionTitle.getStyle().set("margin-bottom", "30px");

        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);
        kpiLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Get the total tasks
        Long totalTareas = metricasDashboard.getTotalTareas();

        // Agrupar y sumar por tipo
        Map<String, Long> conteoPorTipo = metricasDashboard.getDatosPorMes().stream()
                .collect(Collectors.groupingBy(
                        TareaPorMesDTO::getNombre,
                        Collectors.summingLong(TareaPorMesDTO::getCantidad)));

        Long medicamentosCount = conteoPorTipo.getOrDefault("Tarea para MEDICAMENTOS", 0L);
        Long ropaCount = conteoPorTipo.getOrDefault("Tarea para ROPA", 0L);
        Long refugioCount = conteoPorTipo.getOrDefault("Tarea para REFUGIO", 0L);
        Long alimentosCount = conteoPorTipo.getOrDefault("Tarea para ALIMENTOS", 0L);

        kpiLayout.add(
                createKPICard("Total Tareas", String.valueOf(totalTareas), "#000000"),
                createKPICard("Medicamentos", medicamentosCount.toString(), "#109618"),
                createKPICard("Ropa", ropaCount.toString(), "#FF9900"),
                createKPICard("Refugio", refugioCount.toString(), "#3366CC"),
                createKPICard("Alimentos", alimentosCount.toString(), "#990099"));

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
        Chart statusChart = createTaskTypeDistributionChart();
        if (statusChart != null) {
            Div chartCard = createChartCard("Distribución de Tareas por Tipo", statusChart, false);
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
            Div chartCard = createChartCard("Desglose de Tareas por Tipo", taskByNameChart, true);
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

    // Helper para color por tipo
    private SolidColor getColorForTipo(String tipo) {
        switch (tipo) {
            case "Tarea para MEDICAMENTOS":
                return new SolidColor("#109618"); // Verde
            case "Tarea para REFUGIO":
                return new SolidColor("#3366CC"); // Azul
            case "Tarea para ROPA":
                return new SolidColor("#FF9900"); // Naranja
            case "Tarea para ALIMENTOS":
                return new SolidColor("#990099"); // Morado
            default:
                return new SolidColor("#0099C6"); // Otro
        }
    }

    // CHART 1: Timeline trend chart
    private Chart createTaskTrendChart() {
        List<TareaPorMesDTO> metricas = metricasDashboard.getDatosPorMes();
        if (metricas == null || metricas.isEmpty()) {
            return null;
        }

        Chart chart = new Chart(ChartType.LINE);
        Configuration conf = chart.getConfiguration();

        DataSeries series = new DataSeries();
        series.setName("Tareas por Tipo");

        // Add data points
        for (TareaPorMesDTO tipo : metricas) {
            series.add(new DataSeriesItem(tipo.getNombre(), tipo.getCantidad()));
        }
        conf.addSeries(series);

        // Configure x-axis
        XAxis xAxis = new XAxis();
        xAxis.setCategories(metricas.stream()
                .map(TareaPorMesDTO::getNombre)
                .toArray(String[]::new));
        conf.addxAxis(xAxis);

        // Configure y-axis
        YAxis yAxis = new YAxis();
        yAxis.setTitle("Cantidad de Tareas");
        conf.addyAxis(yAxis);

        chart.setHeight("300px");
        return chart;
    }

    // CHART 2: Status distribution as a donut chart
    private Chart createTaskTypeDistributionChart() {
        List<TareaPorMesDTO> metricas = metricasDashboard.getDatosPorMes();
        if (metricas == null || metricas.isEmpty()) {
            return null;
        }

        // Agrupar y sumar por tipo
        Map<String, Long> conteoPorTipo = metricas.stream()
                .collect(Collectors.groupingBy(
                        TareaPorMesDTO::getNombre,
                        Collectors.summingLong(TareaPorMesDTO::getCantidad)));

        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();

        DataSeries series = new DataSeries();
        series.setName("Tareas por Tipo");

        for (Map.Entry<String, Long> entry : conteoPorTipo.entrySet()) {
            DataSeriesItem item = new DataSeriesItem(entry.getKey(), entry.getValue());
            item.setColor(getColorForTipo(entry.getKey()));
            series.add(item);
        }
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
        List<TareaPorMesDTO> metricas = metricasDashboard.getDatosPorMes();
        if (metricas == null || metricas.isEmpty()) {
            return null;
        }

        // Agrupar y sumar por tipo
        Map<String, Long> conteoPorTipo = metricas.stream()
                .collect(Collectors.groupingBy(
                        TareaPorMesDTO::getNombre,
                        Collectors.summingLong(TareaPorMesDTO::getCantidad)));

        Chart chart = new Chart(ChartType.COLUMN);
        Configuration conf = chart.getConfiguration();

        DataSeries series = new DataSeries();
        series.setName("Tareas por Tipo");

        for (Map.Entry<String, Long> entry : conteoPorTipo.entrySet()) {
            DataSeriesItem item = new DataSeriesItem(entry.getKey(), entry.getValue());
            item.setColor(getColorForTipo(entry.getKey()));
            series.add(item);
        }
        conf.addSeries(series);

        // Configure x-axis
        XAxis xAxis = new XAxis();
        xAxis.setCategories(conteoPorTipo.keySet().toArray(new String[0]));
        conf.addxAxis(xAxis);

        // Configure y-axis
        YAxis yAxis = new YAxis();
        yAxis.setTitle("Cantidad de Tareas");
        conf.addyAxis(yAxis);

        chart.setHeight("300px");
        return chart;
    }

    // CHART 4: Tasks by name as a bar chart
    private Chart createTasksByNameChart() {
        List<TareaPorMesDTO> metricas = metricasDashboard.getDatosPorMes();
        if (metricas == null || metricas.isEmpty()) {
            return null;
        }

        // Agrupar y sumar por tipo
        Map<String, Long> conteoPorTipo = metricas.stream()
                .collect(Collectors.groupingBy(
                        TareaPorMesDTO::getNombre,
                        Collectors.summingLong(TareaPorMesDTO::getCantidad)));

        Chart chart = new Chart(ChartType.BAR);
        Configuration conf = chart.getConfiguration();

        DataSeries series = new DataSeries();
        series.setName("Tareas por Tipo");

        for (Map.Entry<String, Long> entry : conteoPorTipo.entrySet()) {
            DataSeriesItem item = new DataSeriesItem(entry.getKey(), entry.getValue());
            item.setColor(getColorForTipo(entry.getKey()));
            series.add(item);
        }
        conf.addSeries(series);

        // Configure x-axis
        XAxis xAxis = new XAxis();
        xAxis.setCategories(conteoPorTipo.keySet().toArray(new String[0]));
        conf.addxAxis(xAxis);

        // Configure y-axis
        YAxis yAxis = new YAxis();
        yAxis.setTitle("Cantidad de Tareas");
        conf.addyAxis(yAxis);

        chart.setHeight("300px");
        return chart;
    }

    private Component createMetricsCards() {
        // Create metrics cards
        HorizontalLayout metricsLayout = new HorizontalLayout();
        metricsLayout.setWidthFull();
        metricsLayout.setSpacing(true);
        metricsLayout.setPadding(true);

        // Total tasks card
        VerticalLayout totalCard = createMetricCard(
                "Total de Tareas",
                String.valueOf(metricasDashboard.getTotalTareas()),
                "tasks");

        // Completed tasks card
        VerticalLayout medCard = createMetricCard(
                "Medicamentos",
                String.valueOf(
                        metricasDashboard.getTipo().equals("Medicamentos") ? metricasDashboard.getCantidad() : 0),
                "medicine");

        // In progress tasks card
        VerticalLayout clothesCard = createMetricCard(
                "Ropa",
                String.valueOf(metricasDashboard.getTipo().equals("Ropa") ? metricasDashboard.getCantidad() : 0),
                "clothes");

        // Pending tasks card
        VerticalLayout shelterCard = createMetricCard(
                "Refugio",
                String.valueOf(metricasDashboard.getTipo().equals("Refugio") ? metricasDashboard.getCantidad() : 0),
                "house");

        // Average tasks per month card
        VerticalLayout foodCard = createMetricCard(
                "Alimentos",
                String.valueOf(metricasDashboard.getTipo().equals("Alimentos") ? metricasDashboard.getCantidad() : 0),
                "food");

        metricsLayout.add(totalCard, medCard, clothesCard, shelterCard, foodCard);
        add(metricsLayout);
        return metricsLayout;
    }

    private VerticalLayout createMetricCard(String title, String value, String icon) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Width.MEDIUM);

        HorizontalLayout content = new HorizontalLayout();
        content.setWidthFull();
        content.setSpacing(true);
        content.setPadding(true);

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

        content.add(titleSpan, valueH3);

        Span iconSpan = new Span(icon);
        iconSpan.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.TextColor.SECONDARY);
        content.add(iconSpan);

        card.add(content);
        return card;
    }
}
