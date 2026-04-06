package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.FlashSaleItemResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.FlashSaleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flash-sales")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlashSaleController {

    FlashSaleService flashSaleService;

    @GetMapping("/active-items")
    public ApiResponse<List<FlashSaleItemResponse>> getActiveFlashSaleItems() {
        return ApiResponse.<List<FlashSaleItemResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(flashSaleService.getActiveFlashSaleItems())
                .build();
    }

    @GetMapping("/price/{variantId}")
    public ApiResponse<Double> getFlashSalePrice(@PathVariable Long variantId) {
        return ApiResponse.<Double>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(flashSaleService.getFlashSalePrice(variantId))
                .build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<MessageResponse>> applyFlashSaleWhenCheckout(
            @RequestParam Long variantId,
            @RequestParam int quantity
    ) {
        flashSaleService.applyFlashSaleWhenCheckout(variantId, quantity);
        return ResponseEntity.ok(ApiResponse.<MessageResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(MessageResponse.builder().message("Flash sale applied successfully").build())
                .build());
    }
}
