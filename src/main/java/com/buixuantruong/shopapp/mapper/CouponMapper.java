package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.CouponDTO;
import com.buixuantruong.shopapp.dto.response.CouponResponse;
import com.buixuantruong.shopapp.model.Coupon;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    Coupon toEntity(CouponDTO dto);

    CouponResponse toResponse(Coupon coupon);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    void updateEntity(CouponDTO dto, @MappingTarget Coupon coupon);
}
