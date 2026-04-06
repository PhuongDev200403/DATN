package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariantRepository extends JpaRepository<Variant, Long> {
    boolean existsBySku(String sku);

    List<Variant> findByProductId(Long productId);
}
