package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.model.Cart;
import java.util.List;
import java.util.Map;

public interface CartService {
    Cart getCart(Long userId);
    Cart addToCart(Long userId, Long productId, Integer quantity);
    Cart updateQuantity(Long userId, Long productId, Integer quantity);
    void removeFromCart(Long userId, Long productId);
    void clearCart(Long userId);
    void syncCart(Long userId, List<Map<String, Object>> cartItems);
}
