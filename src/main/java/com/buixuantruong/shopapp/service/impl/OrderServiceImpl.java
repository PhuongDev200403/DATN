package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.OrderDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.OrderResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.mapper.OrderMapper;
import com.buixuantruong.shopapp.model.Cart;
import com.buixuantruong.shopapp.model.CartItem;
import com.buixuantruong.shopapp.model.Order;
import com.buixuantruong.shopapp.model.OrderDetail;
import com.buixuantruong.shopapp.model.Specification;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.model.Variant;
import com.buixuantruong.shopapp.repository.CartRepository;
import com.buixuantruong.shopapp.repository.OrderDetailRepository;
import com.buixuantruong.shopapp.repository.OrderRepository;
import com.buixuantruong.shopapp.repository.UserRepository;
import com.buixuantruong.shopapp.repository.VariantRepository;
import com.buixuantruong.shopapp.service.CartService;
import com.buixuantruong.shopapp.service.GHNService;
import com.buixuantruong.shopapp.service.OrderService;
import com.buixuantruong.shopapp.utils.fiels.OrderStatusField;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    OrderDetailRepository orderDetailRepository;
    VariantRepository variantRepository;
    CartRepository cartRepository;
    GHNService ghnService;
    CartService cartService;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderDTO orderDTO) {
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(StatusCode.CART_ITEMS_EMPTY));
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new AppException(StatusCode.CART_ITEMS_EMPTY);
        }

        Order order = buildPendingOrder(orderDTO, user);
        List<ValidatedCartItem> validatedCartItems = validateCartItems(cartItems);

        long subTotal = validatedCartItems.stream()
                .mapToLong(item -> Math.round(item.unitPrice() * item.quantity()))
                .sum();

        Long shippingFee = calculateShippingFee(orderDTO, validatedCartItems);
        order.setShippingFee(shippingFee);
        order.setTotalMoney(subTotal + shippingFee);

        Order savedOrder = orderRepository.save(order);
        List<OrderDetail> orderDetails = createOrderDetails(savedOrder, validatedCartItems);
        savedOrder.setOrderDetails(orderDetails);

        applyPaymentSimulation(savedOrder, orderDTO);
        if (shouldDecreaseStock(savedOrder)) {
            decrementStock(validatedCartItems);
        }

        cartService.clearCart(user.getId());
        return orderMapper.toResponse(orderRepository.save(savedOrder));
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.ORDER_NOT_FOUND));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, OrderDTO orderDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.ORDER_NOT_FOUND));
        User existingUser = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));

        order.setUser(existingUser);
        order.setFullName(orderDTO.getFullName());
        order.setEmail(orderDTO.getEmail());
        order.setPhoneNumber(orderDTO.getPhoneNumber());
        order.setAddress(orderDTO.getAddress());
        order.setNote(orderDTO.getNote());
        order.setShippingMethod(orderDTO.getShippingMethod());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setProvinceId(orderDTO.getProvinceId());
        order.setDistrictId(orderDTO.getDistrictId());
        order.setWardCode(orderDTO.getWardCode());

        if (orderDTO.getStatus() != null && !orderDTO.getStatus().isBlank()) {
            order.setStatus(orderDTO.getStatus());
        }
        if (orderDTO.getShippingDate() != null) {
            if (orderDTO.getShippingDate().isBefore(LocalDate.now())) {
                throw new AppException(StatusCode.SHIPPING_DATE_INVALID);
            }
            order.setShippingDate(orderDTO.getShippingDate());
        }

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public MessageResponse deleteOrder(Long id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setActive(false);
            orderRepository.save(order);
            return MessageResponse.builder().message("Order deleted successfully").build();
        }
        throw new AppException(StatusCode.ORDER_NOT_FOUND);
    }

    @Override
    public List<OrderResponse> getOrderByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public Page<OrderResponse> getAllUserOrders(PageRequest pageRequest) {
        return orderRepository.findAll(pageRequest).map(orderMapper::toResponse);
    }

    private Order buildPendingOrder(OrderDTO orderDTO, User user) {
        Order order = orderMapper.toOrder(orderDTO);
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatusField.PENDING);
        order.setPaymentStatus(OrderStatusField.PENDING);
        order.setActive(true);
        order.setPaymentMethod(normalizePaymentMethod(orderDTO.getPaymentMethod()));

        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now() : orderDTO.getShippingDate();
        if (shippingDate.isBefore(LocalDate.now())) {
            throw new AppException(StatusCode.SHIPPING_DATE_INVALID);
        }
        order.setShippingDate(shippingDate);
        return order;
    }

    private List<ValidatedCartItem> validateCartItems(List<CartItem> cartItems) {
        List<ValidatedCartItem> validatedItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            if (cartItem.getVariant() == null || cartItem.getVariant().getId() == null) {
                throw new AppException(StatusCode.VARIANT_NOT_FOUND);
            }
            Variant variant = variantRepository.findById(cartItem.getVariant().getId())
                    .orElseThrow(() -> new AppException(StatusCode.VARIANT_NOT_FOUND));
            if (Boolean.FALSE.equals(variant.getIsActive())) {
                throw new AppException(StatusCode.INVALID_DATA);
            }
            int quantity = cartItem.getQuantity() == null ? 0 : cartItem.getQuantity();
            if (quantity <= 0) {
                throw new AppException(StatusCode.INVALID_QUANTITY);
            }
            long stock = variant.getStock() == null ? 0L : variant.getStock();
            if (stock < quantity) {
                throw new AppException(StatusCode.INVALID_QUANTITY);
            }

            float latestPrice = resolveLatestPrice(variant);
            validatedItems.add(new ValidatedCartItem(variant, quantity, latestPrice));
        }
        return validatedItems;
    }

    private Long calculateShippingFee(OrderDTO orderDTO, List<ValidatedCartItem> items) {
        if (orderDTO.getDistrictId() == null || orderDTO.getWardCode() == null) {
            return 0L;
        }

        int totalWeight = items.stream()
                .mapToInt(item -> resolveVariantWeight(item.variant()) * item.quantity())
                .sum();

        return ghnService.calculateFee(orderDTO.getDistrictId(), orderDTO.getWardCode(), totalWeight);
    }

    private List<OrderDetail> createOrderDetails(Order order, List<ValidatedCartItem> validatedItems) {
        return validatedItems.stream()
                .map(item -> orderDetailRepository.save(OrderDetail.builder()
                        .order(order)
                        .variant(item.variant())
                        .numberOfProducts(item.quantity())
                        .price(item.unitPrice())
                        .totalMoney((long) Math.round(item.unitPrice() * item.quantity()))
                        .color(item.variant().getColor())
                        .build()))
                .map(OrderDetail.class::cast)
                .toList();
    }

    private void applyPaymentSimulation(Order order, OrderDTO orderDTO) {
        boolean cod = "COD".equalsIgnoreCase(normalizePaymentMethod(orderDTO.getPaymentMethod()));
        if (cod) {
            order.setStatus(OrderStatusField.CONFIRMED);
            order.setPaymentStatus(OrderStatusField.CONFIRMED);
            return;
        }

        if (Boolean.TRUE.equals(orderDTO.getPaymentSuccess())) {
            order.setStatus(OrderStatusField.PAID);
            order.setPaymentStatus(OrderStatusField.PAID);
            order.setPaymentDate(LocalDateTime.now().toString());
            return;
        }

        order.setStatus(OrderStatusField.PENDING);
        order.setPaymentStatus("FAILED");
    }

    private boolean shouldDecreaseStock(Order order) {
        return OrderStatusField.PAID.equals(order.getStatus())
                || OrderStatusField.CONFIRMED.equals(order.getStatus());
    }

    private void decrementStock(List<ValidatedCartItem> items) {
        for (ValidatedCartItem item : items) {
            Variant variant = item.variant();
            long currentStock = variant.getStock() == null ? 0L : variant.getStock();
            if (currentStock < item.quantity()) {
                throw new AppException(StatusCode.INVALID_QUANTITY);
            }
            variant.setStock(currentStock - item.quantity());
            variantRepository.save(variant);
        }
    }

    private float resolveLatestPrice(Variant variant) {
        if (variant.getDiscountPrice() != null && variant.getDiscountPrice() > 0) {
            return variant.getDiscountPrice();
        }
        return variant.getPrice() == null ? 0F : variant.getPrice();
    }

    private int resolveVariantWeight(Variant variant) {
        if (variant.getWeight() != null) {
            return variant.getWeight();
        }
        Specification specification = variant.getSpecification();
        if (specification != null && specification.getWeight() != null) {
            return specification.getWeight();
        }
        return 500;
    }

    private String normalizePaymentMethod(String paymentMethod) {
        return paymentMethod == null ? "" : paymentMethod.trim().toUpperCase();
    }

    private record ValidatedCartItem(Variant variant, int quantity, float unitPrice) {
    }
}
