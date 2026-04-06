package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.ApplyCouponRequest;
import com.buixuantruong.shopapp.dto.CouponDTO;
import com.buixuantruong.shopapp.dto.response.CouponResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {
    CouponResponse createCoupon(CouponDTO couponDTO);

    CouponResponse updateCoupon(Long id, CouponDTO couponDTO);

    List<CouponResponse> getAllCoupons();

    CouponResponse getCouponById(Long id);

    MessageResponse deleteCoupon(Long id);

    CouponResponse activateCoupon(Long id);

    CouponResponse deactivateCoupon(Long id);

    CouponResponse applyCoupon(ApplyCouponRequest request);

    BigDecimal markCouponAsUsed(Long couponId);
}
