package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.response.FlashSaleItemResponse;

import java.util.List;

public interface FlashSaleService {

    List<FlashSaleItemResponse> getActiveFlashSaleItems();

    double getFlashSalePrice(Long variantId);

    void applyFlashSaleWhenCheckout(Long variantId, int quantity);
}
