package SolidarityHub.factories;

import SolidarityHub.models.Habilidad;
import SolidarityHub.models.Necesidad;
import SolidarityHub.models.Usuario;

import java.util.List;

public abstract class FabricaGeneral {
    public abstract Usuario crearUsuario(String tipoDeUsuario, String dni, String nombre, String apellidos, String email, String password, String telefono, String direccion, byte[] foto, List<Necesidad> necesidades, List<Habilidad> habilidades,  List<String> diasDisponibles, String turnoDisponibilidad);
}
