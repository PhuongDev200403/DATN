package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.OrderDetailDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.OrderDetailResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.mapper.OrderDetailMapper;
import com.buixuantruong.shopapp.model.OrderDetail;
import com.buixuantruong.shopapp.service.OrderDetailService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/order_details")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrderDetailController {

    OrderDetailService orderDetailService;
    OrderDetailMapper orderDetailMapper;

    @GetMapping("")
    public ApiResponse<OrderDetailResponse> getOrderDetails(@Valid @PathVariable("id") Long id) {
        return ApiResponse.<OrderDetailResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderDetailMapper.toResponse(orderDetailService.getOrderDetailById(id)))
                .build();
    }

    @PostMapping("")
    public ApiResponse<OrderDetailResponse> addOrderDetail(@RequestBody @Valid OrderDetailDTO dto) {
        return ApiResponse.<OrderDetailResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderDetailMapper.toResponse(orderDetailService.createOrderDetail(dto)))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable @Valid Long id) {
        return ApiResponse.<OrderDetailResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderDetailMapper.toResponse(orderDetailService.getOrderDetailById(id)))
                .build();
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<List<OrderDetailResponse>> getOrderDetailsByOrderId(@PathVariable @Valid Long orderId) {
        List<OrderDetailResponse> orderDetails = orderDetailService.getOrderDetailByOrderId(orderId).stream()
                .map(orderDetailMapper::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<OrderDetailResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderDetails)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderDetailResponse> updateOrderDetail(@Valid @RequestBody OrderDetailDTO orderDetailDTO, @PathVariable @Valid Long id) {
        OrderDetail orderDetail = orderDetailService.updateOrderDetail(id, orderDetailDTO);
        return ApiResponse.<OrderDetailResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderDetailMapper.toResponse(orderDetail))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<MessageResponse> deleteOrderDetail(@PathVariable @Valid Long id) {
        return ApiResponse.<MessageResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderDetailService.deleteOrderDetail(id))
                .build();
    }
}
