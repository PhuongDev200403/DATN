package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.GHNService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShippingController {

    GHNService ghnService;

    @GetMapping("/provinces")
    public ApiResponse<Object> getProvinces() {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message("Lấy danh sách tỉnh thành thành công")
                .result(ghnService.getProvinces())
                .build();
    }

    @GetMapping("/districts/{provinceId}")
    public ApiResponse<Object> getDistricts(@PathVariable Integer provinceId) {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message("Lấy danh sách quận huyện thành công")
                .result(ghnService.getDistricts(provinceId))
                .build();
    }

    @GetMapping("/wards/{districtId}")
    public ApiResponse<Object> getWards(@PathVariable Integer districtId) {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message("Lấy danh sách phường xã thành công")
                .result(ghnService.getWards(districtId))
                .build();
    }

    @GetMapping("/fee")
    public ApiResponse<Object> calculateFee(
            @RequestParam Integer districtId,
            @RequestParam String wardCode,
            @RequestParam(defaultValue = "1000") Integer weight) {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message("Tính phí vận chuyển thành công")
                .result(ghnService.calculateFee(districtId, wardCode, weight))
                .build();
    }
}
