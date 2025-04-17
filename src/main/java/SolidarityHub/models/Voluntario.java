package SolidarityHub.models;

import java.util.List;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("voluntario")
public class Voluntario extends Usuario {

    private List<Habilidad> habilidades;
    
    // Nuevos campos para guardar días y turno
    @ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
    private List<String> diasDisponibles; // Por ejemplo: "Lunes", "Martes", ...
    
    private String turnoDisponibilidad; // Por ejemplo: "Mañana", "Tarde", "Día Entero"
    
    private Double radioAccion; // Radio de acción en kilómetros para asignación de tareas
    

    public Voluntario() {}

    public Voluntario(String dni, String nombre, String apellidos, String email, String password, String telefono, String direccion, byte[] foto,
                      List<Habilidad> habilidades, List<String> diasDisponibles, String turnoDisponibilidad, Double radioAccion) {
        super(dni, nombre, apellidos, email, password, telefono, direccion, foto);
        this.habilidades = habilidades;
        this.diasDisponibles = diasDisponibles;
        this.turnoDisponibilidad = turnoDisponibilidad;
        this.radioAccion = radioAccion;
    }

    @Override
    public String getTipoUsuario() {
        return "voluntario";
    }

    // Getters y setters para habilidades, días y turno

    public List<Habilidad> getHabilidades() {
        return habilidades;
    }
    public void setHabilidades(List<Habilidad> habilidades) {
        this.habilidades = habilidades;
    }

    public List<String> getDiasDisponibles() {
        return diasDisponibles;
    }
    public void setDiasDisponibles(List<String> diasDisponibles) {
        this.diasDisponibles = diasDisponibles;
    }

    public String getTurnoDisponibilidad() {
        return turnoDisponibilidad;
    }
    public void setTurnoDisponibilidad(String turnoDisponibilidad) {
        this.turnoDisponibilidad = turnoDisponibilidad;
    }

    public Double getRadioAccion() {
        return radioAccion;
    }

    public void setRadioAccion(Double radioAccion) {
        this.radioAccion = radioAccion;
    }
}
