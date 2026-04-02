package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    Page<Order> findAll(Pageable pageable);

    // Tổng doanh thu
    @Query("SELECT COALESCE(SUM(o.totalMoney), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    Long getTotalRevenue();

    // Doanh thu tháng hiện tại
    @Query(value = "SELECT COALESCE(SUM(total_money), 0) FROM orders WHERE status = 'DELIVERED' AND MONTH(order_date) = MONTH(CURRENT_DATE()) AND YEAR(order_date) = YEAR(CURRENT_DATE())", nativeQuery = true)
    Long getRevenueThisMonth();

    // Doanh thu tháng trước
    @Query(value = "SELECT COALESCE(SUM(total_money), 0) FROM orders WHERE status = 'DELIVERED' AND MONTH(order_date) = MONTH(DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH)) AND YEAR(order_date) = YEAR(DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH))", nativeQuery = true)
    Long getRevenueLastMonth();

    // Đếm đơn hàng tháng này
    @Query(value = "SELECT COUNT(*) FROM orders WHERE MONTH(order_date) = MONTH(CURRENT_DATE()) AND YEAR(order_date) = YEAR(CURRENT_DATE())", nativeQuery = true)
    Long countOrdersThisMonth();

    // Thống kê đơn hàng theo trạng thái
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    // Top N sản phẩm bán chạy nhất
    @Query(value = """
        SELECT od.product_id, p.name, SUM(od.number_of_products) as total_sold,
               SUM(od.number_of_products * od.price) as total_revenue
        FROM order_details od
        JOIN products p ON od.product_id = p.id
        JOIN orders o ON od.order_id = o.id
        WHERE o.status = 'DELIVERED'
        GROUP BY od.product_id, p.name
        ORDER BY total_sold DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getTopSellingProducts(@Param("limit") int limit);
}
