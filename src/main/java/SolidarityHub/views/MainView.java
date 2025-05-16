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

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;

@Route(value = "main", layout = MainLayout.class)
@PageTitle("Main | SolidarityHub")
public class MainView extends VerticalLayout {

        private TareaServicio tareaServicio;

        private Image logo;

        public MainView(UsuarioServicio usuarioServicio, TareaServicio tareaServicio) {
                this.tareaServicio = tareaServicio;

                setSizeFull();
                setSpacing(false);
                setPadding(true);
                setAlignItems(Alignment.CENTER);

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
                add(card, crearGoogleChart());
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

        private Div crearGoogleChart() {
                Div chartDiv = new Div();
                chartDiv.setId("google-bar-chart");
                chartDiv.setWidth("650px");
                chartDiv.setHeight("400px");
                chartDiv.getStyle().set("margin", "2rem auto");

                chartDiv.getElement().executeJs(
                                "fetch('/api/tareas/dashboard')" +
                                                ".then(response => response.json())" +
                                                ".then(datos => {" +
                                                "  const meses = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];"
                                                +
                                                "  let dataArr = [['Mes', 'Nombre', 'Cantidad']];" +
                                                "  datos.forEach(d => dataArr.push([meses[d.mes-1], d.nombre, d.cantidad]));"
                                                +
                                                "  if (!window.googleChartsLoaded) {" +
                                                "    var script = document.createElement('script');" +
                                                "    script.src = 'https://www.gstatic.com/charts/loader.js';" +
                                                "    script.onload = function() { window.googleChartsLoaded = true; drawChart(); };"
                                                +
                                                "    document.head.appendChild(script);" +
                                                "  } else { drawChart(); }" +
                                                "  function drawChart() {" +
                                                "    google.charts.load('current', {packages:['corechart']});" +
                                                "    google.charts.setOnLoadCallback(function() {" +
                                                "      var data = google.visualization.arrayToDataTable(dataArr);" +
                                                "      var options = {" +
                                                "        title: 'Tareas por nombre y mes'," +
                                                "        legend: { position: 'top' }," +
                                                "        chartArea: {width: '70%'}" +
                                                "      };" +
                                                "      var chart = new google.visualization.ColumnChart(document.getElementById('google-bar-chart'));"
                                                +
                                                "      chart.draw(data, options);" +
                                                "    });" +
                                                "  }" +
                                                "});");
                return chartDiv;
        }

}
