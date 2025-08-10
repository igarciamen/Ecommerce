package ecommers.microservicio.categories.controller;

import ecommers.microservicio.categories.model.Category;
import ecommers.microservicio.categories.service.categoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin("*")
public class categoryController {

    private final categoryService service;

    public categoryController(categoryService service) {
        this.service = service;
    }

    /** Listar todas las categorías */
    @GetMapping
    public ResponseEntity<List<Category>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    /** Crear nueva categoría */
    @PostMapping
    public ResponseEntity<Category> create(
            @Valid @RequestBody Category category
    ) throws Exception {
        Category saved = service.save(category);
        return ResponseEntity
                .created(new URI("/api/categories/" + saved.getId()))
                .body(saved);
    }



}
