package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.response.CartResponse;

import java.util.List;
import java.util.Map;

public interface CartService {
    CartResponse getCart(Long userId);

    CartResponse addToCart(Long userId, Long variantId, Integer quantity);

    CartResponse updateQuantity(Long userId, Long variantId, Integer quantity);

    void removeFromCart(Long userId, Long variantId);

    void clearCart(Long userId);

    void syncCart(Long userId, List<Map<String, Object>> cartItems);
}
