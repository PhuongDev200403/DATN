package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.FlashSaleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/flash-sales")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlashSaleController {

    FlashSaleService flashSaleService;

    @GetMapping("/active-items")
    public ApiResponse<Object> getActiveFlashSaleItems() {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(flashSaleService.getActiveFlashSaleItems())
                .build();
    }

    @GetMapping("/price/{variantId}")
    public ApiResponse<Object> getFlashSalePrice(@PathVariable Long variantId) {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(flashSaleService.getFlashSalePrice(variantId))
                .build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<Object>> applyFlashSaleWhenCheckout(
            @RequestParam Long variantId,
            @RequestParam int quantity
    ) {
        try {
            flashSaleService.applyFlashSaleWhenCheckout(variantId, quantity);
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(StatusCode.SUCCESS.getCode())
                    .message(StatusCode.SUCCESS.getMessage())
                    .result("Flash sale applied successfully")
                    .build());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .code(StatusCode.BAD_REQUEST.getCode())
                            .message(e.getMessage())
                            .build());
        }
    }
}
