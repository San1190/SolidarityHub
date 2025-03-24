package SolidarityHub.models;

import org.checkerframework.checker.units.qual.A;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;



@Entity
@DiscriminatorValue("afectado")
public class Afectado extends Usuario {

    private Necesidad[] necesidades;

    public Afectado() {}

    public Afectado(String dni, String nombre, String apellidos, String email, String password, String telefono, String direccion, byte[] foto, Necesidad[] necesidades) {
        super(dni, nombre, apellidos, email, password, telefono, direccion, foto);
        this.necesidades = necesidades;
    }


    @Override
    public String getTipoUsuario() {
        return "afectado";
    }
    
    public Necesidad[] getNecesidades() {
        return necesidades;
    }

    public void setNecesidades(Necesidad[] necesidades) {
        this.necesidades = necesidades;
    }

    
}
