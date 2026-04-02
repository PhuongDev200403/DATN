package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.model.Cart;
import com.buixuantruong.shopapp.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import com.buixuantruong.shopapp.model.User;
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
    public ResponseEntity<Cart> getCart() {
        return ResponseEntity.ok(cartService.getCart(getCurrentUserId()));
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestParam Long productId, @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.addToCart(getCurrentUserId(), productId, quantity));
    }

    @PutMapping("/update")
    public ResponseEntity<Cart> updateQuantity(@RequestParam Long productId, @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(getCurrentUserId(), productId, quantity));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long productId) {
        cartService.removeFromCart(getCurrentUserId(), productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncCart(@RequestBody List<Map<String, Object>> items) {
        cartService.syncCart(getCurrentUserId(), items);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }
}
