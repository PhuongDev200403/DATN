package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.ProductUnit;
import com.buixuantruong.shopapp.model.ProductUnitStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {
    Optional<ProductUnit> findBySerialNumber(String serialNumber);
    boolean existsBySerialNumber(String serialNumber);
    long countByVariantIdAndStatusAndOrderIsNull(Long variantId, ProductUnitStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pu from ProductUnit pu where pu.serialNumber = :serialNumber")
    Optional<ProductUnit> findBySerialNumberForUpdate(@Param("serialNumber") String serialNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select pu from ProductUnit pu
            where pu.variant.id = :variantId
                and pu.status = :status
                and pu.order is null
            order by pu.id asc
            """)
    List<ProductUnit> findAvailableUnitsForUpdate(
            @Param("variantId") Long variantId,
            @Param("status") ProductUnitStatus status,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select pu from ProductUnit pu
            where pu.order.id = :orderId
                and pu.status = :status
            order by pu.id asc
            """)
    List<ProductUnit> findByOrderIdAndStatusForUpdate(
            @Param("orderId") Long orderId,
            @Param("status") ProductUnitStatus status
    );
}
