package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.response.OrderDetailResponse;
import com.buixuantruong.shopapp.model.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = VariantMapper.class)
public interface OrderDetailMapper {
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "variantResponse", source = "variant")
    OrderDetailResponse toResponse(OrderDetail orderDetail);
}
