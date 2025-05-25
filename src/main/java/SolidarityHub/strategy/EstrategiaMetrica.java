package SolidarityHub.strategy;

import com.vaadin.flow.component.Component;

import SolidarityHub.models.dtos.DashboardMetricasDTO;

public interface EstrategiaMetrica {
    Component ejecutar(DashboardMetricasDTO metrica);
}
