package SolidarityHub.views;

import SolidarityHub.models.Necesidad.TipoNecesidad;
import SolidarityHub.models.Tarea;
import SolidarityHub.models.dtos.TareaPorMesDTO;
import SolidarityHub.services.TareaServicio;
import SolidarityHub.services.TareaServicio.DashboardMetricasDTO;
import SolidarityHub.services.UsuarioServicio;

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

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | SolidarityHub")
public class DashboardView extends VerticalLayout {

    private final TareaServicio tareaServicio;
    private final DashboardMetricasDTO metricasDashboard;

    // Colores del tema para un estilo consistente
    private final String[] CHART_COLORS = {
            "#3366CC", "#DC3912", "#FF9900", "#109618", "#990099",
            "#0099C6", "#DD4477", "#66AA00", "#B82E2E", "#316395"
    };

    public DashboardView(UsuarioServicio usuarioServicio, TareaServicio tareaServicio) {
        this.tareaServicio = tareaServicio;
        this.metricasDashboard = tareaServicio.obtenerMetricasDashboard();

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        addClassName(LumoUtility.Background.CONTRAST_5);

        // Sección de cabecera con título y subtítulo
        createHeader();

        // Contenedor principal del dashboard
        Div contentWrapper = new Div();
        contentWrapper.addClassNames(
                LumoUtility.Padding.LARGE,
                LumoUtility.BoxSizing.BORDER);
        contentWrapper.setWidthFull();

        // Sección de tarjetas KPI
        contentWrapper.add(createKPISection());
        
        // Rejilla principal de gráficos
        FlexLayout chartsContainer = createChartsContainer();
        
        // Añadir todos los componentes de gráficos
        createAndAddCharts(chartsContainer);
        
        contentWrapper.add(chartsContainer);
        add(contentWrapper);
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
        subtitle.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.Top.SMALL, LumoUtility.TextColor.PRIMARY_CONTRAST);

