package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.CouponDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.CouponResponse;
import com.buixuantruong.shopapp.exception.DataNotFoundException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Coupon;
import com.buixuantruong.shopapp.model.CouponType;
import com.buixuantruong.shopapp.repository.CouponRepository;
import com.buixuantruong.shopapp.service.CouponService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponServiceImpl implements CouponService {

    CouponRepository couponRepository;

    @Override
    public ApiResponse<Object> createCoupon(CouponDTO couponDTO) throws Exception {
        if (couponRepository.existsByCode(couponDTO.getCode())) {
            throw new Exception("Coupon code already exists");
        }
        Coupon coupon = Coupon.builder()
                .code(couponDTO.getCode())
                .type(parseCouponType(couponDTO.getType()))
                .value(couponDTO.getValue())
                .minimumAmount(couponDTO.getMinimumAmount())
                .startAt(couponDTO.getStartAt())
                .endAt(couponDTO.getEndAt())
                .active(couponDTO.isActive())
                .build();
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(couponRepository.save(coupon))
                .build();
    }

    @Override
    public ApiResponse<Object> updateCoupon(Long id, CouponDTO couponDTO) throws Exception {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Coupon not found"));
        
        coupon.setCode(couponDTO.getCode());
        coupon.setType(parseCouponType(couponDTO.getType()));
        coupon.setValue(couponDTO.getValue());
        coupon.setMinimumAmount(couponDTO.getMinimumAmount());
        coupon.setStartAt(couponDTO.getStartAt());
        coupon.setEndAt(couponDTO.getEndAt());
        coupon.setActive(couponDTO.isActive());
        
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(couponRepository.save(coupon))
                .build();
    }

    @Override
    public ApiResponse<Object> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(coupons)
                .build();
    }

    @Override
    public ApiResponse<Object> getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id).orElse(null);
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(coupon)
                .build();
    }

    @Override
    public ApiResponse<Object> deleteCoupon(Long id) {
        couponRepository.deleteById(id);
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result("Coupon deleted successfully")
                .build();
    }

    @Override
    public ApiResponse<Object> calculateDiscount(String code, Double totalAmount) {
        Coupon coupon = couponRepository.findByCode(code).orElse(null);
        
        if (coupon == null || !coupon.isActive()) {
            return ApiResponse.builder().code(400).message("Invalid or inactive coupon").build();
        }

        LocalDateTime now = LocalDateTime.now();
        if ((coupon.getStartAt() != null && now.isBefore(coupon.getStartAt())) ||
            (coupon.getEndAt() != null && now.isAfter(coupon.getEndAt()))) {
            return ApiResponse.builder().code(400).message("Coupon is expired or not yet started").build();
        }

        if (coupon.getMinimumAmount() != null && totalAmount < coupon.getMinimumAmount()) {
            return ApiResponse.builder().code(400).message("Order amount is below minimum required").build();
        }

        double discountAmount = 0;
        if (coupon.getType() == CouponType.PECENTAGE) {
            discountAmount = totalAmount * (coupon.getValue() / 100);
        } else {
            discountAmount = coupon.getValue();
        }

        CouponResponse response = CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .type(coupon.getType().name())
                .value(coupon.getValue())
                .discountAmount(discountAmount)
                .build();

        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(response)
                .build();
    }

    private CouponType parseCouponType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Coupon type is required");
        }
        return switch (type.trim().toLowerCase()) {
            case "percentage" -> CouponType.PECENTAGE;
            case "fixed" -> CouponType.FIXED;
            default -> throw new IllegalArgumentException("Unsupported coupon type: " + type);
        };
    }
}
