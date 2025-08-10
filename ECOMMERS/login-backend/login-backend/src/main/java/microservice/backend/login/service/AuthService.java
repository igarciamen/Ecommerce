package microservice.backend.login.service;

import microservice.backend.login.model.User;
import microservice.backend.login.payload.response.UserInfoResponse;
import microservice.backend.login.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserService userService,
            JwtUtils jwtUtils,
            PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    public String authenticate(String login, String rawPassword) {
        User user;
        try {
            user = userService.findByUsernameOrEmail(login);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }
        return jwtUtils.generateJwtToken(user);
    }

    public UserInfoResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        User user = userService.findByUsername(authentication.getName());
        var roles = user.getRoles()
                .stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
        return new UserInfoResponse(user.getUsername(), user.getEmail(), roles);
    }
}
