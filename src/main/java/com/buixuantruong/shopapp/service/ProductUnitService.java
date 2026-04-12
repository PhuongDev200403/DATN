package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.response.ReturnProductUnitResponse;
import com.buixuantruong.shopapp.dto.response.WarrantyCheckResponse;

public interface ProductUnitService {
    WarrantyCheckResponse checkWarranty(String serialNumber);

    ReturnProductUnitResponse returnBySerial(Long orderId, String serialNumber);

    void generateUnits(Long variantId, long quantity);

    void reserveUnits(Long orderId);

    void confirmUnitsSold(Long orderId);

    void releaseUnits(Long orderId);
}
