package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.response.ReturnProductUnitResponse;
import com.buixuantruong.shopapp.dto.response.WarrantyCheckResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Order;
import com.buixuantruong.shopapp.model.OrderDetail;
import com.buixuantruong.shopapp.model.ProductUnit;
import com.buixuantruong.shopapp.model.ProductUnitStatus;
import com.buixuantruong.shopapp.model.Variant;
import com.buixuantruong.shopapp.repository.OrderRepository;
import com.buixuantruong.shopapp.repository.ProductUnitRepository;
import com.buixuantruong.shopapp.repository.VariantRepository;
import com.buixuantruong.shopapp.service.ProductUnitService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductUnitServiceImpl implements ProductUnitService {

    static final int DEFAULT_WARRANTY_MONTHS = 12;

    ProductUnitRepository productUnitRepository;
    OrderRepository orderRepository;
    VariantRepository variantRepository;

    @Override
    @Transactional
    public void generateUnits(Long variantId, long quantity) {
        if (quantity <= 0) {
            return;
        }

        Variant variant = variantRepository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new AppException(StatusCode.VARIANT_NOT_FOUND));

        List<ProductUnit> units = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            units.add(ProductUnit.builder()
                    .variant(variant)
                    .serialNumber(generateUniqueSerialNumber())
                    .status(ProductUnitStatus.IN_STOCK)
                    .build());
        }
        productUnitRepository.saveAll(units);
    }

    @Override
    @Transactional(readOnly = true)
    public WarrantyCheckResponse checkWarranty(String serialNumber) {
        ProductUnit unit = findUnitBySerial(serialNumber);
        validateVariantProduct(unit);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warrantyExpiredAt = unit.getWarrantyExpiredAt();
        boolean inWarranty = warrantyExpiredAt != null && !now.isAfter(warrantyExpiredAt);

        return WarrantyCheckResponse.builder()
                .serialNumber(unit.getSerialNumber())
                .variantId(unit.getVariant().getId())
                .orderId(unit.getOrder() == null ? null : unit.getOrder().getId())
                .warrantyExpiredAt(warrantyExpiredAt)
                .inWarranty(inWarranty)
                .status(inWarranty ? "IN_WARRANTY" : "EXPIRED")
                .build();
    }

    @Override
    @Transactional
    public ReturnProductUnitResponse returnBySerial(Long orderId, String serialNumber) {
        ProductUnit unit = productUnitRepository.findBySerialNumberForUpdate(normalizeSerial(serialNumber))
                .orElseThrow(() -> new AppException(StatusCode.PRODUCT_UNIT_NOT_FOUND));

        if (unit.getStatus() != ProductUnitStatus.SOLD) {
            throw new AppException(StatusCode.PRODUCT_UNIT_NOT_SOLD);
        }

        Order order = unit.getOrder();
        if (order == null || !order.getId().equals(orderId)) {
            throw new AppException(StatusCode.ORDER_SERIAL_MISMATCH);
        }

        LocalDateTime orderDate = order.getOrderDate() == null
                ? null
                : LocalDateTime.ofInstant(order.getOrderDate().toInstant(), ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();
        if (orderDate == null || now.isAfter(orderDate.plusDays(7))) {
            throw new AppException(StatusCode.RETURN_WINDOW_EXPIRED);
        }

        unit.setStatus(ProductUnitStatus.RETURNED);
        unit.setReturnedAt(now);
        productUnitRepository.save(unit);

        return ReturnProductUnitResponse.builder()
                .serialNumber(unit.getSerialNumber())
                .orderId(order.getId())
                .variantId(unit.getVariant().getId())
                .unitStatus(unit.getStatus().name())
                .returnedAt(unit.getReturnedAt())
                .message("Return request accepted")
                .build();
    }

    @Override
    @Transactional
    public void reserveUnits(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(StatusCode.ORDER_NOT_FOUND));

        List<OrderDetail> orderDetails = order.getOrderDetails();
        if (orderDetails == null || orderDetails.isEmpty()) {
            throw new AppException(StatusCode.CART_ITEMS_EMPTY);
        }

        List<ProductUnit> unitsToSave = new ArrayList<>();

        for (OrderDetail orderDetail : orderDetails) {
            Variant variant = variantRepository.findByIdForUpdate(orderDetail.getVariant().getId())
                    .orElseThrow(() -> new AppException(StatusCode.VARIANT_NOT_FOUND));

            int quantity = orderDetail.getNumberOfProducts() == null ? 0 : orderDetail.getNumberOfProducts();
            if (quantity <= 0) {
                continue;
            }

            List<ProductUnit> availableUnits = productUnitRepository.findAvailableUnitsForUpdate(
                    variant.getId(),
                    ProductUnitStatus.IN_STOCK,
                    PageRequest.of(0, quantity)
            );
            if (availableUnits.size() < quantity) {
                throw new AppException(StatusCode.PRODUCT_UNIT_OUT_OF_STOCK);
            }

            for (ProductUnit unit : availableUnits) {
                unit.setStatus(ProductUnitStatus.RESERVED);
                unit.setOrder(order);
                unit.setSoldAt(null);
                unit.setReturnedAt(null);
                unit.setWarrantyExpiredAt(null);
                unitsToSave.add(unit);
            }
        }

        productUnitRepository.saveAll(unitsToSave);
    }

    @Override
    @Transactional
    public void confirmUnitsSold(Long orderId) {
        List<ProductUnit> reservedUnits = productUnitRepository.findByOrderIdAndStatusForUpdate(
                orderId, ProductUnitStatus.RESERVED
        );
        if (reservedUnits.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warrantyExpiredAt = now.plusMonths(DEFAULT_WARRANTY_MONTHS);
        for (ProductUnit unit : reservedUnits) {
            unit.setStatus(ProductUnitStatus.SOLD);
            unit.setSoldAt(now);
            unit.setWarrantyExpiredAt(warrantyExpiredAt);
        }
        productUnitRepository.saveAll(reservedUnits);
    }

    @Override
    @Transactional
    public void releaseUnits(Long orderId) {
        List<ProductUnit> reservedUnits = productUnitRepository.findByOrderIdAndStatusForUpdate(
                orderId, ProductUnitStatus.RESERVED
        );
        if (reservedUnits.isEmpty()) {
            return;
        }

        for (ProductUnit unit : reservedUnits) {
            unit.setStatus(ProductUnitStatus.IN_STOCK);
            unit.setOrder(null);
            unit.setSoldAt(null);
            unit.setReturnedAt(null);
            unit.setWarrantyExpiredAt(null);
        }
        productUnitRepository.saveAll(reservedUnits);
    }

    private ProductUnit findUnitBySerial(String serialNumber) {
        return productUnitRepository.findBySerialNumber(normalizeSerial(serialNumber))
                .orElseThrow(() -> new AppException(StatusCode.PRODUCT_UNIT_NOT_FOUND));
    }

    private void validateVariantProduct(ProductUnit unit) {
        if (unit.getVariant() == null || unit.getVariant().getProduct() == null) {
            throw new AppException(StatusCode.PRODUCT_NOT_FOUND);
        }
    }

    private String normalizeSerial(String serialNumber) {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new AppException(StatusCode.INVALID_REQUEST);
        }
        return serialNumber.trim();
    }

    private String generateUniqueSerialNumber() {
        for (int attempt = 0; attempt < 20; attempt++) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1_000_000, 10_000_000);
            String serialNumber = "SN-" + randomNumber;
            if (!productUnitRepository.existsBySerialNumber(serialNumber)) {
                return serialNumber;
            }
        }
        throw new AppException(StatusCode.INTERNAL_SERVER_ERROR, "Unable to generate unique serial number");
    }
}
