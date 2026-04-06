package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.AnalyticsAiInsightsResponse;
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

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AnalyticsOverviewResponse> getOverview() {
        AnalyticsOverviewResponse overview = analyticsService.getOverview();
        return ApiResponse.<AnalyticsOverviewResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(overview)
                .build();
    }

    @GetMapping("/ai-insights")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AnalyticsAiInsightsResponse> getAiInsights() {
        return ApiResponse.<AnalyticsAiInsightsResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(analyticsService.getAiInsights())
                .build();
    }
}
