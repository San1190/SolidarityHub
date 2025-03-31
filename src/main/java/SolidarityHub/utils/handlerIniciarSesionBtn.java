package SolidarityHub.utils;

import SolidarityHub.models.Voluntario;
import SolidarityHub.views.LoginView;
import SolidarityHub.models.Afectado;
import SolidarityHub.models.Usuario;
import org.springframework.web.client.RestTemplate;

public class handlerIniciarSesionBtn {

    public static void pulsarIniciarSesionBtn() {

        LoginView loginView = new LoginView();

        String tipoUsuarioSeleccionado = loginView.getTipoUsuario().getValue(); // Esto obtiene el tipo seleccionado

        // Dependiendo de la selección, se crea el tipo de usuario correspondiente
        Usuario usuario = null;

        if ("Voluntario".equals(tipoUsuarioSeleccionado)) {
            usuario = new Voluntario();
            // Completa los campos del voluntario, por ejemplo:
            usuario.setEmail(loginView.getEmailField().getValue());
            usuario.setPassword(loginView.getContraseñaField().getValue());
            // Otros campos según sea necesario
        } else if ("Afectado".equals(tipoUsuarioSeleccionado)) {
            usuario = new Afectado();
            // Completa los campos del afectado
            usuario.setEmail(loginView.getEmailField().getValue());
            usuario.setPassword(loginView.getContraseñaField().getValue());
            // Otros campos según sea necesario
        }

        // Aquí puedes realizar la llamada al backend para crear el usuario
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/api/usuarios/registrar";

        // Aquí sería el lugar para llamar a la API para registrar el usuario
        restTemplate.postForObject(url, usuario, Usuario.class);
    }
}
