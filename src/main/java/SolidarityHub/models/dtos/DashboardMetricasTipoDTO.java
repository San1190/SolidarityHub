package SolidarityHub.models.dtos;

import java.util.List;

public class DashboardMetricasTipoDTO {
    public String nombre;
    public long cantidad;
    public List<TareaPorMesDTO> datosPorMes;

    public DashboardMetricasTipoDTO(String nombre, long cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public List<TareaPorMesDTO> getDatosPorMes() {
        return datosPorMes;
    }

    public String getNombre() {
        return nombre;
    }

    public long getCantidad() {
        return cantidad;
    }

    public void setDatosPorMes(List<TareaPorMesDTO> datosPorMes) {
        this.datosPorMes = datosPorMes;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCantidad(long cantidad) {
        this.cantidad = cantidad;
    }
}
