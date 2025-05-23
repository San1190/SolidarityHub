package SolidarityHub.views;

import SolidarityHub.models.Tarea;
import SolidarityHub.services.TareaServicio;
import SolidarityHub.services.TareaServicio.DashboardMetricasDTO;
import SolidarityHub.services.UsuarioServicio;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;

@Route(value = "main", layout = MainLayout.class)
@PageTitle("Main | SolidarityHub")
public class MainView extends VerticalLayout {

        private UsuarioServicio usuarioServicio;

        private TareaServicio tareaServicio;

        public MainView(UsuarioServicio usuarioServicio, TareaServicio tareaServicio) {
                this.usuarioServicio = usuarioServicio;
                this.tareaServicio = tareaServicio;

                setSizeFull();
                setSpacing(false);
                setPadding(true);
                setAlignItems(Alignment.CENTER);

                // Mostrar tarjeta con métricas y gráfico
                add(crearDashboard());
        }

        private Div crearGoogleChart() {
                Div chartDiv = new Div();
                chartDiv.setId("google-bar-chart");
                chartDiv.setWidth("650px");
                chartDiv.setHeight("400px");
                chartDiv.getStyle().set("margin", "2rem auto");
                chartDiv.getElement().setProperty("innerHTML",
                                "<div style='text-align:center; padding:20px;'>Cargando datos...</div>");
                return chartDiv;
        }

        private Div crearPieChart() {
                Div pieDiv = new Div();
                pieDiv.setId("google-pie-chart");
                pieDiv.setWidth("650px");
                pieDiv.setHeight("400px");
                pieDiv.getStyle().set("margin", "2rem auto");
                pieDiv.getElement().setProperty("innerHTML",
                                "<div style='text-align:center; padding:20px;'>Cargando gráfico circular...</div>");
                return pieDiv;
        }

        // Método que crea todo el dashboard
        private Div crearDashboard() {
                Div dashboardContainer = new Div();
                dashboardContainer.setWidth("900px");
                dashboardContainer.setMaxWidth("100%");
                dashboardContainer.getStyle().set("margin", "0 auto");

                // Añadir título del dashboard
                H3 dashboardTitle = new H3("Dashboard de SolidarityHub");
                dashboardTitle.addClassName(LumoUtility.FontSize.XXXLARGE);
                dashboardTitle.addClassName(LumoUtility.TextColor.PRIMARY);
                dashboardTitle.addClassName(LumoUtility.TextAlignment.CENTER);
                dashboardTitle.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

                // Añadir componentes al dashboard
                dashboardContainer.add(
                                dashboardTitle,
                                crearMetricas(), // Primero el panel de métricas
                                crearPieChart(), // Después el gráfico circular
                                crearGoogleChart() // Finalmente el gráfico de barras
                );

                // Inicializar dashboard con una única llamada al backend
                inicializarDashboard();

                return dashboardContainer;
        }

