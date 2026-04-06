package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.GHNService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShippingController {

    GHNService ghnService;

    @GetMapping("/provinces")
    public ApiResponse<List<?>> getProvinces() {
        return ApiResponse.<List<?>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message("Get provinces successfully")
                .result(ghnService.getProvinces())
                .build();
    }

    @GetMapping("/districts/{provinceId}")
    public ApiResponse<List<?>> getDistricts(@PathVariable Integer provinceId) {
        return ApiResponse.<List<?>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message("Get districts successfully")
                .result(ghnService.getDistricts(provinceId))
                .build();
    }

    @GetMapping("/wards/{districtId}")
    public ApiResponse<List<?>> getWards(@PathVariable Integer districtId) {
        return ApiResponse.<List<?>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message("Get wards successfully")
                .result(ghnService.getWards(districtId))
                .build();
    }

    @GetMapping("/fee")
    public ApiResponse<Long> calculateFee(
            @RequestParam Integer districtId,
            @RequestParam String wardCode,
            @RequestParam(defaultValue = "1000") Integer weight) {
        return ApiResponse.<Long>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message("Calculate shipping fee successfully")
                .result(ghnService.calculateFee(districtId, wardCode, weight))
                .build();
    }
}
