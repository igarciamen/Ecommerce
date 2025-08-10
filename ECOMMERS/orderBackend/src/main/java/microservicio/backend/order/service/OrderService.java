// src/main/java/microservicio/backend/order/service/OrderService.java
package microservicio.backend.order.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import microservicio.backend.order.model.Order;
import microservicio.backend.order.model.OrderItem;
import microservicio.backend.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepo;
    private final RestTemplate http;
    private final String productsBaseUrl;
    private final String usersBaseUrl;

    public OrderService(OrderRepository orderRepo,
                        RestTemplate http,
                        @Value("${products.base-url:http://localhost:8082/api/products}") String productsBaseUrl,
                        @Value("${users.base-url:http://localhost:8080/api}") String usersBaseUrl) {
        this.orderRepo = orderRepo;
        this.http = http;
        this.productsBaseUrl = productsBaseUrl;
        this.usersBaseUrl = usersBaseUrl;
    }

    /** Crea una orden a partir de un JSON genérico: { userId, items:[{productId, quantity}] } */
    public Order createOrder(Map<String, Object> payload) {
        Long userId = parseLong(payload.get("userId"), "userId");
        List<Map<String, Object>> items = castItems(payload.get("items"));

        // valida usuario remoto
        fetchUserOrThrow(userId);

        // construye la orden
        Order order = new Order();
        order.setUserId(userId);
        order.setCreatedAt(LocalDateTime.now());

        Set<OrderItem> orderItems = items.stream()
                .map(this::toOrderItem)
                .peek(oi -> oi.setOrder(order))
                .collect(Collectors.toSet());

        order.setItems(orderItems);

        Order saved = orderRepo.save(order);

        // Ajuste de stock con compensación
        List<OrderItem> adjusted = new ArrayList<>();
        try {
            for (OrderItem it : saved.getItems()) {
                adjustStock(it.getProductId(), -it.getQuantity());
                adjusted.add(it);
            }
        } catch (RuntimeException ex) {
            for (OrderItem it : adjusted) {
                try { adjustStock(it.getProductId(), +it.getQuantity()); } catch (RuntimeException ignored) {}
            }
            throw ex;
        }

        // enriquecer para la respuesta
        attachUserAndProductsAndTotal(saved);
        return saved;
    }

    public List<Order> findByUserId(Long userId) {
        List<Order> list = orderRepo.findByUserId(userId);
        list.forEach(this::attachUserAndProductsAndTotal);
        return list;
    }

    public Optional<Order> findById(Long id) {
        Optional<Order> opt = orderRepo.findById(id);
        opt.ifPresent(this::attachUserAndProductsAndTotal);
        return opt;
    }

    public void deleteById(Long id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));

        // devolver stock
        for (OrderItem it : order.getItems()) {
            adjustStock(it.getProductId(), +it.getQuantity());
        }

        orderRepo.delete(order);
    }

    // -------------------- helpers --------------------

    private OrderItem toOrderItem(Map<String, Object> m) {
        Long productId = parseLong(m.get("productId"), "productId");
        Integer qty = parseInt(m.get("quantity"), "quantity");
        if (qty == null || qty <= 0) {
            throw new IllegalArgumentException("quantity debe ser > 0");
        }

        // Obtén nombre/precio del microservicio Products
        Map<String, Object> prod = fetchProductOrThrow(productId);
        String name = String.valueOf(prod.getOrDefault("name", "Producto " + productId));
        Double price = parseDouble(prod.get("price"), "price");

        OrderItem oi = new OrderItem();
        oi.setProductId(productId);
        oi.setProductName(name);
        oi.setUnitPrice(price);
        oi.setQuantity(qty);
        return oi;
    }

    private void attachUserAndProductsAndTotal(Order order) {
        // usuario
        try { order.setUser(fetchUserOrThrow(order.getUserId())); }
        catch (Exception ignore) { order.setUser(null); }

        // productos + total
        double total = 0d;
        if (order.getItems() != null) {
            for (OrderItem it : order.getItems()) {
                try { it.setProduct(fetchProductOrThrow(it.getProductId())); }
                catch (Exception ignore) { it.setProduct(null); }
                total += it.getQuantity() * it.getUnitPrice();
            }
        }
        order.setTotalAmount(total);
    }

    private Map<String, Object> fetchUserOrThrow(Long userId) {
        String url = usersBaseUrl + "/user/" + userId;
        @SuppressWarnings("unchecked")
        Map<String, Object> body = http.getForObject(url, Map.class);
        if (body == null || body.get("id") == null) {
            throw new EntityNotFoundException("Usuario no encontrado: " + userId);
        }
        Object username = body.get("username");
        if (username == null || "Desconocido".equalsIgnoreCase(username.toString())) {
            throw new EntityNotFoundException("Usuario no encontrado: " + userId);
        }
        return body;
    }

    private Map<String, Object> fetchProductOrThrow(Long productId) {
        String url = productsBaseUrl + "/" + productId;
        @SuppressWarnings("unchecked")
        Map<String, Object> body = http.getForObject(url, Map.class);
        if (body == null || body.get("id") == null) {
            throw new EntityNotFoundException("Producto no encontrado: " + productId);
        }
        return body;
    }

    /** PATCH /api/products/{id}/stock?delta=±N */
    private void adjustStock(Long productId, int delta) {
        String url = productsBaseUrl + "/" + productId + "/stock?delta=" + delta;
        try {
            ResponseEntity<Void> resp =
                    http.exchange(url, HttpMethod.PATCH, HttpEntity.EMPTY, Void.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Ajuste de stock no exitoso: " + resp.getStatusCode());
            }
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            throw new IllegalStateException(
                    "Error ajustando stock (prod " + productId + ", delta " + delta + "): " +
                            e.getRawStatusCode() + " " + body, e);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error ajustando stock (prod " + productId + ", delta " + delta + "): " + e.getMessage(), e);
        }
    }

    // -------------------- parsers --------------------
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castItems(Object o) {
        if (o instanceof List<?> l) {
            return (List<Map<String, Object>>) (List<?>) l;
        }
        throw new IllegalArgumentException("items debe ser un array de objetos");
    }

    private Long parseLong(Object o, String field) {
        if (o == null) throw new IllegalArgumentException(field + " es requerido");
        if (o instanceof Number n) return n.longValue();
        return Long.valueOf(o.toString());
    }

    private Integer parseInt(Object o, String field) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        return Integer.valueOf(o.toString());
    }

    private Double parseDouble(Object o, String field) {
        if (o == null) throw new IllegalArgumentException(field + " no disponible");
        if (o instanceof Number n) return n.doubleValue();
        return Double.valueOf(o.toString());
    }
}
