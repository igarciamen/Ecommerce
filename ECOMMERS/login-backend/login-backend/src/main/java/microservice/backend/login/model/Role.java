package microservice.backend.login.model;

import jakarta.persistence.*;
import microservice.backend.login.enums.ERole;

@Entity
@Table(schema = "public", name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;

    // --- Constructores ---
    public Role() {}

    public Role(ERole name) {this.name = name;}

    // --- Getters y Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public ERole getName() { return name; }
    public void setName(ERole name) { this.name = name; }
}