        headerContent.add(title, subtitle);
        header.add(headerContent);
        add(header);
    }

    private Component createKPISection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassNames(
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Margin.Vertical.MEDIUM);
        section.setSpacing(false);
        section.setPadding(false);
        section.getStyle().set("margin-bottom", "50px"); // Centrar el texto del título

        H3 sectionTitle = new H3("Indicadores Clave de Rendimiento");
        sectionTitle.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.Margin.Bottom.MEDIUM);
        sectionTitle.getStyle().set("margin-bottom", "30px"); // Centrar el texto del título

        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);
        kpiLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        kpiLayout.add(
            createKPICard("Total Tareas", String.valueOf(metricasDashboard.totalTareas), "var(--lumo-primary-color)"),
            createKPICard("Completadas", String.valueOf(metricasDashboard.tareasCompletadas), "#109618"),
            createKPICard("En Curso", String.valueOf(metricasDashboard.tareasEnCurso), "#FF9900"),
            createKPICard("Pendientes", String.valueOf(metricasDashboard.tareasPendientes), "#3366CC"),
            createKPICard("Promedio/Mes", String.format("%.1f", metricasDashboard.promedioPorMes), "#990099")
        );

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
        // Fila superior - gráfico de ancho completo
        Chart taskTrendChart = createTaskTrendChart();
        if (taskTrendChart != null) {
            Div chartCard = createChartCard("Tendencia de Tareas por Mes", taskTrendChart, true);
            container.add(chartCard);
            chartCard.getStyle().set("margin-bottom", "30px"); // Establecer ancho al 100% para el gráfico de ancho completo
        }

        // Fila media - dos gráficos medianos
        Chart statusChart = createTaskStatusDistributionChart();
        if (statusChart != null) {
            Div chartCard = createChartCard("Distribución por Estado", statusChart, false);
            container.add(chartCard);// Establecer ancho al 48% para los dos gráficos
        }
        
        Chart monthlyTasksChart = createMonthlyTaskDistributionChart();
        if (monthlyTasksChart != null) {
            Div chartCard = createChartCard("Tareas por Mes", monthlyTasksChart, false);
            container.add(chartCard); // Establecer ancho al 48% para los dos gráficos
        }
        
        // Fila inferior - detalles de tareas por nombre
        Chart taskByNameChart = createTasksByNameChart();
        if (taskByNameChart != null) {
            Div chartCard = createChartCard("Desglose de Tareas por Nombre", taskByNameChart, true);
            container.add(chartCard);// Establecer ancho al 100% para el gráfico de ancho completo
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

    // GRÁFICO 1: Gráfico de tendencia temporal
    private Chart createTaskTrendChart() {
        List<TareaPorMesDTO> metricas = metricasDashboard.datosPorMes;
        if (metricas == null || metricas.isEmpty()) {
            return null;
        }

        Chart chart = new Chart(ChartType.AREASPLINE);
        Configuration conf = chart.getConfiguration();
        
        // Nombres de los meses
        String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                              "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        
        // Obtener meses únicos en orden
        Set<Integer> months = metricas.stream()
                .map(TareaPorMesDTO::getMes)
                .collect(Collectors.toCollection(TreeSet::new));
        
        // Crear categorías de meses
        String[] categories = months.stream()
                .map(month -> monthNames[month-1])
                .toArray(String[]::new);
        
        // Total de tareas por mes
        Map<Integer, Long> totalsByMonth = metricas.stream()
                .collect(Collectors.groupingBy(
                        TareaPorMesDTO::getMes, 
                        Collectors.summingLong(TareaPorMesDTO::getCantidad)));
        
        DataSeries series = new DataSeries();
        series.setName("Total Tareas");
        
        for (Integer month : months) {
            DataSeriesItem item = new DataSeriesItem(monthNames[month-1], totalsByMonth.get(month));
            series.add(item);
        }
        
        conf.getxAxis().setCategories(categories);
        
        YAxis yAxis = conf.getyAxis();
        yAxis.setTitle("Número de Tareas");
        yAxis.setMin(0);
        
        conf.addSeries(series);
        
        PlotOptionsAreaspline plotOptions = new PlotOptionsAreaspline();
        plotOptions.setFillOpacity(0.5);
        plotOptions.setColor(SolidColor.MEDIUMBLUE);
        series.setPlotOptions(plotOptions);
        
        chart.setHeight("300px");
        return chart;
    }

    // GRÁFICO 2: Distribución de estados como gráfico de anillo
    private Chart createTaskStatusDistributionChart() {
        if (metricasDashboard.totalTareas == 0) {
            return null;
        }

        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();

        DataSeries series = new DataSeries();
        
        // Añadir puntos de datos con colores específicos
        DataSeriesItem completed = new DataSeriesItem("Finalizadas", metricasDashboard.tareasCompletadas);
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

    // GRÁFICO 3: Distribución mensual de tareas como gráfico de columnas
    private Chart createMonthlyTaskDistributionChart() {
        List<TareaPorMesDTO> metricas = metricasDashboard.datosPorMes;
        if (metricas == null || metricas.isEmpty()) {
            return null;
        }

        Chart chart = new Chart(ChartType.COLUMN);
        Configuration conf = chart.getConfiguration();

        // Obtener nombres de meses y meses únicos
        String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                              "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        
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
            DataSeriesItem item = new DataSeriesItem(monthNames[month-1], tasksByMonth.get(month));
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

    // GRÁFICO 4: Tareas por nombre como gráfico de barras
    private Chart createTasksByNameChart() {
        List<TareaPorMesDTO> metricas = metricasDashboard.datosPorMes;
        if (metricas == null || metricas.isEmpty()) {
            return null;
        }

        Chart chart = new Chart(ChartType.BAR);
        Configuration conf = chart.getConfiguration();
        
        // Agrupar tareas por nombre
        Map<String, Long> tasksByName = metricas.stream()
                .collect(Collectors.groupingBy(
                        TareaPorMesDTO::getNombre, 
                        Collectors.summingLong(TareaPorMesDTO::getCantidad)));
        
        // Ordenar por recuento de tareas descendente
        List<Map.Entry<String, Long>> sortedTasks = new ArrayList<>(tasksByName.entrySet());
        sortedTasks.sort(Map.Entry.<String, Long>comparingByValue().reversed());
        
        // Limitar a los 10 primeros si tenemos más
        if (sortedTasks.size() > 10) {
            sortedTasks = sortedTasks.subList(0, 10);
        }
        
        // Preparar categorías y series
        String[] categories = sortedTasks.stream()
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
        
        DataSeries series = new DataSeries();
        series.setName("Cantidad de Tareas");
        
        int colorIndex = 0;
        for (Map.Entry<String, Long> entry : sortedTasks) {
            DataSeriesItem item = new DataSeriesItem(entry.getKey(), entry.getValue());
            item.setColor(new SolidColor(CHART_COLORS[colorIndex % CHART_COLORS.length]));
            series.add(item);
            colorIndex++;
        }
        
        conf.addSeries(series);
        
        XAxis xAxis = conf.getxAxis();
        xAxis.setCategories(categories);
        
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