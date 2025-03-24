package SolidarityHub.models;

import java.time.LocalTime;
import java.util.Set;

import org.springframework.cglib.core.Local;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


@Entity
@DiscriminatorValue("voluntario")
public class Voluntario extends Usuario {

    private Habilidad[] habilidades;
    private LocalTime horaInicioTrabajo;
    private LocalTime horaFinTrabajo;

    public Voluntario () {}

    public Voluntario(String dni, String nombre, String apellidos, String email, String password, String telefono, String direccion, byte[] foto, Habilidad[] habilidades, LocalTime horaInicioTrabajo, LocalTime horaFinTrabajo) {
        super(dni, nombre, apellidos, email, password, telefono, direccion, foto);
        this.habilidades = habilidades;
        this.horaInicioTrabajo = horaInicioTrabajo;
        this.horaFinTrabajo = horaFinTrabajo;
    }


    @Override
    public String getTipoUsuario() {
        return "voluntario";
    }

    public Habilidad[] getHabilidades() {
        return habilidades;
    }
    public void setHabilidades(Habilidad[] habilidades) {
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
