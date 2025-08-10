package microservice.backend.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoginApplication.class, args);

		// Mensaje claro en consola indicando que el servidor ha iniciado correctamente
		System.out.println("✅ Aplicación Spring Boot iniciada correctamente");

	}

}
