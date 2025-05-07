package SolidarityHub.views;

import SolidarityHub.services.UsuarioServicio;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import software.xdev.vaadin.maps.leaflet.MapContainer;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.raster.LTileLayer;
import software.xdev.vaadin.maps.leaflet.layer.ui.LMarker;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;
import software.xdev.vaadin.maps.leaflet.registry.LDefaultComponentManagementRegistry;

import java.util.HashMap;
import java.util.Map;

@Route(value = "main", layout = MainLayout.class)
@PageTitle("Main | SolidarityHub")
public class MainView extends VerticalLayout {

    public MainView(UsuarioServicio usuarioServicio) {
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

        

        add(title);
        add(crearMapaContainer());
    }

    

    private Component crearMapaContainer() {
        LComponentManagementRegistry registry = new LDefaultComponentManagementRegistry(this);
        MapContainer container = new MapContainer(registry);
        container.setSizeFull();
        container.getElement().getStyle()
            .set("border-radius", "8px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
            .set("margin", "1rem auto")
            .set("max-width", "1200px")
            .set("width", "95%")
            .set("min-height", "500px");

        LMap map = container.getlMap();
        map.addLayer(LTileLayer.createDefaultForOpenStreetMapTileServer(registry));
        map.setView(new LLatLng(registry, 39.4699, -0.3763), 10);
        añadirMarcadoresValencia(map, registry);
        return container;
    }

    private void añadirMarcadoresValencia(LMap map, LComponentManagementRegistry registry) {
        Map<String, double[]> ubicaciones = new HashMap<>();
        ubicaciones.put("Centro de distribución de alimentos", new double[]{39.4683, -0.3768});
        ubicaciones.put("Albergue temporal",             new double[]{39.4720, -0.3820});
        ubicaciones.put("Punto de asistencia médica",    new double[]{39.4650, -0.3730});
        ubicaciones.put("Almacén de suministros",        new double[]{39.4750, -0.3680});
        ubicaciones.put("Centro logístico",             new double[]{39.4630, -0.3850});
        ubicaciones.put("Centro de coordinación",       new double[]{39.4699, -0.3763});
        ubicaciones.put("Punto de encuentro voluntarios",new double[]{39.4670, -0.3790});
        ubicaciones.put("Zona inundada",                new double[]{39.4580, -0.3700});
        ubicaciones.put("Área de evacuación",           new double[]{39.4620, -0.3650});

        for (Map.Entry<String, double[]> entry : ubicaciones.entrySet()) {
            String nombre = entry.getKey();
            double[] coords = entry.getValue();
            LLatLng ll = new LLatLng(registry, coords[0], coords[1]);
            LMarker marker = new LMarker(registry, ll);
            marker.bindPopup("<b>" + nombre + "</b>");
            marker.addTo(map);
        }

        Notification notification = new Notification(
            "Mapa cargado con puntos de interés en Valencia", 3000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.open();
    }
}
