package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.FlashSaleItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Long> {

    @Query("""
            SELECT fsi
            FROM FlashSaleItem fsi
            JOIN FETCH fsi.flashSale fs
            JOIN FETCH fsi.variant v
            WHERE fsi.active = true
              AND fs.active = true
              AND fs.startTime <= :now
              AND fs.endTime >= :now
              AND COALESCE(fsi.quantitySold, 0) < fsi.quantityLimit
            ORDER BY fs.priority DESC, fs.startTime ASC
            """)
    List<FlashSaleItem> findActiveFlashSaleItems(@Param("now") LocalDateTime now);

    @Query("""
            SELECT fsi
            FROM FlashSaleItem fsi
            JOIN FETCH fsi.flashSale fs
            JOIN FETCH fsi.variant v
            WHERE v.id = :variantId
              AND fsi.active = true
              AND fs.active = true
              AND fs.startTime <= :now
              AND fs.endTime >= :now
              AND COALESCE(fsi.quantitySold, 0) < fsi.quantityLimit
            ORDER BY fs.priority DESC, fs.startTime ASC
            """)
    List<FlashSaleItem> findValidFlashSaleItemsByVariantId(
            @Param("variantId") Long variantId,
            @Param("now") LocalDateTime now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT fsi
            FROM FlashSaleItem fsi
            JOIN FETCH fsi.flashSale fs
            JOIN FETCH fsi.variant v
            WHERE v.id = :variantId
              AND fsi.active = true
              AND fs.active = true
              AND fs.startTime <= :now
              AND fs.endTime >= :now
            ORDER BY fs.priority DESC, fs.startTime ASC
            """)
    List<FlashSaleItem> lockValidFlashSaleItemsByVariantId(
            @Param("variantId") Long variantId,
            @Param("now") LocalDateTime now
    );
}
