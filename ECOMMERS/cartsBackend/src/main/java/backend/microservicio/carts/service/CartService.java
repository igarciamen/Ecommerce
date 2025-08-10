// src/main/java/backend/microservicio/carts/service/CartService.java
package backend.microservicio.carts.service;

import backend.microservicio.carts.model.Cart;
import backend.microservicio.carts.model.CartItem;
import backend.microservicio.carts.repository.CartRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepo;
    private final RestTemplate http;
    private final String usersBaseUrl;
    private final String productsBaseUrl;

    public CartService(CartRepository cartRepo,
                       RestTemplate http,
                       @Value("${users.base-url:http://localhost:8080/api}") String usersBaseUrl,
                       @Value("${products.base-url:http://localhost:8082/api}") String productsBaseUrl) {
        this.cartRepo = cartRepo;
        this.http = http;
        this.usersBaseUrl = usersBaseUrl;
        this.productsBaseUrl = productsBaseUrl;
    }

    // ----------------- helpers de carga -----------------

    private Cart loadCart(Long userId) {
        return cartRepo.findByUserId(userId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(userId);
                    return cartRepo.save(c);
                });
    }

    // ----------------- helpers REST -----------------

    private Map<String, Object> fetchUserOrThrow(Long userId) {
        String url = usersBaseUrl + "/user/" + userId;
        try {
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
        } catch (RestClientException ex) {
            throw new IllegalStateException("No se pudo consultar usuario remoto: " + ex.getMessage(), ex);
        }
    }

    private Map<String, Object> fetchProductOrThrow(Long productId) {
        String url = productsBaseUrl + "/products/" + productId;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = http.getForObject(url, Map.class);
            if (body == null || body.get("id") == null) {
                throw new EntityNotFoundException("Producto no encontrado: " + productId);
            }
            return body;
        } catch (RestClientException ex) {
            throw new IllegalStateException("No se pudo consultar producto remoto: " + ex.getMessage(), ex);
        }
    }

    private void attachUserAndProducts(Cart cart) {
        // Adjunta user completo
        if (cart.getUserId() != null) {
            try { cart.setUser(fetchUserOrThrow(cart.getUserId())); }
            catch (Exception ignore) { cart.setUser(null); }
        }
        // Adjunta product completo por cada ítem
        if (cart.getItems() != null) {
            for (CartItem it : cart.getItems()) {
                if (it.getProductId() != null) {
                    try { it.setProduct(fetchProductOrThrow(it.getProductId())); }
                    catch (Exception ignore) { it.setProduct(null); }
                }
            }
        }
    }

    // ----------------- API usada por el Controller -----------------

    public Cart getOrCreateCart(Long userId) {
        Cart cart = loadCart(userId);
        attachUserAndProducts(cart);
        return cart;
    }

    public Cart addItem(Long userId, Long productId, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty debe ser > 0");
        // Validar que el user y product existan
        fetchUserOrThrow(userId);
        fetchProductOrThrow(productId);

        Cart cart = loadCart(userId);
        cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + qty),
                        () -> {
                            CartItem item = new CartItem();
                            item.setCart(cart);
                            item.setProductId(productId);
                            item.setQuantity(qty);
                            cart.getItems().add(item);
                        }
                );
        Cart saved = cartRepo.save(cart);
        attachUserAndProducts(saved);
        return saved;
    }

    public Cart clearCart(Long userId) {
        Cart cart = loadCart(userId);
        cart.getItems().clear();
        Cart saved = cartRepo.save(cart);
        attachUserAndProducts(saved);
        return saved;
    }

    public int countUnits(Long userId) {
        return loadCart(userId).getItems()
                .stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public int countDistinctItems(Long userId) {
        return loadCart(userId).getItems().size();
    }

    public Cart updateItemQuantity(Long userId, Long cartItemId, int quantity) {
        Cart cart = loadCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Ítem no encontrado: " + cartItemId));

        if (quantity > 0) {
            item.setQuantity(quantity);
        } else {
            cart.getItems().remove(item);
        }
        Cart saved = cartRepo.save(cart);
        attachUserAndProducts(saved);
        return saved;
    }
}
