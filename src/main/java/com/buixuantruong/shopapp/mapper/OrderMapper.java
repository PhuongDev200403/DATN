package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.OrderDTO;
import com.buixuantruong.shopapp.dto.response.OrderResponse;
import com.buixuantruong.shopapp.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderDetailMapper.class)
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "shippingFee", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "orderDetails", ignore = true)
    @Mapping(target = "shippingDate", source = "shippingDate")
    Order toOrder(OrderDTO dto);

    OrderDTO toOrderDTO(Order order);

    @Mapping(target = "userId", source = "user.id")
    OrderResponse toResponse(Order order);
}
