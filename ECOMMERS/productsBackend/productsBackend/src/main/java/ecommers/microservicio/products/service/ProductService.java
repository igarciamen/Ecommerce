// src/main/java/ecommers/microservicio/products/service/ProductService.java
package ecommers.microservicio.products.service;

import ecommers.microservicio.products.model.Product;
import ecommers.microservicio.products.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository repo;
    private final Path uploadDir = Paths.get("uploads");

    // HTTP client y bases remotas
    private final RestTemplate http;
    private final String usersBaseUrl;
    private final String categoriesBaseUrl; // <-- nuevo

    public ProductService(ProductRepository repo,
                          RestTemplate http,
                          @Value("${users.base-url:http://localhost:8080/api}") String usersBaseUrl,
                          @Value("${categories.base-url:http://localhost:8081/api}") String categoriesBaseUrl // <-- nuevo
    ) throws IOException {
        this.repo = repo;
        this.http = http;
        this.usersBaseUrl = usersBaseUrl;
        this.categoriesBaseUrl = categoriesBaseUrl; // <-- nuevo
        Files.createDirectories(uploadDir);
    }

    public Page<Product> getProducts(Pageable pageable) {
        // 👇 Forzamos orden por ID DESC (últimos creados primero), ignorando cualquier sort entrante
        Pageable ordered = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<Product> page = repo.findAll(ordered);
        page.getContent().forEach(p -> { attachSellerSafe(p); attachCategorySafe(p); });
        return page;
    }

    public Page<Product> getByCategory(Long categoryId, Pageable pageable) {
        Page<Product> page = repo.findByCategoryId(categoryId, pageable);
        page.getContent().forEach(p -> { attachSellerSafe(p); attachCategorySafe(p); });
        return page;
    }

    public Page<Product> search(String q, Pageable pageable) {
        Page<Product> page = repo.search(q, pageable);
        page.getContent().forEach(p -> { attachSellerSafe(p); attachCategorySafe(p); });
        return page;
    }

    public Optional<Product> getById(Long id) {
        Optional<Product> opt = repo.findById(id);
        opt.ifPresent(p -> { attachSellerSafe(p); attachCategorySafe(p); });
        return opt;
    }

    // ----------------- helpers: USER -----------------
    private Map<String, Object> fetchUserOrThrow(Long sellerId) {
        String url = usersBaseUrl + "/user/" + sellerId;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = http.getForObject(url, Map.class);
            if (body == null || body.get("id") == null) {
                throw new EntityNotFoundException("Usuario vendedor no encontrado: " + sellerId);
            }
            Object username = body.get("username");
            if (username == null || "Desconocido".equalsIgnoreCase(username.toString())) {
                throw new EntityNotFoundException("Usuario vendedor no encontrado: " + sellerId);
            }
            boolean okRole = false;
            Object rolesObj = body.get("roles");
            if (rolesObj instanceof Collection<?> col) {
                for (Object r : col) {
                    if (r != null) {
                        String rs = r.toString();
                        if ("ROLE_SELLER".equals(rs) || "ROLE_ADMIN".equals(rs)) {
                            okRole = true; break;
                        }
                    }
                }
            }
            if (!okRole) {
                throw new IllegalArgumentException("El usuario " + sellerId + " no tiene permisos de vendedor");
            }
            return body;
        } catch (RestClientException ex) {
            throw new IllegalStateException("No se pudo consultar el usuario remoto: " + ex.getMessage(), ex);
        }
    }

    private void attachSellerSafe(Product p) {
        try {
            if (p.getSellerId() != null) {
                p.setSeller(fetchUserOrThrow(p.getSellerId()));
            }
        } catch (Exception ignore) {
            p.setSeller(null);
        }
    }

    // ----------------- helpers: CATEGORY -----------------
    /** Intenta GET /categories/{id}; si falla, hace fallback a GET /categories y filtra. */
    private Map<String, Object> fetchCategoryOrThrow(Long categoryId) {
        // 1) intento directo por ID
        String byId = categoriesBaseUrl + "/categories/" + categoryId;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> cat = http.getForObject(byId, Map.class);
            if (cat != null && cat.get("id") != null) {
                return cat;
            }
        } catch (RestClientException ignore) {
            // seguimos al fallback
        }

        // 2) fallback: listar y filtrar
        String listUrl = categoriesBaseUrl + "/categories";
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> all = http.getForObject(listUrl, List.class);
            if (all != null) {
                for (Map<String, Object> c : all) {
                    Object idObj = c.get("id");
                    if (idObj != null && Long.valueOf(idObj.toString()).equals(categoryId)) {
                        return c;
                    }
                }
            }
            throw new EntityNotFoundException("Categoría no encontrada: " + categoryId);
        } catch (RestClientException ex) {
            throw new IllegalStateException("No se pudo consultar categorías remotas: " + ex.getMessage(), ex);
        }
    }

    private void attachCategorySafe(Product p) {
        try {
            if (p.getCategoryId() != null) {
                p.setCategory(fetchCategoryOrThrow(p.getCategoryId()));
            }
        } catch (Exception ignore) {
            p.setCategory(null);
        }
    }

    // ----------------- CRUD -----------------

    @Transactional
    public Product createProduct(String name, String description,
                                 Double price, Integer stock,
                                 Long categoryId,
                                 Long sellerId,
                                 MultipartFile image) throws IOException {

        Map<String, Object> sellerObj = fetchUserOrThrow(sellerId);
        Map<String, Object> categoryObj = fetchCategoryOrThrow(categoryId); // <-- valida categoría

        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setStock(stock);
        p.setCategoryId(categoryId);
        p.setSellerId(sellerId);

        if (image != null && !image.isEmpty()) {
            String filename = UUID.randomUUID() + "-" + image.getOriginalFilename();
            Path target = uploadDir.resolve(filename);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            p.setImageUrl(filename);
        }

        Product saved = repo.save(p);
        saved.setSeller(sellerObj);
        saved.setCategory(categoryObj); // <-- adjunta para la respuesta
        return saved;
    }

    @Transactional
    public Product updateProduct(Long id,
                                 String name, String description,
                                 Double price, Integer stock,
                                 Long categoryId,
                                 Long sellerId,
                                 MultipartFile image) throws IOException {
        Product existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + id));

        Map<String, Object> sellerObj = fetchUserOrThrow(sellerId);
        Map<String, Object> categoryObj = fetchCategoryOrThrow(categoryId);

        existing.setName(name);
        existing.setDescription(description);
        existing.setPrice(price);
        existing.setStock(stock);
        existing.setCategoryId(categoryId);
        existing.setSellerId(sellerId);

        if (image != null && !image.isEmpty()) {
            if (existing.getImageUrl() != null) {
                Files.deleteIfExists(uploadDir.resolve(existing.getImageUrl()));
            }
            String filename = UUID.randomUUID() + "-" + image.getOriginalFilename();
            Path target = uploadDir.resolve(filename);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            existing.setImageUrl(filename);
        }

        Product saved = repo.save(existing);
        saved.setSeller(sellerObj);
        saved.setCategory(categoryObj);
        return saved;
    }

    @Transactional
    public void deleteProduct(Long id) throws IOException {
        Product existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + id));
        if (existing.getImageUrl() != null) {
            Files.deleteIfExists(uploadDir.resolve(existing.getImageUrl()));
        }
        repo.delete(existing);
    }

    @Transactional
    public Product adjustStock(Long id, int delta) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + id));

        int newStock = p.getStock() + delta;
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock insuficiente para realizar la operación");
        }
        p.setStock(newStock);
        Product saved = repo.save(p);
        attachSellerSafe(saved);
        attachCategorySafe(saved);
        return saved;
    }

    public Product save(Product p) {
        Product saved = repo.save(p);
        attachSellerSafe(saved);
        attachCategorySafe(saved);
        return saved;
    }
}
