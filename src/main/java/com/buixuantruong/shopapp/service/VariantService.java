package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.VariantDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.VariantResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface VariantService {

    VariantResponse createVariant(VariantDTO variantDTO, MultipartFile file);

    VariantResponse getVariantById(Long id);

    List<VariantResponse> getAllVariants();

    List<VariantResponse> getVariantsByProductId(Long productId);

    VariantResponse updateVariant(Long id, VariantDTO variantDTO, MultipartFile file);

    VariantResponse updateStock(Long id, Long newStock);

    VariantResponse updatePrice(Long id, BigDecimal price, BigDecimal discountPrice);

    MessageResponse deleteVariant(Long id);
}
