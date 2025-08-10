// src/main/java/microservice/backend/login/utils/JwtUtils.java
package microservice.backend.login.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import microservice.backend.login.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private final Key key;
    private final int jwtExpirationMs;

    public JwtUtils(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expirationMs}") int jwtExpirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Genera un JWT con:
     *  - subject = username
     *  - claim "roles" = lista de roles
     *  - claim "userId" = ID del usuario
     */
    public String generateJwtToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", roles)
                .claim("userId", user.getId())       // <— añadido
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
