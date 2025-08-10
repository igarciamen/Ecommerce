// src/main/java/microservicio/backend/order/controller/OrderController.java
package microservicio.backend.order.controller;

import microservicio.backend.order.model.Order;
import microservicio.backend.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    /** Crea una orden: body JSON { "userId":1, "items":[{"productId":10,"quantity":2}, ...] } */
    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.createOrder(body));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> findByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
