package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.OrderDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.OrderListResponse;
import com.buixuantruong.shopapp.dto.response.OrderResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrderController {

    OrderService orderService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderService.createOrder(orderDTO))
                .build());
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(@Valid @RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderService.createOrder(orderDTO))
                .build());
    }

    @GetMapping("/user/{user_id}")
    public ApiResponse<List<OrderResponse>> getOrdersByUserId(@PathVariable("user_id") Long userId) {
        return ApiResponse.<List<OrderResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderService.getOrderByUserId(userId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable("id") Long orderId) {
        return ApiResponse.<OrderResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderService.getOrderById(orderId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderResponse> updateOrder(@PathVariable("id") @Valid Long id, @RequestBody @Valid OrderDTO order) {
        return ApiResponse.<OrderResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderService.updateOrder(id, order))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<MessageResponse> deleteOrder(@PathVariable("id") Long id) {
        return ApiResponse.<MessageResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderService.deleteOrder(id))
                .build();
    }

    @GetMapping("/get-user-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderListResponse> getProduct(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
        Page<OrderResponse> orders = orderService.getAllUserOrders(pageRequest);
        return ApiResponse.<OrderListResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(OrderListResponse.builder()
                        .orders(orders.getContent())
                        .totalPages(orders.getTotalPages())
                        .build())
                .build();
    }

    @GetMapping("/get-orders-by-keyword")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderListResponse> getOrdersByKeyword(
            @RequestParam("keyword") String keyword,
            @RequestParam("page") int page,
            @RequestParam("limit") int limit) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
        Page<OrderResponse> orders = orderService.getAllUserOrders(pageRequest);
        List<OrderResponse> orderList = orders.getContent();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.toLowerCase();
            orderList = orderList.stream()
                    .filter(order ->
                            (order.getFullName() != null && order.getFullName().toLowerCase().contains(searchKeyword)) ||
                                    (order.getEmail() != null && order.getEmail().toLowerCase().contains(searchKeyword)) ||
                                    (order.getPhoneNumber() != null && order.getPhoneNumber().toLowerCase().contains(searchKeyword)) ||
                                    (order.getAddress() != null && order.getAddress().toLowerCase().contains(searchKeyword)) ||
                                    (order.getNote() != null && order.getNote().toLowerCase().contains(searchKeyword)))
                    .toList();
        }

        return ApiResponse.<OrderListResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(OrderListResponse.builder()
                        .orders(orderList)
                        .totalPages(orders.getTotalPages())
                        .build())
                .build();
    }
}
