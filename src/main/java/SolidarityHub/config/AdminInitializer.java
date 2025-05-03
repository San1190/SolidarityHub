package SolidarityHub.config;

import SolidarityHub.models.Gestor;
import SolidarityHub.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@gmail.com";

        // Si no existe ya, lo creamos
        if (usuarioRepositorio.findByEmail(adminEmail).isEmpty()) {
            Gestor admin = new Gestor(
                "00000000A",
                "Admin",
                "Del Sistema",
                adminEmail,
                "admin",  // Puedes usar un encriptador si aplicas seguridad
                "000000000",
                "Sistema Central",
                null // o una imagen en bytes si tienes una
            );

            usuarioRepositorio.save(admin);
            System.out.println("✅ Gestor admin creado");
        } else {
            System.out.println("ℹ️ Gestor admin ya existe");
        }
    }
}
