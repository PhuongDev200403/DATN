package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.CouponDTO;
import com.buixuantruong.shopapp.dto.response.CouponResponse;
import com.buixuantruong.shopapp.model.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    // map từ dto qua entity
    @Mapping(target ="id", ignore = true)
    Coupon toCoupon(CouponDTO dto);

    // map từ entity qua response
    @Mapping(target = "discountAmount", ignore = true)
    CouponResponse toCouponResponse(Coupon coupon);
}
