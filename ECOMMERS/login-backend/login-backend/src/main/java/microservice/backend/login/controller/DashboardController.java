// src/main/java/com/example/loginroles/controller/DashboardController.java
package microservice.backend.login.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Operation(
        summary = "Vista protegida de dashboard",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<String> dashboard() {
        return ResponseEntity.ok("🏠 Bienvenido al dashboard protegido");
    }
}
