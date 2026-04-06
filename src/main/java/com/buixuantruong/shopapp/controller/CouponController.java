package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.ApplyCouponRequest;
import com.buixuantruong.shopapp.dto.CouponDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.CouponResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.CouponService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponController {

    CouponService couponService;

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponResponse> createCoupon(@Valid @RequestBody CouponDTO couponDTO) {
        return ApiResponse.<CouponResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(couponService.createCoupon(couponDTO))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponResponse> updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponDTO couponDTO) {
        return ApiResponse.<CouponResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(couponService.updateCoupon(id, couponDTO))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MessageResponse> deleteCoupon(@PathVariable Long id) {
        return ApiResponse.<MessageResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(couponService.deleteCoupon(id))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponResponse> getCouponById(@PathVariable Long id) {
        return ApiResponse.<CouponResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(couponService.getCouponById(id))
                .build();
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CouponResponse>> getAllCoupons() {
        return ApiResponse.<List<CouponResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(couponService.getAllCoupons())
                .build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponResponse> activateCoupon(@PathVariable Long id) {
        return ApiResponse.<CouponResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(couponService.activateCoupon(id))
                .build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponResponse> deactivateCoupon(@PathVariable Long id) {
        return ApiResponse.<CouponResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(couponService.deactivateCoupon(id))
                .build();
    }

    @PostMapping("/apply")
    public ApiResponse<CouponResponse> applyCoupon(@Valid @RequestBody ApplyCouponRequest request) {
        return ApiResponse.<CouponResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(couponService.applyCoupon(request))
                .build();
    }
}
