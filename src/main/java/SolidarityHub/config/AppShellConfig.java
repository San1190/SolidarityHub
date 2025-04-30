package SolidarityHub.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;


/**
 * Configuración del shell de la aplicación.
 * Esta clase centraliza las anotaciones de configuración de Vaadin.
 */
@Push
@PWA(name = "SolidarityHub", shortName = "SH")
public class AppShellConfig implements AppShellConfigurator {
    // La implementación de AppShellConfigurator no requiere métodos adicionales
}