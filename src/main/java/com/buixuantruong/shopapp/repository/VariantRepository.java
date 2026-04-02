package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantRepository extends JpaRepository<Variant, Long> {
}
