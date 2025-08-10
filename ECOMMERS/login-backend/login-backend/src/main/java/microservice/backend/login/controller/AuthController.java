package microservice.backend.login.controller;

import jakarta.validation.Valid;
import microservice.backend.login.payload.request.LoginRequest;
import microservice.backend.login.payload.request.SignupRequest;
import microservice.backend.login.payload.response.JwtResponse;
import microservice.backend.login.payload.response.MessageResponse;
import microservice.backend.login.service.AuthService;
import microservice.backend.login.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping(path = "/signup",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody SignupRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Las contraseñas no coinciden"));
        }
        userService.registerUser(req.getUsername(), req.getEmail(), req.getPassword(), req.getConfirmPassword());
        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @PostMapping(path = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = authService.authenticate(req.getLogin(), req.getPassword());
        return ResponseEntity.ok(new JwtResponse(token));
    }
}
