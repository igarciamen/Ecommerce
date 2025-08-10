// src/main/java/backend/microservicio/carts/model/Cart.java
package backend.microservicio.carts.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; // <-- nuevo

@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del usuario (viene desde otro microservicio) */
    private Long userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CartItem> items = new ArrayList<>();

    /** Objeto completo del usuario (NO se persiste) */
    @Transient
    private Map<String, Object> user; // <-- nuevo

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public Map<String, Object> getUser() { return user; }                 // <-- nuevo
    public void setUser(Map<String, Object> user) { this.user = user; }   // <-- nuevo
}
