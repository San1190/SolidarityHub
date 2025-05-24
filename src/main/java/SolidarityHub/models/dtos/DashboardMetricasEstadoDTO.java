package SolidarityHub.models.dtos;

import java.util.ArrayList;
import java.util.List;

// DTO para métricas completas del dashboard para estado
public class DashboardMetricasEstadoDTO {
    public long totalTareas;
    public long tareasCompletadas;
    public long tareasEnCurso;
    public long tareasPendientes;
    public double promedioPorMes;
    public List<TareaPorMesDTO> datosPorMes;

    public DashboardMetricasEstadoDTO() {
        this.totalTareas = 0;
        this.tareasCompletadas = 0;
        this.tareasEnCurso = 0;
        this.tareasPendientes = 0;
        this.promedioPorMes = 0.0;
        this.datosPorMes = new ArrayList<>();
    }

    public List<TareaPorMesDTO> getDatosPorMes() {
        return datosPorMes;
    }

    public void setDatosPorMes(List<TareaPorMesDTO> datosPorMes) {
        this.datosPorMes = datosPorMes;
    }

    public long getTotalTareas() {
        return totalTareas;
    }

    public void setTotalTareas(long totalTareas) {
        this.totalTareas = totalTareas;
    }

    public long getTareasCompletadas() {
        return tareasCompletadas;
    }

    public void setTareasCompletadas(long tareasCompletadas) {
        this.tareasCompletadas = tareasCompletadas;
    }

    public long getTareasEnCurso() {
        return tareasEnCurso;
    }

    public void setTareasEnCurso(long tareasEnCurso) {
        this.tareasEnCurso = tareasEnCurso;
    }

    public long getTareasPendientes() {
        return tareasPendientes;
    }

    public void setTareasPendientes(long tareasPendientes) {
        this.tareasPendientes = tareasPendientes;
    }

    public double getPromedioPorMes() {
        return promedioPorMes;
    }

    public void setPromedioPorMes(double promedioPorMes) {
        this.promedioPorMes = promedioPorMes;
    }
}
