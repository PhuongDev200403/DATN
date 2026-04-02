package com.buixuantruong.shopapp.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyticsOverviewResponse {
    // Doanh thu
    Long totalRevenue;
    Long revenueThisMonth;
    Long revenueLastMonth;
    Double revenueGrowthRate; // % tăng trưởng so với tháng trước

    // Đơn hàng
    Long totalOrders;
    Long ordersThisMonth;
    Map<String, Long> ordersByStatus; // {PENDING: 10, DELIVERED: 80, ...}

    // Sản phẩm
    List<TopProductItem> topSellingProducts;

    // Đánh giá
    Double averageRating;
    Long totalReviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductItem {
        Long productId;
        String productName;
        Long totalSold;
        Long totalRevenue;
    }
}
