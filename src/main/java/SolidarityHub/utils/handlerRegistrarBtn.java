package SolidarityHub.utils;

import com.vaadin.flow.component.UI;

public class handlerRegistrarBtn {
    public static void pulsarRegistrarBtn() {
        // Al pulsar el botón registrar, ir a la ventana RegistroView
        UI.getCurrent().navigate("RegistroView");
    }
}
