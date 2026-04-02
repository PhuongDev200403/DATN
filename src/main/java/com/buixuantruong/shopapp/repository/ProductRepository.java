package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
    boolean existsByWarrantyCode(String warrantyCode);
    Page<Product> findAll(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id IN :productIds")
    List<Product> findProductByIds(@Param("productIds") List<Long> productIds);

    @Query("""
            SELECT p FROM Product p
            WHERE p.id IN :productIds
              AND EXISTS (
                  SELECT 1 FROM Variant v
                  WHERE v.product = p
                    AND v.isActive = true
              )
            """)
    List<Product> findActiveProductsByIds(@Param("productIds") List<Long> productIds);

    @Query("""
            SELECT p FROM Product p
            WHERE p.id <> :productId
              AND p.category.id = :categoryId
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND EXISTS (
                  SELECT 1 FROM Variant v
                  WHERE v.product = p
                    AND v.isActive = true
              )
            """)
    List<Product> findSimilarCandidates(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Float minPrice,
            @Param("maxPrice") Float maxPrice
    );
    
    // Find products by category ID
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProducts(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("minPrice") Float minPrice,
            @Param("maxPrice") Float maxPrice,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.thumbnail LIKE %:keyword%")
    List<Product> findByThumbnailContaining(@Param("keyword") String keyword);

    void deleteAll(Iterable<? extends Product> products);
}
