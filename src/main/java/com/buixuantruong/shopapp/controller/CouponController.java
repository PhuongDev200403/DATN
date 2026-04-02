package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.CouponDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.service.CouponService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponController {

    CouponService couponService;

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> createCoupon(@Valid @RequestBody CouponDTO couponDTO) throws Exception {
        return couponService.createCoupon(couponDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponDTO couponDTO) throws Exception {
        return couponService.updateCoupon(id, couponDTO);
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> getCouponById(@PathVariable Long id) {
        return couponService.getCouponById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> deleteCoupon(@PathVariable Long id) {
        return couponService.deleteCoupon(id);
    }

    // Endpoint công khai cho người dùng kiểm tra mã giảm giá
    @GetMapping("/apply")
    public ApiResponse<Object> applyCoupon(
            @RequestParam("code") String code,
            @RequestParam("total_amount") Double totalAmount) {
        return couponService.calculateDiscount(code, totalAmount);
    }
}
