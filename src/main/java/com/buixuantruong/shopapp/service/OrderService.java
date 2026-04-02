package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.OrderDTO;
import com.buixuantruong.shopapp.dto.response.OrderResponse;
import com.buixuantruong.shopapp.dto.response.ProductResponse;
import com.buixuantruong.shopapp.exception.DataNotFoundException;
import com.buixuantruong.shopapp.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    ApiResponse<Object> createOrder(OrderDTO orderDTO) throws DataNotFoundException;
    Order getOrderById(Long id) throws DataNotFoundException;
    ApiResponse<Object> updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException;
    ApiResponse<Object> deleteOrder(Long id);
    ApiResponse<Object> getOrderByUserId(Long userId);
    Page<OrderResponse> getAllUserOrders(PageRequest pageRequest);
}
