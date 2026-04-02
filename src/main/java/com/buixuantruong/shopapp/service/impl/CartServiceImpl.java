package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.model.Cart;
import com.buixuantruong.shopapp.model.CartItem;
import com.buixuantruong.shopapp.model.Product;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.repository.CartItemRepository;
import com.buixuantruong.shopapp.repository.CartRepository;
import com.buixuantruong.shopapp.repository.ProductRepository;
import com.buixuantruong.shopapp.repository.UserRepository;
import com.buixuantruong.shopapp.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductRepository productRepository;
    UserRepository userRepository;

    @Override
    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    @Override
    @Transactional
    public Cart addToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = getCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(newItem);
            cart.getCartItems().add(newItem);
        }
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart updateQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = getCart(userId);
        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (quantity <= 0) {
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        Cart cart = getCart(userId);
        cart.getCartItems().removeIf(item -> {
            if (item.getProduct().getId().equals(productId)) {
                cartItemRepository.delete(item);
                return true;
            }
            return false;
        });
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartItemRepository.deleteAll(cart.getCartItems());
            cart.getCartItems().clear();
            cartRepository.save(cart);
        });
    }

    @Override
    @Transactional
    public void syncCart(Long userId, List<Map<String, Object>> items) {
        Cart cart = getCart(userId);
        for (Map<String, Object> itemData : items) {
            Long productId = Long.valueOf(String.valueOf(itemData.get("product_id")));
            Integer quantity = Integer.valueOf(String.valueOf(itemData.get("quantity")));
            addToCart(userId, productId, quantity);
        }
    }

    private Cart createNewCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = Cart.builder()
                .user(user)
                .cartItems(new ArrayList<>())
                .build();
        return cartRepository.save(cart);
    }
}
