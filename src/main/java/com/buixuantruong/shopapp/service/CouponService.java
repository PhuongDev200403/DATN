package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.CouponDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import java.util.List;

public interface CouponService {
    ApiResponse<Object> createCoupon(CouponDTO couponDTO) throws Exception;
    ApiResponse<Object> updateCoupon(Long id, CouponDTO couponDTO) throws Exception;
    ApiResponse<Object> getAllCoupons();
    ApiResponse<Object> getCouponById(Long id);
    ApiResponse<Object> deleteCoupon(Long id);
    
    // Phương thức quan trọng nhất cho người dùng: Kiểm tra và tính tiền giảm
    ApiResponse<Object> calculateDiscount(String code, Double totalAmount);
}
