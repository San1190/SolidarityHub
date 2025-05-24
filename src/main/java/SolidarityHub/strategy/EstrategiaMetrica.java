package SolidarityHub.strategy;

import com.vaadin.flow.component.Component;

import SolidarityHub.models.dtos.DashboardMetricasEstadoDTO;

public interface EstrategiaMetrica {
    Component ejecutar(DashboardMetricasEstadoDTO metrica);
}
