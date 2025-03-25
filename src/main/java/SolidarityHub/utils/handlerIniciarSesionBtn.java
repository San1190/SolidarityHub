package SolidarityHub.utils;

import com.vaadin.flow.component.UI;

public class handlerIniciarSesionBtn {
    public static void pulsarIniciarSesionBtn() {
        // Al pulsar el botón Iniciar Sesión:
        // Comprobar credenciales, método backend

        // Ir a la ventana MainView
        UI.getCurrent().navigate("MainView");
    }
}
