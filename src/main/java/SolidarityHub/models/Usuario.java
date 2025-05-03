// Usuario.java
package SolidarityHub.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonGetter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "tipo_usuario",
    visible = true
)
// Definir los subtipos de Usuario (Afectado y Voluntario)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Afectado.class, name = "afectado"),
    @JsonSubTypes.Type(value = Voluntario.class, name = "voluntario"),
    @JsonSubTypes.Type(value = Gestor.class, name = "gestor")
})
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String dni;
    public String nombre;
    public String apellidos;
    public String email;
    public String password;
    public String telefono;
    public String direccion;
    
    @Lob
    public byte[] foto;


    public Usuario() {}

    public Usuario(String dni,String nombre, String apellidos, String email, String password, String telefono, String direccion, byte[] foto) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.direccion = direccion;
        this.foto = foto;
    }

    @JsonGetter("tipo_usuario") // Getter para el tipo de usuario
    public abstract String getTipoUsuario(); // Método abstracto para obtener el tipo de usuario

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) { this.foto = foto; }



}