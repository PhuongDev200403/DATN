package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.ApplyCouponRequest;
import com.buixuantruong.shopapp.dto.CouponDTO;
import com.buixuantruong.shopapp.dto.response.CouponResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.mapper.CouponMapper;
import com.buixuantruong.shopapp.model.Coupon;
import com.buixuantruong.shopapp.model.CouponType;
import com.buixuantruong.shopapp.repository.CouponRepository;
import com.buixuantruong.shopapp.service.CouponService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponServiceImpl implements CouponService {

    CouponRepository couponRepository;
    CouponMapper couponMapper;

    @Override
    @Transactional
    public CouponResponse createCoupon(CouponDTO couponDTO) {
        String normalizedCode = normalizeCode(couponDTO.getCode());
        if (couponRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new AppException(StatusCode.COUPON_CODE_EXISTED);
        }

        validateCouponDto(couponDTO);

        Coupon coupon = couponMapper.toEntity(couponDTO);
        coupon.setCode(normalizedCode);
        coupon.setUsedCount(0);
        return couponMapper.toResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Long id, CouponDTO couponDTO) {
        Coupon coupon = getCouponEntityById(id);
        String normalizedCode = normalizeCode(couponDTO.getCode());
        boolean duplicatedCode = !coupon.getCode().equalsIgnoreCase(normalizedCode)
                && couponRepository.existsByCodeIgnoreCase(normalizedCode);
        if (duplicatedCode) {
            throw new AppException(StatusCode.COUPON_CODE_EXISTED);
        }

        validateCouponDto(couponDTO);
        couponMapper.updateEntity(couponDTO, coupon);
        coupon.setCode(normalizedCode);
        return couponMapper.toResponse(couponRepository.save(coupon));
    }

    @Override
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(couponMapper::toResponse)
                .toList();
    }

    @Override
    public CouponResponse getCouponById(Long id) {
        return couponMapper.toResponse(getCouponEntityById(id));
    }

    @Override
    @Transactional
    public MessageResponse deleteCoupon(Long id) {
        Coupon coupon = getCouponEntityById(id);
        couponRepository.delete(coupon);
        return MessageResponse.builder().message("Coupon deleted successfully").build();
    }

    @Override
    @Transactional
    public CouponResponse activateCoupon(Long id) {
        Coupon coupon = getCouponEntityById(id);
        coupon.setActive(true);
        return couponMapper.toResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse deactivateCoupon(Long id) {
        Coupon coupon = getCouponEntityById(id);
        coupon.setActive(false);
        return couponMapper.toResponse(couponRepository.save(coupon));
    }

    @Override
    public CouponResponse applyCoupon(ApplyCouponRequest request) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(normalizeCode(request.getCode()))
                .orElseThrow(() -> new AppException(StatusCode.COUPON_NOT_FOUND));

        validateCouponForAmount(coupon, request.getTotalAmount());

        BigDecimal discountAmount = calculateDiscountAmount(coupon, request.getTotalAmount());
        CouponResponse response = couponMapper.toResponse(coupon);
        response.setDiscountAmount(discountAmount);
        return response;
    }

    @Override
    @Transactional
    public BigDecimal markCouponAsUsed(Long couponId) {
        Coupon coupon = couponRepository.findByIdForUpdate(couponId)
                .orElseThrow(() -> new AppException(StatusCode.COUPON_NOT_FOUND));

        validateCouponForUsage(coupon);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
        return coupon.getValue();
    }

    private Coupon getCouponEntityById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.COUPON_NOT_FOUND));
    }

    private void validateCouponDto(CouponDTO couponDTO) {
        if (couponDTO.getType() == null) {
            throw new AppException(StatusCode.INVALID_COUPON_TYPE);
        }
        if (couponDTO.getValue() == null || couponDTO.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(StatusCode.INVALID_DATA);
        }
        if (couponDTO.getUsageLimit() == null || couponDTO.getUsageLimit() <= 0) {
            throw new AppException(StatusCode.INVALID_DATA);
        }
        if (couponDTO.getMinimumAmount() != null && couponDTO.getMinimumAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(StatusCode.INVALID_DATA);
        }
        if (couponDTO.getMaxDiscount() != null && couponDTO.getMaxDiscount().compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(StatusCode.INVALID_DATA);
        }
        if (couponDTO.getStartAt() != null && couponDTO.getEndAt() != null && couponDTO.getEndAt().isBefore(couponDTO.getStartAt())) {
            throw new AppException(StatusCode.INVALID_DATA);
        }
    }

    private void validateCouponForAmount(Coupon coupon, BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(StatusCode.INVALID_DATA);
        }

        validateCouponForUsage(coupon);
        if (coupon.getMinimumAmount() != null && totalAmount.compareTo(coupon.getMinimumAmount()) < 0) {
            throw new AppException(StatusCode.ORDER_AMOUNT_BELOW_MINIMUM);
        }
    }

    private void validateCouponForUsage(Coupon coupon) {
        if (!coupon.isActive()) {
            throw new AppException(StatusCode.INVALID_COUPON);
        }

        LocalDateTime now = LocalDateTime.now();
        if ((coupon.getStartAt() != null && now.isBefore(coupon.getStartAt()))
                || (coupon.getEndAt() != null && now.isAfter(coupon.getEndAt()))) {
            throw new AppException(StatusCode.COUPON_NOT_AVAILABLE);
        }

        if (coupon.getUsedCount() != null
                && coupon.getUsageLimit() != null
                && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new AppException(StatusCode.COUPON_USAGE_LIMIT_EXCEEDED);
        }
    }

    private BigDecimal calculateDiscountAmount(Coupon coupon, BigDecimal totalAmount) {
        BigDecimal discount = switch (coupon.getType()) {
            case PERCENTAGE -> totalAmount
                    .multiply(coupon.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED -> coupon.getValue();
        };

        if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
            discount = coupon.getMaxDiscount();
        }
        if (discount.compareTo(totalAmount) > 0) {
            discount = totalAmount;
        }

        return discount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new AppException(StatusCode.INVALID_COUPON);
        }
        return code.trim().toUpperCase();
    }
}
