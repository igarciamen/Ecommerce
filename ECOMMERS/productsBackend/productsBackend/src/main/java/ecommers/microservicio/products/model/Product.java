// src/main/java/ecommers/microservicio/products/model/Product.java
package ecommers.microservicio.products.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Map; // <-- nuevo

@Entity
@Table(name = "products", schema = "public")
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @NotNull @PositiveOrZero
    @Column(name = "price", nullable = false)
    private Double price;

    @NotNull @Min(0)
    @Column(name = "stock", nullable = false)
    private Integer stock;

    /** Solo guardamos el ID de categoría */
    @NotNull
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /** ID del vendedor que subió el producto */
    @NotNull
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    /** Objeto completo del vendedor (microservicio Login) */
    @Transient
    private Map<String, Object> seller;   // ya lo tenías

    /** Objeto completo de la categoría (microservicio Categories) */
    @Transient
    private Map<String, Object> category; // <-- nuevo

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // --- Getters y Setters (solo muestro los nuevos al final) ---
    public Map<String, Object> getSeller() { return seller; }
    public void setSeller(Map<String, Object> seller) { this.seller = seller; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Object> getCategory() { return category; }          // <-- nuevo
    public void setCategory(Map<String, Object> category) { this.category = category; } // <-- nuevo
}
