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
import com.buixuantruong.shopapp.repository.ProductUnitRepository;
import com.buixuantruong.shopapp.model.ProductUnitStatus;
import com.buixuantruong.shopapp.service.ProductUnitService;
import com.buixuantruong.shopapp.service.VariantService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VariantServiceImpl implements VariantService {
    VariantRepository variantRepository;
    ProductRepository productRepository;
    VariantMapper variantMapper;
    SpecificationMapper specificationMapper;
    Cloudinary cloudinary;
    ProductUnitService productUnitService;
    ProductUnitRepository productUnitRepository;

    @Value("${cloudinary.folder:shopapp}")
    @NonFinal
    String cloudinaryFolder;

    @Override
    @Transactional
    public VariantResponse createVariant(VariantDTO variantDTO, MultipartFile file) {
        validateVariantPayload(variantDTO);
        if (variantRepository.existsBySku(variantDTO.getSku())) {
            throw new AppException(StatusCode.INVALID_DATA);
        }

        Product product = findProduct(variantDTO.getProductId());
        Variant variant = variantMapper.toVariant(variantDTO);
        variant.setProduct(product);
        variant.setImageUrl(uploadVariantFileIfPresent(resolveUploadedFile(variantDTO, file)));
        applyDefaultValues(variant);
        variant.setSpecification(buildSpecification(variant, variantDTO.getSpecification()));
        Variant savedVariant = variantRepository.save(variant);
        long initialQuantity = savedVariant.getStock() == null ? 0L : savedVariant.getStock();
        productUnitService.generateUnits(savedVariant.getId(), initialQuantity);
        return variantMapper.toResponse(savedVariant);
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
    public VariantResponse updateVariant(Long id, VariantDTO variantDTO, MultipartFile file) {
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
        MultipartFile uploadedFile = resolveUploadedFile(variantDTO, file);
        if (uploadedFile != null && !uploadedFile.isEmpty()) {
            existingVariant.setImageUrl(uploadVariantFile(uploadedFile));
        }
        applyDefaultValues(existingVariant);
        mergeSpecification(existingVariant, variantDTO.getSpecification());

        return variantMapper.toResponse(variantRepository.save(existingVariant));
    }

    @Override
    @Transactional
    public VariantResponse updateStock(Long id, Long newStock) {
        if (newStock == null || newStock < 0) {
            throw new AppException(StatusCode.INVALID_QUANTITY);
        }

        Variant variant = findVariant(id);
        long currentAvailableUnits = productUnitRepository.countByVariantIdAndStatusAndOrderIsNull(
                variant.getId(),
                ProductUnitStatus.IN_STOCK
        );

        if (newStock > currentAvailableUnits) {
            productUnitService.generateUnits(variant.getId(), newStock - currentAvailableUnits);
            currentAvailableUnits = newStock;
        }

        // Never delete physical units when requested stock is lower than current available units.
        variant.setStock(currentAvailableUnits);
        return variantMapper.toResponse(variantRepository.save(variant));
    }

    @Override
    @Transactional
    public VariantResponse updatePrice(Long id, BigDecimal price, BigDecimal discountPrice) {
        if (price == null || price.signum() < 0) {
            throw new AppException(StatusCode.INVALID_DATA);
        }
        if (discountPrice != null && discountPrice.signum() < 0) {
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
            variant.setPrice(BigDecimal.ZERO);
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

    private String uploadVariantFileIfPresent(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return uploadVariantFile(file);
    }

    private MultipartFile resolveUploadedFile(VariantDTO variantDTO, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            return file;
        }
        if (variantDTO == null) {
            return null;
        }
        MultipartFile dtoFile = variantDTO.getImageUrl();
        return (dtoFile == null || dtoFile.isEmpty()) ? null : dtoFile;
    }

    private String uploadVariantFile(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", cloudinaryFolder + "/variants",
                            "resource_type", "image"
                    )
            );
            return String.valueOf(uploadResult.get("secure_url"));
        } catch (Exception exception) {
            throw new AppException(
                    StatusCode.INVALID_REQUEST,
                    "Cloudinary upload failed: " + exception.getMessage()
            );
        }
    }

}
