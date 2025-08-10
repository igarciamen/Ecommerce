// src/main/java/microservice/backend/login/controller/UserController.java
package microservice.backend.login.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import microservice.backend.login.model.User;
import microservice.backend.login.payload.response.UserInfoResponse;
import microservice.backend.login.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Devuelve info del usuario autenticado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfoResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        User user = userService.findByUsername(authentication.getName());
        Set<String> roles = user.getRoles()
                .stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        // Construimos la respuesta incluyendo el ID
        UserInfoResponse resp = new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
        return ResponseEntity.ok(resp);
    }



    @Operation(summary = "Devuelve el usuario (incluye username) por ID")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfoResponse> getById(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            Set<String> roles = user.getRoles()
                    .stream()
                    .map(r -> r.getName().name())
                    .collect(Collectors.toSet());
            UserInfoResponse resp = new UserInfoResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    roles
            );
            return ResponseEntity.ok(resp);

        } catch (IllegalArgumentException ex) {
            // si no existe, devolvemos un usuario "dummy" pero sin error 500
            UserInfoResponse resp = new UserInfoResponse(
                    id,
                    "Desconocido",
                    "",
                    Collections.emptySet()
            );
            return ResponseEntity.ok(resp);
        }
    }








}
