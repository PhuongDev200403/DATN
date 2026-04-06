package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.response.CartItemResponse;
import com.buixuantruong.shopapp.dto.response.CartResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.mapper.VariantMapper;
import com.buixuantruong.shopapp.model.Cart;
import com.buixuantruong.shopapp.model.CartItem;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.model.Variant;
import com.buixuantruong.shopapp.repository.CartItemRepository;
import com.buixuantruong.shopapp.repository.CartRepository;
import com.buixuantruong.shopapp.repository.UserRepository;
import com.buixuantruong.shopapp.repository.VariantRepository;
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
    VariantRepository variantRepository;
    UserRepository userRepository;
    VariantMapper variantMapper;

    @Override
    public CartResponse getCart(Long userId) {
        return toCartResponse(getOrCreateCart(userId));
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long userId, Long variantId, Integer quantity) {
        validateQuantity(quantity);

        Cart cart = getOrCreateCart(userId);
        Variant variant = findVariant(variantId);
        int currentQuantity = findCartItem(cart, variantId)
                .map(CartItem::getQuantity)
                .orElse(0);

        ensureEnoughStock(variant, currentQuantity + quantity);

        Optional<CartItem> existingItem = findCartItem(cart, variantId);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(newItem);
            cart.getCartItems().add(newItem);
        }

        return toCartResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(Long userId, Long variantId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findCartItem(cart, variantId)
                .orElseThrow(() -> new AppException(StatusCode.CART_ITEM_NOT_FOUND));

        if (quantity == null || quantity <= 0) {
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
            return toCartResponse(cartRepository.save(cart));
        }

        ensureEnoughStock(item.getVariant(), quantity);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return toCartResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long variantId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findCartItem(cart, variantId)
                .orElseThrow(() -> new AppException(StatusCode.CART_ITEM_NOT_FOUND));
        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);
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
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();

        for (Map<String, Object> itemData : items) {
            Long variantId = Long.valueOf(String.valueOf(itemData.get("variant_id")));
            Integer quantity = Integer.valueOf(String.valueOf(itemData.get("quantity")));
            if (quantity <= 0) {
                continue;
            }

            Variant variant = findVariant(variantId);
            ensureEnoughStock(variant, quantity);

            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(quantity)
                    .build();
            cart.getCartItems().add(cartItem);
        }

        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    private Cart createNewCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));
        Cart cart = Cart.builder()
                .user(user)
                .cartItems(new ArrayList<>())
                .build();
        return cartRepository.save(cart);
    }

    private Variant findVariant(Long variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(StatusCode.VARIANT_NOT_FOUND));
    }

    private Optional<CartItem> findCartItem(Cart cart, Long variantId) {
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems == null) {
            return Optional.empty();
        }
        return cartItems.stream()
                .filter(item -> item.getVariant() != null && item.getVariant().getId().equals(variantId))
                .findFirst();
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new AppException(StatusCode.INVALID_QUANTITY);
        }
    }

    private void ensureEnoughStock(Variant variant, int requiredQuantity) {
        long availableStock = variant.getStock() == null ? 0L : variant.getStock();
        if (availableStock < requiredQuantity) {
            throw new AppException(StatusCode.INVALID_QUANTITY);
        }
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getCartItems() == null
                ? List.of()
                : cart.getCartItems().stream()
                .map(item -> CartItemResponse.builder()
                        .quantity(item.getQuantity())
                        .variantResponse(variantMapper.toResponse(item.getVariant()))
                        .build())
                .toList();

        return new CartResponse(cart.getId(), cart.getUser().getId(), itemResponses);
    }
}
