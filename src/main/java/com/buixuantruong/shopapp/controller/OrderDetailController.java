package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.OrderDetailDTO;
import com.buixuantruong.shopapp.dto.response.OrderDetailResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.OrderDetail;
import com.buixuantruong.shopapp.service.OrderDetailService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/order_details")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrderDetailController {

    OrderDetailService orderDetailService;

    @GetMapping("")
    public ApiResponse<Object> getOrderDetails(@Valid @PathVariable("id") Long id) {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(OrderDetailResponse.fromOrderDetail(orderDetailService.getOrderDetailById(id)))
                .build();
    }

    @PostMapping("")
    public ApiResponse<Object> addOrderDetail(@RequestBody @Valid OrderDetailDTO dto) {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(OrderDetailResponse.fromOrderDetail(orderDetailService.createOrderDetail(dto)))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<Object> getOrderDetail(@PathVariable @Valid Long id) {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(OrderDetailResponse.fromOrderDetail(orderDetailService.getOrderDetailById(id)))
                .build();
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<Object> getOrderDetailsByOrderId(@PathVariable @Valid Long orderId) {
        List<OrderDetailResponse> orderDetails = orderDetailService.getOrderDetailByOrderId(orderId).stream()
                .map(OrderDetailResponse::fromOrderDetail)
                .collect(Collectors.toList());
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderDetails)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<Object> updateOrderDetail(@Valid @RequestBody OrderDetailDTO orderDetailDTO,
                                               @PathVariable @Valid Long id) {
        OrderDetail orderDetail = orderDetailService.updateOrderDetail(id, orderDetailDTO);
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(OrderDetailResponse.fromOrderDetail(orderDetail))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteOrderDetail(@PathVariable @Valid Long id) {
        return orderDetailService.deleteOrderDetail(id);
    }
}
