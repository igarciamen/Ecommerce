package ecommers.microservicio.products.repository;


import ecommers.microservicio.products.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Paginación y orden por ID ascendente */
    @Override
    Page<Product> findAll(Pageable pageable);

    /** Filtrar por categoría */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /** Búsqueda por nombre o descripción */
    @Query("SELECT p FROM Product p " +
           "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Product> search(@Param("q") String query, Pageable pageable);
}
