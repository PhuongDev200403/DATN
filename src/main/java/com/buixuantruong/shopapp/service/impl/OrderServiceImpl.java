package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.OrderDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.OrderResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.mapper.OrderMapper;
import com.buixuantruong.shopapp.model.Cart;
import com.buixuantruong.shopapp.model.CartItem;
import com.buixuantruong.shopapp.model.Coupon;
import com.buixuantruong.shopapp.model.CouponType;
import com.buixuantruong.shopapp.model.Order;
import com.buixuantruong.shopapp.model.OrderDetail;
import com.buixuantruong.shopapp.model.Specification;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.model.Variant;
import com.buixuantruong.shopapp.repository.CartRepository;
import com.buixuantruong.shopapp.repository.CouponRepository;
import com.buixuantruong.shopapp.repository.OrderDetailRepository;
import com.buixuantruong.shopapp.repository.OrderRepository;
import com.buixuantruong.shopapp.repository.UserRepository;
import com.buixuantruong.shopapp.repository.VariantRepository;
import com.buixuantruong.shopapp.service.CartService;
import com.buixuantruong.shopapp.service.FlashSaleService;
import com.buixuantruong.shopapp.service.GHNService;
import com.buixuantruong.shopapp.service.OrderService;
import com.buixuantruong.shopapp.service.ProductUnitService;
import com.buixuantruong.shopapp.utils.fiels.OrderStatusField;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    UserRepository userRepository;
    OrderRepository orderRepository;
    OrderMapper orderMapper;
    OrderDetailRepository orderDetailRepository;
    VariantRepository variantRepository;
    CartRepository cartRepository;
    CouponRepository couponRepository;
    GHNService ghnService;
    CartService cartService;
    FlashSaleService flashSaleService;
    ProductUnitService productUnitService;

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

        List<CheckoutItem> checkoutItems = lockAndPrepareCartItems(cartItems);
        BigDecimal subTotal = calculateSubTotal(checkoutItems);
        BigDecimal shippingFee = calculateShippingFee(orderDTO, checkoutItems);
        CouponApplication couponApplication = applyCouponIfPresent(orderDTO.getCouponCode(), subTotal);
        BigDecimal totalMoney = subTotal.subtract(couponApplication.discountAmount()).add(shippingFee)
                .max(ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        reserveInventory(checkoutItems);
        reserveFlashSaleQuota(checkoutItems);

        Order order = buildPendingOrder(orderDTO, user);
        order.setCouponCode(couponApplication.couponCode());
        order.setDiscountAmount(couponApplication.discountAmount());
        order.setShippingFee(shippingFee);
        order.setTotalMoney(totalMoney);
        updateOrderStatusAfterCheckout(order, orderDTO);

        Order savedOrder = orderRepository.save(order);
        List<OrderDetail> orderDetails = createOrderDetails(savedOrder, checkoutItems);
        savedOrder.setOrderDetails(orderDetails);
        productUnitService.reserveUnits(savedOrder.getId());
        if (isPaymentCompletedAtCheckout(savedOrder, orderDTO)) {
            productUnitService.confirmUnitsSold(savedOrder.getId());
        }

        cartService.clearCart(user.getId());
        return orderMapper.toResponse(orderRepository.save(savedOrder));
    }

    @Override
    @Transactional
    public void finalizeOnlinePayment(Long orderId, boolean paymentSuccessful) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(StatusCode.ORDER_NOT_FOUND));

        if (OrderStatusField.CONFIRMED.equals(order.getStatus())
                || OrderStatusField.PAID.equals(order.getStatus())
                || OrderStatusField.FAILED.equals(order.getStatus())) {
            return;
        }

        if (paymentSuccessful) {
            productUnitService.confirmUnitsSold(order.getId());
            order.setStatus(OrderStatusField.PAID);
            order.setPaymentStatus(OrderStatusField.PAID);
            order.setPaymentDate(LocalDateTime.now().toString());
            orderRepository.save(order);
            return;
        }

        productUnitService.releaseUnits(order.getId());
        releaseReservedInventory(order);
        releaseCouponUsage(order.getCouponCode());
        order.setStatus(OrderStatusField.FAILED);
        order.setPaymentStatus(OrderStatusField.FAILED);
        orderRepository.save(order);
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
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllUserOrders(PageRequest pageRequest) {
        return orderRepository.findAll(pageRequest).map(orderMapper::toResponse);
    }

    private Order buildPendingOrder(OrderDTO orderDTO, User user) {
        Order order = orderMapper.toOrder(orderDTO);
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatusField.PENDING);
        order.setPaymentStatus(OrderStatusField.PENDING);
        order.setDiscountAmount(ZERO);
        order.setActive(true);
        order.setPaymentMethod(normalizePaymentMethod(orderDTO.getPaymentMethod()));

        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now() : orderDTO.getShippingDate();
        if (shippingDate.isBefore(LocalDate.now())) {
            throw new AppException(StatusCode.SHIPPING_DATE_INVALID);
        }
        order.setShippingDate(shippingDate);
        return order;
    }

    private List<CheckoutItem> lockAndPrepareCartItems(List<CartItem> cartItems) {
        List<CheckoutItem> checkoutItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            if (cartItem.getVariant() == null || cartItem.getVariant().getId() == null) {
                throw new AppException(StatusCode.VARIANT_NOT_FOUND);
            }

            Variant variant = variantRepository.findByIdForUpdate(cartItem.getVariant().getId())
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

            checkoutItems.add(new CheckoutItem(
                    variant,
                    quantity,
                    resolveCurrentUnitPrice(variant)
            ));
        }
        return checkoutItems;
    }

    private BigDecimal calculateSubTotal(List<CheckoutItem> items) {
        return items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateShippingFee(OrderDTO orderDTO, List<CheckoutItem> items) {
        if (orderDTO.getDistrictId() == null || orderDTO.getWardCode() == null) {
            return ZERO;
        }

        int totalWeight = items.stream()
                .mapToInt(item -> resolveVariantWeight(item.variant()) * item.quantity())
                .sum();

        return BigDecimal.valueOf(ghnService.calculateFee(orderDTO.getDistrictId(), orderDTO.getWardCode(), totalWeight))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private CouponApplication applyCouponIfPresent(String couponCode, BigDecimal subTotal) {
        if (couponCode == null || couponCode.isBlank()) {
            return new CouponApplication(null, ZERO);
        }

        Coupon coupon = couponRepository.findByCodeForUpdate(couponCode.trim().toUpperCase())
                .orElseThrow(() -> new AppException(StatusCode.COUPON_NOT_FOUND));

        validateCoupon(coupon, subTotal);
        BigDecimal discountAmount = calculateCouponDiscount(coupon, subTotal);
        int usedCount = coupon.getUsedCount() == null ? 0 : coupon.getUsedCount();
        coupon.setUsedCount(usedCount + 1);
        couponRepository.save(coupon);
        return new CouponApplication(coupon.getCode(), discountAmount);
    }

    private void validateCoupon(Coupon coupon, BigDecimal subTotal) {
        if (!coupon.isActive()) {
            throw new AppException(StatusCode.INVALID_COUPON);
        }

        LocalDateTime now = LocalDateTime.now();
        if ((coupon.getStartAt() != null && now.isBefore(coupon.getStartAt()))
                || (coupon.getEndAt() != null && now.isAfter(coupon.getEndAt()))) {
            throw new AppException(StatusCode.COUPON_NOT_AVAILABLE);
        }

        if (coupon.getUsedCount() != null
                && coupon.getUsageLimit() != null
                && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new AppException(StatusCode.COUPON_USAGE_LIMIT_EXCEEDED);
        }

        if (coupon.getMinimumAmount() != null && subTotal.compareTo(coupon.getMinimumAmount()) < 0) {
            throw new AppException(StatusCode.ORDER_AMOUNT_BELOW_MINIMUM);
        }
    }

    private BigDecimal calculateCouponDiscount(Coupon coupon, BigDecimal subTotal) {
        BigDecimal discount = coupon.getType() == CouponType.PERCENTAGE
                ? subTotal.multiply(coupon.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : coupon.getValue();

        if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
            discount = coupon.getMaxDiscount();
        }

        return discount.min(subTotal).max(ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private void reserveInventory(List<CheckoutItem> items) {
        for (CheckoutItem item : items) {
            Variant variant = item.variant();
            long currentStock = variant.getStock() == null ? 0L : variant.getStock();
            if (currentStock < item.quantity()) {
                throw new AppException(StatusCode.INVALID_QUANTITY);
            }
            variant.setStock(currentStock - item.quantity());
            variantRepository.save(variant);
        }
    }

    private void reserveFlashSaleQuota(List<CheckoutItem> items) {
        for (CheckoutItem item : items) {
            flashSaleService.applyFlashSaleWhenCheckout(item.variant().getId(), item.quantity());
        }
    }

    private List<OrderDetail> createOrderDetails(Order order, List<CheckoutItem> checkoutItems) {
        return checkoutItems.stream()
                .map(item -> orderDetailRepository.save(OrderDetail.builder()
                        .order(order)
                        .variant(item.variant())
                        .numberOfProducts(item.quantity())
                        .price(item.unitPrice())
                        .totalMoney(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())).setScale(2, RoundingMode.HALF_UP))
                        .color(item.variant().getColor())
                        .build()))
                .toList();
    }

    private void updateOrderStatusAfterCheckout(Order order, OrderDTO orderDTO) {
        String paymentMethod = normalizePaymentMethod(order.getPaymentMethod());
        if ("COD".equals(paymentMethod)) {
            order.setStatus(OrderStatusField.CONFIRMED);
            order.setPaymentStatus(OrderStatusField.CONFIRMED);
            order.setPaymentDate(LocalDateTime.now().toString());
            return;
        }

        if (Boolean.TRUE.equals(orderDTO.getPaymentSuccess())) {
            order.setStatus(OrderStatusField.PAID);
            order.setPaymentStatus(OrderStatusField.PAID);
            order.setPaymentDate(LocalDateTime.now().toString());
        }
    }

    private boolean isPaymentCompletedAtCheckout(Order order, OrderDTO orderDTO) {
        String paymentMethod = normalizePaymentMethod(order.getPaymentMethod());
        if ("COD".equals(paymentMethod)) {
            return true;
        }
        return Boolean.TRUE.equals(orderDTO.getPaymentSuccess());
    }

    private void releaseReservedInventory(Order order) {
        if (order.getOrderDetails() == null) {
            return;
        }

        for (OrderDetail orderDetail : order.getOrderDetails()) {
            Variant variant = variantRepository.findByIdForUpdate(orderDetail.getVariant().getId())
                    .orElseThrow(() -> new AppException(StatusCode.VARIANT_NOT_FOUND));
            long currentStock = variant.getStock() == null ? 0L : variant.getStock();
            variant.setStock(currentStock + orderDetail.getNumberOfProducts());
            variantRepository.save(variant);
            flashSaleService.releaseFlashSaleReservation(variant.getId(), orderDetail.getNumberOfProducts());
        }
    }

    private void releaseCouponUsage(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return;
        }

        couponRepository.findByCodeForUpdate(couponCode)
                .ifPresent(coupon -> {
                    int usedCount = coupon.getUsedCount() == null ? 0 : coupon.getUsedCount();
                    coupon.setUsedCount(Math.max(0, usedCount - 1));
                    couponRepository.save(coupon);
                });
    }

    private BigDecimal resolveCurrentUnitPrice(Variant variant) {
        BigDecimal flashSalePrice = BigDecimal.valueOf(flashSaleService.getFlashSalePrice(variant.getId()))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal basePrice = resolveVariantBasePrice(variant);
        if (flashSalePrice.signum() > 0 && flashSalePrice.compareTo(basePrice) < 0) {
            return flashSalePrice;
        }
        return basePrice;
    }

    private BigDecimal resolveVariantBasePrice(Variant variant) {
        if (variant.getDiscountPrice() != null && variant.getDiscountPrice().signum() > 0) {
            return variant.getDiscountPrice().setScale(2, RoundingMode.HALF_UP);
        }
        if (variant.getPrice() != null && variant.getPrice().signum() > 0) {
            return variant.getPrice().setScale(2, RoundingMode.HALF_UP);
        }
        return ZERO;
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

    private record CheckoutItem(Variant variant, int quantity, BigDecimal unitPrice) {
    }

    private record CouponApplication(String couponCode, BigDecimal discountAmount) {
    }
}
