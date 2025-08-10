// src/main/java/backend/microservicio/carts/model/CartItem.java
package backend.microservicio.carts.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Map; // <-- nuevo

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    private Long productId;

    private Integer quantity;

    /** Objeto completo del producto (NO se persiste) */
    @Transient
    private Map<String, Object> product; // <-- nuevo

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Map<String, Object> getProduct() { return product; }                 // <-- nuevo
    public void setProduct(Map<String, Object> product) { this.product = product; } // <-- nuevo
}
