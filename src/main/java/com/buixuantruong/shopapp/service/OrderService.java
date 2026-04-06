package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.OrderDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {
    OrderResponse createOrder(OrderDTO orderDTO);

    OrderResponse getOrderById(Long id);

    OrderResponse updateOrder(Long id, OrderDTO orderDTO);

    MessageResponse deleteOrder(Long id);

    List<OrderResponse> getOrderByUserId(Long userId);

    Page<OrderResponse> getAllUserOrders(PageRequest pageRequest);
}
