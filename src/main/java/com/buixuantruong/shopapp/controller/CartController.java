package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.CartResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {

    CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        return ApiResponse.<CartResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(cartService.getCart(getCurrentUserId()))
                .build();
    }

    @PostMapping("/add")
    public ApiResponse<CartResponse> addToCart(@RequestParam Long variantId, @RequestParam Integer quantity) {
        return ApiResponse.<CartResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(cartService.addToCart(getCurrentUserId(), variantId, quantity))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<CartResponse> updateQuantity(@RequestParam Long variantId, @RequestParam Integer quantity) {
        return ApiResponse.<CartResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(cartService.updateQuantity(getCurrentUserId(), variantId, quantity))
                .build();
    }

    @DeleteMapping("/remove/{variantId}")
    public ApiResponse<MessageResponse> removeFromCart(@PathVariable Long variantId) {
        cartService.removeFromCart(getCurrentUserId(), variantId);
        return ApiResponse.<MessageResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(MessageResponse.builder().message("Removed item from cart successfully").build())
                .build();
    }

    @PostMapping("/sync")
    public ApiResponse<MessageResponse> syncCart(@RequestBody List<Map<String, Object>> items) {
        cartService.syncCart(getCurrentUserId(), items);
        return ApiResponse.<MessageResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(MessageResponse.builder().message("Synchronized cart successfully").build())
                .build();
    }

    private Long getCurrentUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }
}
