// src/main/java/microservice/backend/login/payload/response/UserInfoResponse.java
package microservice.backend.login.payload.response;

import java.util.Set;

public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;

    public UserInfoResponse() {}

    /**
     * Constructor completo incluyendo ID de usuario.
     */
    public UserInfoResponse(Long id, String username, String email, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    /**
     * Constructor antiguo para compatibilidad:
     * devuelve null en el campo id.
     */
    public UserInfoResponse(String username, String email, Set<String> roles) {
        this(null, username, email, roles);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
