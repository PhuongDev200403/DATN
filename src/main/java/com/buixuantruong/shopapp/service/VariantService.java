package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.VariantDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.VariantResponse;

import java.util.List;

public interface VariantService {

    VariantResponse createVariant(VariantDTO variantDTO);

    VariantResponse getVariantById(Long id);

    List<VariantResponse> getAllVariants();

    List<VariantResponse> getVariantsByProductId(Long productId);

    VariantResponse updateVariant(Long id, VariantDTO variantDTO);

    VariantResponse updateStock(Long id, Long purchasedQuantity);

    VariantResponse updatePrice(Long id, Float price, Float discountPrice);

    MessageResponse deleteVariant(Long id);
}
