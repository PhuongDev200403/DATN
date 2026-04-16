package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.Order;
import com.buixuantruong.shopapp.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long id);

    @Query("""
            SELECT od2.variant.product.id, COUNT(od2.id)
            FROM OrderDetail od1, OrderDetail od2
            WHERE od1.order.id = od2.order.id
              AND od1.order.status = com.buixuantruong.shopapp.model.OrderStatus.DELIVERY
              AND od1.variant.product.id = :productId
              AND od2.variant.product.id <> :productId
              AND EXISTS (
                  SELECT 1 FROM Variant v
                  WHERE v.product = od2.variant.product
                    AND v.isActive = true
              )
            GROUP BY od2.variant.product.id
            ORDER BY COUNT(od2.id) DESC
            """)
    List<Object[]> findFrequentlyBoughtTogetherProductIds(@Param("productId") Long productId);

    @Query("""
            SELECT od.variant.product.id, SUM(od.numberOfProducts)
            FROM OrderDetail od
            WHERE EXISTS (
                SELECT 1 FROM Variant v
                WHERE v.product = od.variant.product
                  AND v.isActive = true
            )
              AND od.order.status = com.buixuantruong.shopapp.model.OrderStatus.DELIVERY
            GROUP BY od.variant.product.id
            ORDER BY SUM(od.numberOfProducts) DESC
            """)
    List<Object[]> findTopSellingProductIds();
}
