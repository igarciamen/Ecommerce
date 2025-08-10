// src/main/java/ecommers/microservicio/products/controllers/ProductController.java
package ecommers.microservicio.products.controllers;

import ecommers.microservicio.products.model.Product;
import ecommers.microservicio.products.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
public class ProductController {

    private final ProductService productSvc;

    public ProductController(ProductService productSvc) {
        this.productSvc = productSvc;
    }

    /**
     * GET /api/products
     *   pageable se rellena con page, size, sort... automáticamente.
     *   Por defecto size=10.
     */
    @GetMapping
    public Page<Product> listarProductos(
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return productSvc.getProducts(pageable);
    }

    /**
     * GET /api/products/by-category
     *   Listado paginado filtrado por categoría (query param).
     */
    @GetMapping("/by-category")
    public Page<Product> listarPorCategoria(
            @RequestParam Long categoryId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return productSvc.getByCategory(categoryId, pageable);
    }

    /**
     * GET /api/products/search?q=...
     *   Búsqueda paginada por nombre o descripción.
     */
    @GetMapping("/search")
    public Page<Product> buscarProductos(
            @RequestParam("q") String q,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        return productSvc.search(q, pageable);
    }

    /**
     * GET /api/products/{id}
     *   Obtener un producto por su ID.
     */
    @GetMapping("/{id}")
    public Product obtenerProducto(@PathVariable Long id) {
        return productSvc.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + id));
    }

    /**
     * POST /api/products
     *   Crear producto (multipart/form-data para imagen opcional).
     *   Recibe sellerId por query param (lo extrae el frontend tras login).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product crearProducto(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Double price,
            @RequestParam Integer stock,
            @RequestParam Long categoryId,
            @RequestParam Long sellerId,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        return productSvc.createProduct(
                name, description, price, stock, categoryId, sellerId, image
        );
    }

    /**
     * PUT /api/products/{id}
     *   Actualizar producto (multipart/form-data para imagen opcional).
     *   Recibe sellerId por query param para validar/updatear el propietario.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product actualizarProducto(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Double price,
            @RequestParam Integer stock,
            @RequestParam Long categoryId,
            @RequestParam Long sellerId,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            return productSvc.updateProduct(
                    id, name, description, price, stock, categoryId, sellerId, image
            );
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al procesar la imagen", ex
            );
        }
    }

    /**
     * DELETE /api/products/{id}
     *   Eliminar producto y su imagen.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        try {
            productSvc.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al eliminar la imagen", ex
            );
        }
    }




    /** ✅ PATCH /api/products/{id}/stock?delta=-N  (ajustar stock) */
    @PatchMapping("/{id}/stock")
    public Product ajustarStock(
            @PathVariable Long id,
            @RequestParam int delta
    ) {
        try {
            return productSvc.adjustStock(id, delta);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }






}
