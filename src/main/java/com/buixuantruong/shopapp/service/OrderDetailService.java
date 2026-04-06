package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.OrderDetailDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.model.OrderDetail;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderDetailService {
    OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO);
    OrderDetail getOrderDetailById(Long id);
    OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO);
    MessageResponse deleteOrderDetail(Long id);
    List<OrderDetail> getOrderDetailByOrderId(Long orderId);
}
