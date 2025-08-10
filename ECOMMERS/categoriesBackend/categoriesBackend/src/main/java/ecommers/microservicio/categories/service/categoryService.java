package ecommers.microservicio.categories.service;

import ecommers.microservicio.categories.model.Category;
import ecommers.microservicio.categories.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class categoryService {

    private final CategoryRepository repo;

    public categoryService(CategoryRepository repo) {
        this.repo = repo;
    }

    public List<Category> findAll() {
        return repo.findAll();
    }


    public Category save(Category category) {
        return repo.save(category);
    }
}
