// src/main/java/microservice/backend/login/service/UserService.java
package microservice.backend.login.service;

import microservice.backend.login.Repository.RoleRepository;
import microservice.backend.login.Repository.UserRepository;
import microservice.backend.login.enums.ERole;
import microservice.backend.login.model.Role;
import microservice.backend.login.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo,
                       RoleRepository roleRepo,
                       PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder  = encoder;
    }

    /** Crea un nuevo usuario con roles USER y SELLER */
    public User registerUser(String username, String email, String rawPassword, String confirmPassword) {
        if (!rawPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Usuario ya existe: " + username);
        }
        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email ya está en uso: " + email);
        }

        String encoded = encoder.encode(rawPassword);
        User user = new User(username, email, encoded);

        // Rol USER
        Role roleUser = roleRepo.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER no existe en BD"));
        user.getRoles().add(roleUser);

        // Rol SELLER
        Role roleSeller = roleRepo.findByName(ERole.ROLE_SELLER)
                .orElseThrow(() -> new IllegalStateException("ROLE_SELLER no existe en BD"));
        user.getRoles().add(roleSeller);

        return userRepo.save(user);
    }

    /** Recupera usuario por username */
    public User findByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
    }

    /** Recupera usuario por username o email */
    public User findByUsernameOrEmail(String login) {
        return userRepo.findByUsernameOrEmail(login, login)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + login));
    }


    /** Recupera usuario por ID */
    public User findById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }






}