        // Método que crea el panel de métricas
        private HorizontalLayout crearMetricas() {
                HorizontalLayout metricas = new HorizontalLayout();
                metricas.setWidthFull();
                metricas.setJustifyContentMode(JustifyContentMode.CENTER);
                metricas.setSpacing(true);
                metricas.setPadding(true);
                metricas.setAlignItems(Alignment.CENTER);

                Div totalTareas = crearTarjetaMetrica("Total Tareas", "valor-total");
                Div completadas = crearTarjetaMetrica("Completadas", "valor-completadas");
                Div enCurso = crearTarjetaMetrica("En Curso", "valor-en-curso");
                Div pendientes = crearTarjetaMetrica("Pendientes", "valor-pendientes");

                metricas.add(totalTareas, completadas, enCurso, pendientes);

                UI.getCurrent().getPage().retrieveExtendedClientDetails(details -> {
                        try {
                                DashboardMetricasDTO metrics = tareaServicio.obtenerMetricasDashboard();
                                Page page = UI.getCurrent().getPage();
                                page.executeJs("document.getElementById('valor-total').innerText = $0;",
                                                metrics.totalTareas);
                                page.executeJs("document.getElementById('valor-completadas').innerText = $0;",
                                                metrics.tareasCompletadas);
                                page.executeJs("document.getElementById('valor-en-curso').innerText = $0;",
                                                metrics.tareasEnCurso);
                                page.executeJs("document.getElementById('valor-pendientes').innerText = $0;",
                                                metrics.tareasPendientes);
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                });

                return metricas;
        }

        // Método auxiliar para crear una tarjeta de métrica individual
        private Div crearTarjetaMetrica(String titulo, String idValorSpan) {
                Div tarjeta = new Div();
                tarjeta.setWidth("150px");
                tarjeta.setHeight("100px");
                tarjeta.getStyle()
                                .set("border", "1px solid #ccc")
                                .set("border-radius", "8px")
                                .set("padding", "10px")
                                .set("text-align", "center")
                                .set("box-shadow", "2px 2px 5px rgba(0,0,0,0.1)");

                Span tituloSpan = new Span(titulo);
                tituloSpan.getStyle().set("display", "block").set("font-weight", "bold");

                Span valorSpan = new Span("0");
                valorSpan.setId(idValorSpan); // Identificador único
                valorSpan.getStyle()
                                .set("display", "block")
                                .set("margin-top", "10px")
                                .set("font-size", "1.5em");

                // Colores según tipo
                switch (titulo) {
                        case "Total Tareas":
                                valorSpan.getStyle().set("color", "#050608");
                                break;
                        case "Completadas":
                                valorSpan.getStyle().set("color", "#4CAF50");
                                break;
                        case "En Curso":
                                valorSpan.getStyle().set("color", "#2f77ef");
                                break;
                        case "Pendientes":
                                valorSpan.getStyle().set("color", "#FFC107");
                                break;
                }

                tarjeta.add(tituloSpan, valorSpan);
                return tarjeta;
        }

        private void inicializarDashboard() {
                UI.getCurrent().getPage().executeJs(
                                "console.log('Iniciando carga del dashboard...');" +
                                // Solo funciones para los gráficos
                                                "function mostrarError(error) {" +
                                                "  console.error('Error:', error);" +
                                                "  const errorMsg = '<div style=\\\"text-align:center; color:red; padding:20px;\\\">Error: ' + error.message + '</div>';"
                                                +
                                                "  document.getElementById('google-bar-chart').innerHTML = errorMsg;" +
                                                "  document.getElementById('google-pie-chart').innerHTML = errorMsg;" +
                                                "}" +
                                                "function dibujarGraficoCircular(data) {" +
                                                "  try {" +
                                                "    const dataTable = new google.visualization.DataTable();" +
                                                "    dataTable.addColumn('string', 'Estado');" +
                                                "    dataTable.addColumn('number', 'Cantidad');" +
                                                "    dataTable.addRows([" +
                                                "      ['Completadas', data.tareasCompletadas || 0]," +
                                                "      ['En Curso', data.tareasEnCurso || 0]," +
                                                "      ['Pendientes', data.tareasPendientes || 0]" +
                                                "    ]);" +
                                                "    const options = {" +
                                                "      title: 'Proporción de tareas por estado'," +
                                                "      pieHole: 0.4," +
                                                "      legend: { position: 'right' }," +
                                                "      chartArea: { width: '80%', height: '80%' }," +
                                                "      colors: ['#4CAF50', '#2f77ef', '#FFC107']" +
                                                "    };" +
                                                "    const chart = new google.visualization.PieChart(document.getElementById('google-pie-chart'));"
                                                +
                                                "    chart.draw(dataTable, options);" +
                                                "  } catch (error) {" +
                                                "    mostrarError(error);" +
                                                "  }" +
                                                "}" +
                                                "function dibujarGraficoBarras(data) {" +
                                                "  try {" +
                                                "    if (!data || !data.datosPorMes || data.datosPorMes.length === 0) {"
                                                +
                                                "      document.getElementById('google-bar-chart').innerHTML = " +
                                                "        '<div style=\\\"text-align:center; padding:20px;\\\">No hay datos disponibles</div>';"
                                                +
                                                "      return;" +
                                                "    }" +
                                                "    const dataTable = new google.visualization.DataTable();" +
                                                "    dataTable.addColumn('string', 'Mes');" +
                                                "    const tareas = [...new Set(data.datosPorMes.map(d => d.nombre))];"
                                                +
                                                "    tareas.forEach(tarea => dataTable.addColumn('number', tarea));" +
                                                "    const mesesTexto = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',"
                                                +
                                                "                       'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];"
                                                +
                                                "    const porMes = {};" +
                                                "    data.datosPorMes.forEach(d => {" +
                                                "      const mesTexto = mesesTexto[d.mes-1];" +
                                                "      if (!porMes[mesTexto]) porMes[mesTexto] = {};" +
                                                "      porMes[mesTexto][d.nombre] = d.cantidad;" +
                                                "    });" +
                                                "    Object.keys(porMes).forEach(mes => {" +
                                                "      const fila = [mes];" +
                                                "      tareas.forEach(tarea => fila.push(porMes[mes][tarea] || 0));" +
                                                "      dataTable.addRow(fila);" +
                                                "    });" +
                                                "    const options = {" +
                                                "      title: 'Tareas completadas por mes'," +
                                                "      legend: { position: 'top' }," +
                                                "      vAxis: { title: 'Cantidad' }," +
                                                "      hAxis: { title: 'Mes' }," +
                                                "      chartArea: { width: '70%' }" +
                                                "    };" +
                                                "    const chart = new google.visualization.ColumnChart(document.getElementById('google-bar-chart'));"
                                                +
                                                "    chart.draw(dataTable, options);" +
                                                "  } catch (error) {" +
                                                "    mostrarError(error);" +
                                                "  }" +
                                                "}" +
                                                "function cargarDatosDashboard() {" +
                                                "  fetch('/api/tareas/dashboard-metrics')" +
                                                "    .then(response => {" +
                                                "      if (!response.ok) throw new Error('Error en el servidor: ' + response.status);"
                                                +
                                                "      return response.json();" +
                                                "    })" +
                                                "    .then(data => {" +
                                                "      dibujarGraficoCircular(data);" +
                                                "      dibujarGraficoBarras(data);" +
                                                "    })" +
                                                "    .catch(error => { mostrarError(error); });" +
                                                "}" +
                                                // Cargar Google Charts y ejecutar
                                                "if (typeof google === 'undefined' || !google.charts) {" +
                                                "  const script = document.createElement('script');" +
                                                "  script.src = 'https://www.gstatic.com/charts/loader.js';" +
                                                "  script.onload = () => {" +
                                                "    google.charts.load('current', {packages:['corechart']});" +
                                                "    google.charts.setOnLoadCallback(cargarDatosDashboard);" +
                                                "  };" +
                                                "  document.head.appendChild(script);" +
                                                "} else {" +
                                                "  cargarDatosDashboard();" +
                                                "}");
        }

}
