package SolidarityHub.models;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.springframework.cglib.core.Local;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


@Entity
@DiscriminatorValue("voluntario")
public class Voluntario extends Usuario {

    private List<Habilidad> habilidades;
    private LocalTime horaInicioTrabajo;
    private LocalTime horaFinTrabajo;

    public Voluntario () {}

    public Voluntario(String dni, String nombre, String apellidos, String email, String password, String telefono, String direccion, byte[] foto, List<Habilidad> habilidades, LocalTime horaInicioTrabajo, LocalTime horaFinTrabajo) {
        super(dni, nombre, apellidos, email, password, telefono, direccion, foto);
        this.habilidades = habilidades;
        this.horaInicioTrabajo = horaInicioTrabajo;
        this.horaFinTrabajo = horaFinTrabajo;
    }


    @Override
    public String getTipoUsuario() {
        return "voluntario";
    }

    public List<Habilidad> getHabilidades() {
        return habilidades;
    }
    public void setHabilidades(List<Habilidad> habilidades) {
        this.habilidades = habilidades;
    }


    public LocalTime getHoraInicioTrabajo() {
        return horaInicioTrabajo;
    }
    public void setHoraInicioTrabajo(LocalTime horaInicioTrabajo) {
        this.horaInicioTrabajo = horaInicioTrabajo;
    }

    public LocalTime getHoraFinTrabajo() {
        return horaFinTrabajo;
    }

    public void setHoraFinTrabajo(LocalTime horaFinTrabajo) {
        this.horaFinTrabajo = horaFinTrabajo;
    }



    
}
