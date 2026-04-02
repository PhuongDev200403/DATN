package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.response.AnalyticsOverviewResponse;
import com.buixuantruong.shopapp.model.Product;

import java.util.List;

public interface AiTextService {

    String analyzeShopData(AnalyticsOverviewResponse data);

    String getRecommendations(String query, List<Product> products);

    String generateProductDescription(String productName, String categoryName, String specs);

    String getSupportResponse(String query, String context);
}
