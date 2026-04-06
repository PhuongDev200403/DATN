package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.VariantDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.VariantResponse;
import com.buixuantruong.shopapp.service.VariantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.buixuantruong.shopapp.exception.StatusCode.SUCCESS;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VariantController {
    VariantService variantService;

    @PostMapping("")
    public ApiResponse<VariantResponse> createVariant(@RequestBody VariantDTO variantDTO) {
        return ApiResponse.<VariantResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.createVariant(variantDTO))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<VariantResponse> getVariantById(@PathVariable Long id) {
        return ApiResponse.<VariantResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.getVariantById(id))
                .build();
    }

    @GetMapping("")
    public ApiResponse<List<VariantResponse>> getVariants(@RequestParam(value = "productId", required = false) Long productId) {
        List<VariantResponse> result = productId == null
                ? variantService.getAllVariants()
                : variantService.getVariantsByProductId(productId);
        return ApiResponse.<List<VariantResponse>>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(result)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<VariantResponse> updateVariant(@PathVariable Long id, @RequestBody VariantDTO variantDTO) {
        return ApiResponse.<VariantResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.updateVariant(id, variantDTO))
                .build();
    }

    @PatchMapping("/{id}/stock")
    public ApiResponse<VariantResponse> updateStock(@PathVariable Long id, @RequestParam Long purchasedQuantity) {
        return ApiResponse.<VariantResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.updateStock(id, purchasedQuantity))
                .build();
    }

    @PatchMapping("/{id}/price")
    public ApiResponse<VariantResponse> updatePrice(
            @PathVariable Long id,
            @RequestParam Float price,
            @RequestParam(required = false) Float discountPrice) {
        return ApiResponse.<VariantResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.updatePrice(id, price, discountPrice))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<MessageResponse> deleteVariant(@PathVariable Long id) {
        return ApiResponse.<MessageResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.deleteVariant(id))
                .build();
    }
}
