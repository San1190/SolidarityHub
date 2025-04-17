// FabricaUsuario.java
package SolidarityHub.factories;

import SolidarityHub.models.*;
import java.util.List;

public class FabricaUsuario extends FabricaGeneral {
    public FabricaUsuario(){
        // Constructor vacío
    }

    @Override
    public Usuario crearUsuario(String tipoDeUsuario, String dni, String nombre, String apellidos, String email, String password, String telefono, String direccion, byte[] foto, List<Necesidad> necesidades, List<Habilidad> habilidades, List<String> diasDisponibles, String turnoDisponibilidad) {
        return switch (tipoDeUsuario) {
            case "Voluntario" ->
                    new Voluntario(dni, nombre, apellidos, email, password, telefono, direccion,
                            foto, habilidades, diasDisponibles, turnoDisponibilidad, null);
            case "Afectado" ->
                    new Afectado(dni, nombre, apellidos, email, password, telefono, direccion, foto, necesidades);
            default -> null;
        };
    }
}
