package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.CartItemDTO;
import com.buixuantruong.shopapp.dto.OrderDetailDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.OrderDTO;
import com.buixuantruong.shopapp.dto.response.OrderResponse;
import com.buixuantruong.shopapp.dto.response.ProductResponse;
import com.buixuantruong.shopapp.exception.DataNotFoundException;
import com.buixuantruong.shopapp.exception.InvalidParamException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.mapper.OrderMapper;
import com.buixuantruong.shopapp.model.Order;
import com.buixuantruong.shopapp.model.OrderDetail;
import com.buixuantruong.shopapp.model.Product;
import com.buixuantruong.shopapp.model.Specification;
import com.buixuantruong.shopapp.model.Variant;
import com.buixuantruong.shopapp.service.CartService;
import com.buixuantruong.shopapp.service.GHNService;
import com.buixuantruong.shopapp.utils.fiels.OrderStatusField;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.repository.OrderDetailRepository;
import com.buixuantruong.shopapp.repository.OrderRepository;
import com.buixuantruong.shopapp.repository.ProductRepository;
import com.buixuantruong.shopapp.repository.UserRepository;
import com.buixuantruong.shopapp.service.EmailService;
import com.buixuantruong.shopapp.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    UserRepository userRepository;
    OrderRepository orderRepository;
    OrderMapper orderMapper;
    ProductRepository productRepository;
    OrderDetailRepository orderDetailRepository;
    EmailService emailService;
    GHNService ghnService;
    CartService cartService;

    @Override
    @Transactional
    public ApiResponse<Object> createOrder(OrderDTO orderDTO) throws DataNotFoundException {
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if cartItems exist
        if (orderDTO.getCartItems() == null || orderDTO.getCartItems().isEmpty()) {
            throw new DataNotFoundException("Cart items cannot be empty");
        }
        
        // Verify product quantities before creating the order
        List<String> insufficientStockItems = new ArrayList<>();
        for (CartItemDTO cartItem : orderDTO.getCartItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + cartItem.getProductId()));
            
            // Check if there's enough quantity in stock
            if (product.getQuantity() < cartItem.getQuantity()) {
                insufficientStockItems.add(String.format("Product '%s' has only %d items in stock, but %d were requested", 
                        product.getName(), product.getQuantity(), cartItem.getQuantity()));
            }
        }
        
        // If any product has insufficient stock, return error
        if (!insufficientStockItems.isEmpty()) {
            return ApiResponse.builder()
                    .code(StatusCode.INVALID_DATA.getCode())
                    .message("Insufficient stock for some products")
                    .result(insufficientStockItems)
                    .build();
        }

        Order order = orderMapper.toOrder(orderDTO);
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatusField.PENDING);

        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now() : orderDTO.getShippingDate();
        if(shippingDate.isBefore(LocalDate.now())) {
            throw new DataNotFoundException("Shipping date cannot be null or in the past");
        }

        order.setShippingDate(shippingDate);
        order.setActive(true);

        // Tính phí vận chuyển bằng GHN
        if (orderDTO.getDistrictId() != null && orderDTO.getWardCode() != null) {
            order.setProvinceId(orderDTO.getProvinceId());
            order.setDistrictId(orderDTO.getDistrictId());
            order.setWardCode(orderDTO.getWardCode());
            
            // Tính tổng khối lượng (giả sử mỗi sp 500g nếu chưa có cấu hình)
            int totalWeight = orderDTO.getCartItems().stream()
                    .mapToInt(item -> {
                        Product p = productRepository.findById(item.getProductId()).orElse(null);
                        return resolveProductWeight(p) * item.getQuantity();
                    }).sum();
            
            Long shippingFee = ghnService.calculateFee(orderDTO.getDistrictId(), orderDTO.getWardCode(), totalWeight);
            order.setShippingFee(shippingFee);
        }

        Order savedOrder = orderRepository.save(order);
        
        // Create order details and update product quantities
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItemDTO cartItem : orderDTO.getCartItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + cartItem.getProductId()));
            
            OrderDetail orderDetail = OrderDetail.builder()
                    .order(savedOrder)
                    .product(product)
                    .numberOfProducts(cartItem.getQuantity())
                    .price(product.getPrice())
                    .totalMoney((long) (product.getPrice() * cartItem.getQuantity()))
                    .build();
            
            orderDetails.add(orderDetailRepository.save(orderDetail));
            
            // Update product quantity
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }
        
        savedOrder.setOrderDetails(orderDetails);

        // Gửi email xác nhận đặt hàng (bất đồng bộ)
        try {
            double total = orderDetails.stream()
                    .mapToLong(od -> od.getTotalMoney()).sum();
            emailService.sendOrderConfirmation(
                    user.getEmail(),
                    user.getFullName() != null ? user.getFullName() : user.getUsername(),
                    savedOrder.getId(),
                    total);
        } catch (Exception e) {
            // Lỗi email không được làm fail đơn hàng
        }

        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(savedOrder)
                .build();
    }


    @Override
    public Order getOrderById(Long id) throws DataNotFoundException {
        return orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
    }

    @Override
    @Transactional
    public ApiResponse<Object> updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        User existingUser = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found"));
                
        // Update order fields from DTO instead of replacing the entire object
        order.setFullName(orderDTO.getFullName());
        order.setEmail(orderDTO.getEmail());
        order.setPhoneNumber(orderDTO.getPhoneNumber());
        order.setAddress(orderDTO.getAddress());
        order.setNote(orderDTO.getNote());
        
        // Update status if it's present in the DTO
        if (orderDTO.getStatus() != null && !orderDTO.getStatus().isEmpty()) {
            order.setStatus(orderDTO.getStatus());
        }
        
        order.setTotalMoney(orderDTO.getTotalMoney());
        order.setShippingMethod(orderDTO.getShippingMethod());
        if (orderDTO.getShippingAddress() != null) {
            order.setShippingAddress(orderDTO.getShippingAddress());
        }
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        if (orderDTO.getShippingDate() != null) {
            order.setShippingDate(orderDTO.getShippingDate());
        }
        
        // Keep the existing user association
        order.setUser(existingUser);
        
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderRepository.save(order))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Object> deleteOrder(Long id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if(optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setActive(false);
            orderRepository.save(order);
            return ApiResponse.builder()
                    .code(StatusCode.SUCCESS.getCode())
                    .message(StatusCode.SUCCESS.getMessage())
                    .result("Order deleted successfully")
                    .build();
        }
        return null;
    }

    @Override
    public ApiResponse<Object> getOrderByUserId(Long userId) {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(orderRepository.findByUserId(userId))
                .build();
    }

    @Override
    @Transactional
    public Page<OrderResponse> getAllUserOrders(PageRequest pageRequest) {
        // Use findAll directly and map to OrderResponse to prevent unnecessary loading of User objects
        return orderRepository.findAll(pageRequest).map(order -> {
            // Create OrderResponse manually without loading user details
            OrderResponse response = new OrderResponse();
            response.setId(order.getId());
            // Only set userId if user is not null
            if (order.getUser() != null) {
                response.setUserId(order.getUser().getId());
            }
            response.setFullName(order.getFullName());
            response.setPhoneNumber(order.getPhoneNumber());
            response.setEmail(order.getEmail());
            response.setAddress(order.getAddress());
            response.setNote(order.getNote());
            response.setOrderDate(order.getOrderDate());
            response.setStatus(order.getStatus());
            response.setTotalMoney(order.getTotalMoney());
            response.setShippingMethod(order.getShippingMethod());
            response.setShippingAddress(order.getShippingAddress());
            response.setShippingDate(order.getShippingDate());
            response.setPaymentMethod(order.getPaymentMethod());
            response.setShippingFee(order.getShippingFee());
            response.setProvinceId(order.getProvinceId());
            response.setDistrictId(order.getDistrictId());
            response.setWardCode(order.getWardCode());
            response.setOrderDetails(order.getOrderDetails());
            return response;
        });
    }

    private int resolveProductWeight(Product product) {
        if (product == null || product.getVariants() == null || product.getVariants().isEmpty()) {
            return 500;
        }
        Variant variant = product.getVariants().getFirst();
        if (variant.getWeight() != null) {
            return variant.getWeight();
        }
        Specification specification = variant.getSpecification();
        if (specification != null && specification.getWeight() != null) {
            return specification.getWeight();
        }
        return 500;
    }

}
