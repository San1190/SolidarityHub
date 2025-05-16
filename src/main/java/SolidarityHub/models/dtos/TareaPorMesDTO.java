package SolidarityHub.models.dtos;

// DTO para el dashboard
public class TareaPorMesDTO {
    private Integer mes;
    private String nombre;
    private Long cantidad;

    public TareaPorMesDTO(Integer mes, String nombre, Long cantidad) {
        this.mes = mes;
        this.nombre = nombre;
        this.cantidad = cantidad;
    }
}
