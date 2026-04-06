package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);

    Page<Product> findAll(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p WHERE p.id IN :productIds")
    List<Product> findProductByIds(@Param("productIds") List<Long> productIds);

    @Query("""
            SELECT DISTINCT p FROM Product p
            WHERE p.id IN :productIds
              AND EXISTS (
                  SELECT 1 FROM Variant v
                  WHERE v.product = p
                    AND v.isActive = true
              )
            """)
    List<Product> findActiveProductsByIds(@Param("productIds") List<Long> productIds);

    @Query("""
            SELECT DISTINCT p FROM Product p
            WHERE p.id <> :productId
              AND p.category.id = :categoryId
              AND EXISTS (
                  SELECT 1 FROM Variant v
                  WHERE v.product = p
                    AND v.isActive = true
                    AND (:minPrice IS NULL OR v.price >= :minPrice)
                    AND (:maxPrice IS NULL OR v.price <= :maxPrice)
              )
            """)
    List<Product> findSimilarCandidates(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Float minPrice,
            @Param("maxPrice") Float maxPrice
    );

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Product p
            WHERE p.createAt >= :fromDate
              AND EXISTS (
                  SELECT 1 FROM Variant v
                  WHERE v.product = p
                    AND v.isActive = true
              )
            """)
    Page<Product> findNewProducts(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN p.variants v
            WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice IS NULL OR EXISTS (
                    SELECT 1 FROM Variant vMin
                    WHERE vMin.product = p
                      AND vMin.price >= :minPrice
              ))
              AND (:maxPrice IS NULL OR EXISTS (
                    SELECT 1 FROM Variant vMax
                    WHERE vMax.product = p
                      AND vMax.price <= :maxPrice
              ))
            """)
    Page<Product> searchProducts(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("minPrice") Float minPrice,
            @Param("maxPrice") Float maxPrice,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Product p
            JOIN p.variants v
            WHERE v.imageUrl LIKE %:keyword%
            """)
    List<Product> findByVariantImageContaining(@Param("keyword") String keyword);

    void deleteAll(Iterable<? extends Product> products);
}
