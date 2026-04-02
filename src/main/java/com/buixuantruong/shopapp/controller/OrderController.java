package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.*;
import com.buixuantruong.shopapp.dto.OrderDTO;
import com.buixuantruong.shopapp.exception.DataNotFoundException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Order;
import com.buixuantruong.shopapp.service.OrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrderController {

    OrderService orderService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<Object>> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        try {
            System.out.println("OrderController - Creating order with data: " + orderDTO);
            ApiResponse<Object> orderResponse = orderService.createOrder(orderDTO);
            System.out.println("OrderController - Order created successfully. Response: " + orderResponse);
            return ResponseEntity.ok(orderResponse);
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .code(404)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .code(400)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/user/{user_id}")
    public ApiResponse<Object> getOrdersByUserId(@PathVariable("user_id") Long userId) {
        return orderService.getOrderByUserId(userId);
    }

    @GetMapping("/{id}")
    public ApiResponse<Object> getOrderById(@PathVariable("id") Long orderId) throws DataNotFoundException {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(OrderResponse.fromOrder(orderService.getOrderById(orderId)))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<Object> updateOrder(@PathVariable("id") @Valid Long id, @RequestBody @Valid OrderDTO order) throws DataNotFoundException {
        return orderService.updateOrder(id, order);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteOrder(@PathVariable("id") Long id) {
        return orderService.deleteOrder(id);
    }

    @GetMapping("/get-user-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> getProduct(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        try {
            System.out.println("OrderController: get-user-orders called with page=" + page + ", limit=" + limit);
            PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
            Page<OrderResponse> orders = orderService.getAllUserOrders(pageRequest);
            System.out.println("OrderController: Retrieved " + orders.getContent().size() + " orders from service");
            int totalPages = orders.getTotalPages();
            List<OrderResponse> orderList = orders.getContent();
            OrderListResponse orderListResponse = OrderListResponse.builder()
                    .orders(orderList)
                    .totalPages(totalPages)
                    .build();
            System.out.println("OrderController: Returning " + orderList.size() + " orders to client");
            return ApiResponse.builder()
                    .result(orderListResponse)
                    .build();
        } catch (Exception e) {
            System.err.println("OrderController: Error fetching orders: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.builder()
                    .message("Error fetching orders: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/get-orders-by-keyword")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> getOrdersByKeyword(
            @RequestParam("keyword") String keyword,
            @RequestParam("page") int page,
            @RequestParam("limit") int limit) {
        try {
            PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
            // Just use the existing getAllUserOrders method, as we don't have a specific search method
            // In a real implementation, we would add a search method in the service
            Page<OrderResponse> orders = orderService.getAllUserOrders(pageRequest);
            int totalPages = orders.getTotalPages();
            List<OrderResponse> orderList = orders.getContent();

            // Filter orders on the server side if keyword is provided
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchKeyword = keyword.toLowerCase();
                orderList = orderList.stream()
                    .filter(order ->
                        (order.getFullName() != null && order.getFullName().toLowerCase().contains(searchKeyword)) ||
                        (order.getEmail() != null && order.getEmail().toLowerCase().contains(searchKeyword)) ||
                        (order.getPhoneNumber() != null && order.getPhoneNumber().toLowerCase().contains(searchKeyword)) ||
                        (order.getAddress() != null && order.getAddress().toLowerCase().contains(searchKeyword)) ||
                        (order.getNote() != null && order.getNote().toLowerCase().contains(searchKeyword))
                    )
                    .toList();
            }

            OrderListResponse orderListResponse = OrderListResponse.builder()
                    .orders(orderList)
                    .totalPages(totalPages)
                    .build();
            return ApiResponse.builder()
                    .result(orderListResponse)
                    .build();
        } catch (Exception e) {
            return ApiResponse.builder()
                    .message("Error fetching orders: " + e.getMessage())
                    .build();
        }
    }
}
