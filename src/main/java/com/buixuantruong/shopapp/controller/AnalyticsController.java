package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.AnalyticsOverviewResponse;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.AnalyticsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyticsController {

    AnalyticsService analyticsService;

    /**
     * API 1: Thống kê dữ liệu thuần (không AI)
     * GET /api/v1/admin/analytics/overview
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> getOverview() {
        AnalyticsOverviewResponse overview = analyticsService.getOverview();
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(overview)
                .build();
    }

    /**
     * API 2: Thống kê + Phân tích AI thông minh bằng Gemini
     * GET /api/v1/admin/analytics/ai-insights
     */
    @GetMapping("/ai-insights")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> getAiInsights() {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(analyticsService.getAiInsights())
                .build();
    }
}
