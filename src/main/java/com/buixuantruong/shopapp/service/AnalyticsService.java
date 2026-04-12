package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.response.AnalyticsAiInsightsResponse;
import com.buixuantruong.shopapp.dto.response.AnalyticsOverviewResponse;
import com.buixuantruong.shopapp.repository.AnalyticsReviewRepository;
import com.buixuantruong.shopapp.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyticsService {

    OrderRepository orderRepository;
    AnalyticsReviewRepository analyticsReviewRepository;
    AiTextService aiTextService;

    /**
     * Tổng hợp dữ liệu thống kê từ DB
     */
    public AnalyticsOverviewResponse getOverview() {
        // Doanh thu
        Long totalRevenue = toLong(orderRepository.getTotalRevenue());
        Long revenueThisMonth = toLong(orderRepository.getRevenueThisMonth());
        Long revenueLastMonth = toLong(orderRepository.getRevenueLastMonth());
        double growthRate = (revenueLastMonth != null && revenueLastMonth > 0)
                ? ((revenueThisMonth - revenueLastMonth) * 100.0 / revenueLastMonth)
                : 0.0;

        // Đơn hàng
        Long totalOrders = orderRepository.count();
        Long ordersThisMonth = orderRepository.countOrdersThisMonth();
        List<Object[]> statusData = orderRepository.countOrdersByStatus();
        Map<String, Long> ordersByStatus = new HashMap<>();
        for (Object[] row : statusData) {
            ordersByStatus.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }

        // Top 5 sản phẩm
        List<Object[]> topProducts = orderRepository.getTopSellingProducts(5);
        List<AnalyticsOverviewResponse.TopProductItem> topList = topProducts.stream()
                .map(row -> AnalyticsOverviewResponse.TopProductItem.builder()
                        .productId(((Number) row[0]).longValue())
                        .productName(String.valueOf(row[1]))
                        .totalSold(((Number) row[2]).longValue())
                        .totalRevenue(((Number) row[3]).longValue())
                        .build())
                .toList();

        // Review
        Double avgRating = analyticsReviewRepository.getAverageRating();
        Long totalReviews = analyticsReviewRepository.countAllReviews();

        return AnalyticsOverviewResponse.builder()
                .totalRevenue(totalRevenue)
                .revenueThisMonth(revenueThisMonth)
                .revenueLastMonth(revenueLastMonth)
                .revenueGrowthRate(growthRate)
                .totalOrders(totalOrders)
                .ordersThisMonth(ordersThisMonth)
                .ordersByStatus(ordersByStatus)
                .topSellingProducts(topList)
                .averageRating(avgRating)
                .totalReviews(totalReviews)
                .build();
    }

    /**
     * Tổng hợp thống kê + phân tích AI bằng Gemini
     */
    public AnalyticsAiInsightsResponse getAiInsights() {
        AnalyticsOverviewResponse overview = getOverview();
        String aiAnalysis = aiTextService.analyzeShopData(overview);
        return AnalyticsAiInsightsResponse.builder()
                .overview(overview)
                .aiInsights(aiAnalysis)
                .generatedAt(java.time.LocalDateTime.now().toString())
                .build();
    }

    private Long toLong(BigDecimal value) {
        return value == null ? 0L : value.longValue();
    }
}
