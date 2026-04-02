package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.OrderDTO;
import com.buixuantruong.shopapp.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shippingDate", source = "shippingDate")
    Order toOrder(OrderDTO dto);

    OrderDTO toOrderDTO(Order order);
}
