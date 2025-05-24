package SolidarityHub.strategy;

import SolidarityHub.models.dtos.DashboardMetricasEstadoDTO;
import com.vaadin.flow.component.Component;

public class ContextoMetricas {
    private EstrategiaMetrica estrategia;

    public void setEstrategia(EstrategiaMetrica estrategia) {
        this.estrategia = estrategia;
    }

    public Component ejecutarEstrategia(DashboardMetricasEstadoDTO metrica) {
        return estrategia.ejecutar(metrica);
    }
}
