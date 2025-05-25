package SolidarityHub.strategy;

import SolidarityHub.models.dtos.DashboardMetricasDTO;
import com.vaadin.flow.component.Component;

public class ContextoMetricas {
    private EstrategiaMetrica estrategia;

    public void setEstrategia(EstrategiaMetrica estrategia) {
        this.estrategia = estrategia;
    }

    public Component ejecutarEstrategia(DashboardMetricasDTO metrica) {
        return estrategia.ejecutar(metrica);
    }
}
