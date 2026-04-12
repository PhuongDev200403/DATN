package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.ReturnProductUnitRequest;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.ReturnProductUnitResponse;
import com.buixuantruong.shopapp.dto.response.WarrantyCheckResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.ProductUnitService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product-units")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductUnitController {

    ProductUnitService productUnitService;

    @GetMapping("/warranty/{serialNumber}")
    public ApiResponse<WarrantyCheckResponse> checkWarranty(@PathVariable String serialNumber) {
        return ApiResponse.<WarrantyCheckResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productUnitService.checkWarranty(serialNumber))
                .build();
    }

    @PostMapping("/returns")
    public ApiResponse<ReturnProductUnitResponse> returnBySerial(@Valid @RequestBody ReturnProductUnitRequest request) {
        return ApiResponse.<ReturnProductUnitResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productUnitService.returnBySerial(request.getOrderId(), request.getSerialNumber()))
                .build();
    }
}
