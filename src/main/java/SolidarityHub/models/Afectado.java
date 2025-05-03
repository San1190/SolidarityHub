package SolidarityHub.models;
import jakarta.persistence.*;

import java.util.List;

@Entity
@DiscriminatorValue("afectado")
public class Afectado extends Usuario {

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Necesidad> necesidades;

    public Afectado() {}    

    public Afectado(String dni, String nombre, String apellidos, String email, String password, String telefono, String direccion, byte[] foto, List<Necesidad> necesidades) {
        super(dni, nombre, apellidos, email, password, telefono, direccion, foto);
        this.necesidades = necesidades;
    }

    @Override
    public String getTipoUsuario() {
        return "afectado";
    }

    public List<Necesidad> getNecesidades() {
        return necesidades;
    }

    public void setNecesidades(List<Necesidad> necesidades) {
        this.necesidades = necesidades;
    }
}
