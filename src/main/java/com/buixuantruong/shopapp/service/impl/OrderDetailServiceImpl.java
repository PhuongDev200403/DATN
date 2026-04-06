package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.OrderDetailDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Order;
import com.buixuantruong.shopapp.model.OrderDetail;
import com.buixuantruong.shopapp.model.Variant;
import com.buixuantruong.shopapp.repository.OrderDetailRepository;
import com.buixuantruong.shopapp.repository.OrderRepository;
import com.buixuantruong.shopapp.repository.VariantRepository;
import com.buixuantruong.shopapp.service.OrderDetailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderDetailServiceImpl implements OrderDetailService {
    OrderDetailRepository orderDetailRepository;
    OrderRepository orderRepository;
    VariantRepository variantRepository;

    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) {
        Order order = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new AppException(StatusCode.ORDER_NOT_FOUND));
        Variant variant = variantRepository.findById(orderDetailDTO.getVariantId())
                .orElseThrow(() -> new AppException(StatusCode.VARIANT_NOT_FOUND));
        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .variant(variant)
                .numberOfProducts(orderDetailDTO.getNumberOfProducts())
                .price(orderDetailDTO.getPrice())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .color(orderDetailDTO.getColor())
                .build();

        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public OrderDetail getOrderDetailById(Long id) {
        return orderDetailRepository.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.ORDER_DETAIL_NOT_FOUND));
    }

    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) {
        OrderDetail existingOrderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.ORDER_DETAIL_NOT_FOUND));
        Order existingOrder = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new AppException(StatusCode.ORDER_NOT_FOUND));
        Variant existingVariant = variantRepository.findById(orderDetailDTO.getVariantId())
                .orElseThrow(() -> new AppException(StatusCode.VARIANT_NOT_FOUND));
        existingOrderDetail.setOrder(existingOrder);
        existingOrderDetail.setVariant(existingVariant);
        existingOrderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
        existingOrderDetail.setPrice(orderDetailDTO.getPrice());
        existingOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
        existingOrderDetail.setColor(orderDetailDTO.getColor());
        return orderDetailRepository.save(existingOrderDetail);
    }

    @Override
    public MessageResponse deleteOrderDetail(Long id) {
        orderDetailRepository.deleteById(id);
        return MessageResponse.builder().message("Order detail deleted successfully").build();
    }

    @Override
    public List<OrderDetail> getOrderDetailByOrderId(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
}
