package SolidarityHub.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings; 
import com.vaadin.flow.server.PWA;


/**
 * Configuración del shell de la aplicación.
 * Esta clase centraliza las anotaciones de configuración de Vaadin.
 */
@PWA(name = "SolidarityHub", shortName = "SH")
public class AppShellConfig implements AppShellConfigurator {
    @Override
    public void configurePage(AppShellSettings settings) {
        // Add favicon links here
        settings.addLink("icon", "/icons/icon-144x144.png");
        settings.addFavIcon("icon", "/icons/icon-144x144.png", "144-144"); 
    }
}