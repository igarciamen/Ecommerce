// src/main/java/backend/microservicio/carts/controller/CartController.java
package backend.microservicio.carts.controller;

import backend.microservicio.carts.model.Cart;
import backend.microservicio.carts.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:4200")
public class CartController {

    private final CartService cartService;
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** Ver carrito de un usuario */
    @GetMapping("/{userId}")
    public ResponseEntity<Cart> viewCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }

    /** Agregar producto (sin DTO) */
    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity
    ) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("quantity debe ser al menos 1");
        }
        return ResponseEntity.ok(cartService.addItem(userId, productId, quantity));
    }

    /** Vaciar carrito */
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Cart> clearCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.clearCart(userId));
    }

    /** Contar unidades totales */
    @GetMapping("/{userId}/count/units")
    public ResponseEntity<Integer> countUnits(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.countUnits(userId));
    }

    /** Contar ítems distintos */
    @GetMapping("/{userId}/count/distinct")
    public ResponseEntity<Integer> countDistinct(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.countDistinctItems(userId));
    }

    /** Actualizar cantidad de un ítem (sin DTO) */
    @PutMapping("/item")
    public ResponseEntity<Cart> updateItem(
            @RequestParam Long userId,
            @RequestParam Long itemId,
            @RequestParam Integer quantity
    ) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("quantity debe ser >= 0");
        }
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, itemId, quantity));
    }

    /** Manejo simple de errores (opcional) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
