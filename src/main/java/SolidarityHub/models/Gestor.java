package SolidarityHub.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("gestor")
public class Gestor extends Usuario {

    public Gestor() {
        
    }

    public Gestor(String dni, String nombre, String apellidos, String email, String password, String telefono, String direccion, byte[] foto) {
        super(dni, nombre, apellidos, email, password, telefono, direccion, foto);
    }

    @Override
    public String getTipoUsuario() {
        return "gestor";
    }

}
