package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.SpecificationDTO;
import com.buixuantruong.shopapp.dto.VariantDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.VariantResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.mapper.SpecificationMapper;
import com.buixuantruong.shopapp.mapper.VariantMapper;
import com.buixuantruong.shopapp.model.Product;
import com.buixuantruong.shopapp.model.Specification;
import com.buixuantruong.shopapp.model.Variant;
import com.buixuantruong.shopapp.repository.ProductRepository;
import com.buixuantruong.shopapp.repository.VariantRepository;
import com.buixuantruong.shopapp.service.VariantService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VariantServiceImpl implements VariantService {
    VariantRepository variantRepository;
    ProductRepository productRepository;
    VariantMapper variantMapper;
    SpecificationMapper specificationMapper;

    @Override
    @Transactional
    public VariantResponse createVariant(VariantDTO variantDTO) {
        validateVariantPayload(variantDTO);
        if (variantRepository.existsBySku(variantDTO.getSku())) {
            throw new AppException(StatusCode.INVALID_DATA);
        }

        Product product = findProduct(variantDTO.getProductId());
        Variant variant = variantMapper.toVariant(variantDTO);
        variant.setProduct(product);
        applyDefaultValues(variant);
        variant.setSpecification(buildSpecification(variant, variantDTO.getSpecification()));

        return variantMapper.toResponse(variantRepository.save(variant));
    }

    @Override
    public VariantResponse getVariantById(Long id) {
        return variantMapper.toResponse(findVariant(id));
    }

    @Override
    public List<VariantResponse> getAllVariants() {
        return variantRepository.findAll().stream()
                .map(variantMapper::toResponse)
                .toList();
    }

    @Override
    public List<VariantResponse> getVariantsByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new AppException(StatusCode.PRODUCT_NOT_FOUND);
        }
        return variantRepository.findByProductId(productId).stream()
                .map(variantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public VariantResponse updateVariant(Long id, VariantDTO variantDTO) {
        validateVariantPayload(variantDTO);

        Variant existingVariant = findVariant(id);
        if (variantDTO.getSku() != null
                && !existingVariant.getSku().equalsIgnoreCase(variantDTO.getSku())
                && variantRepository.existsBySku(variantDTO.getSku())) {
            throw new AppException(StatusCode.INVALID_DATA);
        }

        Product product = findProduct(variantDTO.getProductId());
        variantMapper.updateVariantFromDto(variantDTO, existingVariant);
        existingVariant.setProduct(product);
        applyDefaultValues(existingVariant);
        mergeSpecification(existingVariant, variantDTO.getSpecification());

        return variantMapper.toResponse(variantRepository.save(existingVariant));
    }

    @Override
    @Transactional
    public VariantResponse updateStock(Long id, Long purchasedQuantity) {
        if (purchasedQuantity == null || purchasedQuantity <= 0) {
            throw new AppException(StatusCode.INVALID_QUANTITY);
        }

        Variant variant = findVariant(id);
        long currentStock = variant.getStock() == null ? 0L : variant.getStock();
        if (currentStock < purchasedQuantity) {
            throw new AppException(StatusCode.INVALID_QUANTITY);
        }

        variant.setStock(currentStock - purchasedQuantity);
        return variantMapper.toResponse(variantRepository.save(variant));
    }

    @Override
    @Transactional
    public VariantResponse updatePrice(Long id, Float price, Float discountPrice) {
        if (price == null || price < 0) {
            throw new AppException(StatusCode.INVALID_DATA);
        }
        if (discountPrice != null && discountPrice < 0) {
            throw new AppException(StatusCode.INVALID_DATA);
        }

        Variant variant = findVariant(id);
        variant.setPrice(price);
        variant.setDiscountPrice(discountPrice);
        return variantMapper.toResponse(variantRepository.save(variant));
    }

    @Override
    @Transactional
    public MessageResponse deleteVariant(Long id) {
        Variant variant = findVariant(id);
        variantRepository.delete(variant);
        return MessageResponse.builder().message("Variant deleted successfully").build();
    }

    private void validateVariantPayload(VariantDTO variantDTO) {
        if (variantDTO == null
                || variantDTO.getProductId() == null
                || variantDTO.getSku() == null
                || variantDTO.getSku().isBlank()) {
            throw new AppException(StatusCode.INVALID_DATA);
        }
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new AppException(StatusCode.PRODUCT_NOT_FOUND));
    }

    private Variant findVariant(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.VARIANT_NOT_FOUND));
    }

    private void applyDefaultValues(Variant variant) {
        if (variant.getPrice() == null) {
            variant.setPrice(0F);
        }
        if (variant.getStock() == null) {
            variant.setStock(0L);
        }
        if (variant.getIsActive() == null) {
            variant.setIsActive(Boolean.TRUE);
        }
    }

    private Specification buildSpecification(Variant variant, SpecificationDTO specificationDTO) {
        Specification specification = specificationMapper.toSpecification(
                specificationDTO != null ? specificationDTO : new SpecificationDTO()
        );
        specification.setVariant(variant);
        if (specification.getWeight() == null) {
            specification.setWeight(variant.getWeight());
        }
        return specification;
    }

    private void mergeSpecification(Variant variant, SpecificationDTO specificationDTO) {
        Specification specification = variant.getSpecification();
        if (specification == null) {
            variant.setSpecification(buildSpecification(variant, specificationDTO));
            return;
        }

        if (specificationDTO != null) {
            specificationMapper.updateSpecificationFromDto(specificationDTO, specification);
        }
        specification.setVariant(variant);
        if (specification.getWeight() == null) {
            specification.setWeight(variant.getWeight());
        }
    }
}
