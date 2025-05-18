package SolidarityHub.views;

import SolidarityHub.models.Tarea;
import SolidarityHub.services.TareaServicio;
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

        private Image logo;

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

        private Image crearLogo() {
                // Cargar el logo
                logo = new Image(
                                "https://cliente.tuneupprocess.com/ApiWeb/UploadFiles/7dcef7b2-6389-45f4-9961-8741a558c286.png/LogoSH-transparent.png",
                                "Solidarity Hub Logo");
                logo.setWidth("220px");
                logo.setHeight("auto");
                logo.getStyle().set("margin", "0 auto").set("display", "block").set("padding", "1rem")
                                .set("align-items", "center").set("justify-content", "center")
                                .set("margin-top", "2rem");
                return logo;
        }

        private Div crearCard() {
                Div card = new Div();
                card.addClassName(LumoUtility.Background.BASE);
                card.addClassName(LumoUtility.BoxShadow.SMALL);
                card.addClassName(LumoUtility.BorderRadius.LARGE);
                card.addClassName(LumoUtility.Padding.LARGE);
                card.setMaxWidth("600px");
                card.setWidth("100%");

                H3 title = new H3("Bienvenido/a a SolidarityHub");
                title.addClassName(LumoUtility.FontSize.XXXLARGE);
                title.addClassName(LumoUtility.TextColor.SUCCESS);
                title.addClassName(LumoUtility.TextAlignment.CENTER);
                title.addClassName(LumoUtility.Margin.Bottom.LARGE);

                var usuario = usuarioServicio.obtenerUsuarioPorId(1L); // Obtener usuario actual

                H3 nombre = new H3("Nombre: " + usuario.getNombre() + " " + usuario.getApellidos());
                nombre.addClassName(LumoUtility.FontSize.MEDIUM);
                nombre.addClassName(LumoUtility.TextAlignment.CENTER);
                nombre.addClassName(LumoUtility.Margin.Bottom.SMALL);

                H3 email = new H3("Email: " + usuario.getEmail());
                email.addClassName(LumoUtility.FontSize.MEDIUM);
                email.addClassName(LumoUtility.TextAlignment.CENTER);
                email.addClassName(LumoUtility.Margin.Bottom.SMALL);

                H3 telefono = new H3("Teléfono: " + usuario.getTelefono());
                telefono.addClassName(LumoUtility.FontSize.MEDIUM);
                telefono.addClassName(LumoUtility.TextAlignment.CENTER);
                telefono.addClassName(LumoUtility.Margin.Bottom.SMALL);

                H3 rol = new H3("Rol: " + usuario.getTipoUsuario());
                rol.addClassName(LumoUtility.FontSize.MEDIUM);
                rol.addClassName(LumoUtility.TextAlignment.CENTER);
                rol.addClassName(LumoUtility.Margin.Bottom.LARGE);

                card.add(title, nombre, email, telefono, rol, crearLogo());

                return card;
        }

        private Div crearGoogleChart() {
                Div chartDiv = new Div();
                chartDiv.setId("google-bar-chart");
                chartDiv.setWidth("650px");
                chartDiv.setHeight("400px");
                chartDiv.getStyle().set("margin", "2rem auto");

                // Mensaje de carga inicial
                chartDiv.getElement().setProperty("innerHTML",
                                "<div style='text-align:center; padding:20px;'>Cargando datos...</div>");

                // Script simplificado que funciona de manera más directa
                chartDiv.getElement().executeJs(
                                "// Cargar Google Charts primero\n" +
                                                "const loadCharts = () => {\n" +
                                                "  return new Promise((resolve) => {\n" +
                                                "    if (typeof google !== 'undefined' && google.charts) {\n" +
                                                "      resolve();\n" +
                                                "      return;\n" +
                                                "    }\n" +
                                                "    const script = document.createElement('script');\n" +
                                                "    script.src = 'https://www.gstatic.com/charts/loader.js';\n" +
                                                "    script.onload = () => {\n" +
                                                "      google.charts.load('current', {packages:['corechart']});\n" +
                                                "      google.charts.setOnLoadCallback(() => resolve());\n" +
                                                "    };\n" +
                                                "    document.head.appendChild(script);\n" +
                                                "  });\n" +
                                                "};\n" +
                                                "\n" +
                                                "// Función para dibujar el gráfico\n" +
                                                "const drawChart = (data) => {\n" +
                                                "  const element = document.getElementById('google-bar-chart');\n" +
                                                "  if (!element) return console.error('No se encontró el elemento google-bar-chart');\n"
                                                +
                                                "  \n" +
                                                "  try {\n" +
                                                "    // Crear datos de ejemplo si no hay datos\n" +
                                                "    if (!data || !data.datosPorMes || data.datosPorMes.length === 0) {\n"
                                                +
                                                "      element.innerHTML = '<div style=\"text-align:center; padding:20px;\">No hay datos disponibles</div>';\n"
                                                +
                                                "      return;\n" +
                                                "    }\n" +
                                                "    \n" +
                                                "    // Obtener los datos por mes del objeto de métricas\n" +
                                                "    const datosPorMes = data.datosPorMes;\n" +
                                                "    \n" +
                                                "    // Datos fijos si hay problemas (esto siempre funcionará)\n" +
                                                "    const dataTable = new google.visualization.DataTable();\n" +
                                                "    dataTable.addColumn('string', 'Mes');\n" +
                                                "    \n" +
                                                "    // Obtener nombres únicos de tareas\n" +
                                                "    const tareas = [...new Set(datosPorMes.map(d => d.nombre))];\n" +
                                                "    tareas.forEach(tarea => {\n" +
                                                "      dataTable.addColumn('number', tarea);\n" +
                                                "    });\n" +
                                                "    \n" +
                                                "    // Agrupar por mes\n" +
                                                "    const mesesTexto = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',\n"
                                                +
                                                "                       'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];\n"
                                                +
                                                "    \n" +
                                                "    // Primero agrupar por mes\n" +
                                                "    const porMes = {};\n" +
                                                "    datosPorMes.forEach(d => {\n" +
                                                "      const mesTexto = mesesTexto[d.mes-1];\n" +
                                                "      if (!porMes[mesTexto]) porMes[mesTexto] = {};\n" +
                                                "      porMes[mesTexto][d.nombre] = d.cantidad;\n" +
                                                "    });\n" +
                                                "    \n" +
                                                "    // Convertir a filas\n" +
                                                "    Object.keys(porMes).forEach(mes => {\n" +
                                                "      const fila = [mes];\n" +
                                                "      tareas.forEach(tarea => {\n" +
                                                "        fila.push(porMes[mes][tarea] || 0);\n" +
                                                "      });\n" +
                                                "      dataTable.addRow(fila);\n" +
                                                "    });\n" +
                                                "    \n" +
                                                "    const options = {\n" +
                                                "      title: 'Tareas completadas por mes',\n" +
                                                "      legend: { position: 'top' },\n" +
                                                "      vAxis: { title: 'Cantidad' },\n" +
                                                "      hAxis: { title: 'Mes' },\n" +
                                                "      chartArea: { width: '70%' }\n" +
                                                "    };\n" +
                                                "    \n" +
                                                "    const chart = new google.visualization.ColumnChart(element);\n" +
                                                "    chart.draw(dataTable, options);\n" +
                                                "  } catch (error) {\n" +
                                                "    console.error('Error dibujando gráfico:', error);\n" +
                                                "    element.innerHTML = '<div style=\"text-align:center; color:red; padding:20px;\">Error al dibujar el gráfico: ' + error.message + '</div>';\n"
                                                +
                                                "  }\n" +
                                                "};\n" +
                                                "\n" +
                                                "// Iniciar proceso\n" +
                                                "const initChart = async () => {\n" +
                                                "  try {\n" +
                                                "    // Cargar biblioteca de gráficos\n" +
                                                "    await loadCharts();\n" +
                                                "    \n" +
                                                "    // Obtener datos de métricas completas\n" +
                                                "    const response = await fetch('/api/tareas/dashboard-metrics');\n" +
                                                "    if (!response.ok) {\n" +
                                                "      throw new Error('Error en el servidor: ' + response.status);\n" +
                                                "    }\n" +
                                                "    const data = await response.json();\n" +
                                                "    \n" +
                                                "    // Dibujar gráfico\n" +
                                                "    drawChart(data);\n" +
                                                "  } catch (error) {\n" +
                                                "    console.error('Error inicializando gráfico:', error);\n" +
                                                "    const element = document.getElementById('google-bar-chart');\n" +
                                                "    if (element) {\n" +
                                                "      element.innerHTML = '<div style=\"text-align:center; color:red; padding:20px;\">Error: ' + error.message + '</div>';\n"
                                                +
                                                "    }\n" +
                                                "  }\n" +
                                                "};\n" +
                                                "\n" +
                                                "// Ejecutar\n" +
                                                "initChart();");

                return chartDiv;
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
                                crearGoogleChart() // Después el gráfico
                );

                return dashboardContainer;
        }

        // Método que crea el panel de métricas
        private HorizontalLayout crearMetricas() {
                HorizontalLayout metricas = new HorizontalLayout();
                metricas.setWidthFull();
                metricas.setJustifyContentMode(JustifyContentMode.CENTER);
                metricas.setSpacing(true);
                metricas.setPadding(true);

                // Crear tarjetas de métricas
                metricas.add(
                                crearTarjetaMetrica("Total Tareas", "totalTareas", "...",
                                                LumoUtility.TextColor.PRIMARY),
                                crearTarjetaMetrica("Completadas", "completadas", "...", LumoUtility.TextColor.SUCCESS),
                                crearTarjetaMetrica("En Curso", "enCurso", "...", LumoUtility.TextColor.WARNING),
                                crearTarjetaMetrica("Pendientes", "pendientes", "...", LumoUtility.TextColor.ERROR));

                // Script mejorado para cargar y mostrar las métricas
                metricas.getElement().executeJs(
                                "console.log('Iniciando carga de métricas...');" +
                                                "setTimeout(() => {" +
                                                "  fetch('/api/tareas/dashboard-metrics')" +
                                                "  .then(response => {" +
                                                "    console.log('Respuesta recibida:', response.status);" +
                                                "    if (!response.ok) {" +
                                                "      throw new Error('Error en el servidor: ' + response.status);" +
                                                "    }" +
                                                "    return response.json();" +
                                                "  })" +
                                                "  .then(data => {" +
                                                "    console.log('Datos recibidos:', data);" +
                                                "    // Actualizar métricas con datos reales" +
                                                "    document.getElementById('totalTareas').textContent = data.totalTareas || 0;"
                                                +
                                                "    document.getElementById('completadas').textContent = data.tareasCompletadas || 0;"
                                                +
                                                "    document.getElementById('enCurso').textContent = data.tareasEnCurso || 0;"
                                                +
                                                "    document.getElementById('pendientes').textContent = data.tareasPendientes || 0;"
                                                +
                                                "  })" +
                                                "  .catch(error => {" +
                                                "    console.error('Error cargando métricas:', error);" +
                                                "    document.getElementById('totalTareas').textContent = 'Error';" +
                                                "    document.getElementById('completadas').textContent = 'Error';" +
                                                "    document.getElementById('enCurso').textContent = 'Error';" +
                                                "    document.getElementById('pendientes').textContent = 'Error';" +
                                                "  });" +
                                                "}, 500);" // Pequeño retraso para asegurar que los elementos están
                                                           // renderizados
                );

                return metricas;
        }

        // Método auxiliar para crear una tarjeta de métrica individual
        private Div crearTarjetaMetrica(String titulo, String id, String valorInicial, String colorClass) {
                Div tarjeta = new Div();
                tarjeta.addClassNames(
                                LumoUtility.Background.BASE,
                                LumoUtility.BoxShadow.SMALL,
                                LumoUtility.BorderRadius.MEDIUM,
                                LumoUtility.Padding.MEDIUM);
                tarjeta.setWidth("150px");
                tarjeta.setHeight("100px");

                Span tituloSpan = new Span(titulo);
                tituloSpan.addClassNames(
                                LumoUtility.FontSize.SMALL,
                                LumoUtility.TextColor.SECONDARY,
                                LumoUtility.TextAlignment.CENTER);
                tituloSpan.getStyle().set("display", "block");

                Span valorSpan = new Span(valorInicial);
                valorSpan.setId(id);
                valorSpan.addClassNames(
                                LumoUtility.FontSize.XXXLARGE,
                                colorClass,
                                LumoUtility.FontWeight.BOLD,
                                LumoUtility.TextAlignment.CENTER);
                valorSpan.getStyle().set("display", "block");
                valorSpan.getStyle().set("margin-top", "10px");

                tarjeta.add(tituloSpan, valorSpan);
                return tarjeta;
        }

}
