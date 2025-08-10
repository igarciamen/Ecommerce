package microservice.backend.login;

import microservice.backend.login.Repository.RoleRepository;
import microservice.backend.login.Repository.UserRepository;
import microservice.backend.login.enums.ERole;
import microservice.backend.login.model.Role;
import microservice.backend.login.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("!test")
public class DataLoader {

    @Bean
    public CommandLineRunner initData(
            RoleRepository roleRepo,
            UserRepository userRepo,
            PasswordEncoder encoder,
            @Value("${app.admin.username:}") String adminUser,
            @Value("${app.admin.password:}") String adminPass
    ) {
        return args -> {
            // 1) Crear roles si no existen
            if (roleRepo.count() == 0) {
                roleRepo.save(new Role(ERole.ROLE_USER));
                roleRepo.save(new Role(ERole.ROLE_ADMIN));
                roleRepo.save(new Role(ERole.ROLE_SELLER));  // <-- añadimos ROLE_SELLER
            }

            // 2) Crear usuario administrador inicial
            if (adminUser != null && !adminUser.isBlank()) {
                if (!userRepo.existsByUsername(adminUser)) {
                    String adminEmail = adminUser + "@admin.local";
                    User admin = new User(adminUser, adminEmail, encoder.encode(adminPass));
                    Role adminRole = roleRepo.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN no existe"));
                    admin.getRoles().add(adminRole);
                    userRepo.save(admin);
                }
            }
        };
    }
}
