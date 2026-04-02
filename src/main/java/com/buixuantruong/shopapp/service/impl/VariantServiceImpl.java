package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.ProductDTO;
import com.buixuantruong.shopapp.dto.SpecificationDTO;
import com.buixuantruong.shopapp.dto.VariantDTO;
import com.buixuantruong.shopapp.model.Product;
import com.buixuantruong.shopapp.model.Specification;
import com.buixuantruong.shopapp.model.Variant;
import com.buixuantruong.shopapp.service.VariantService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VariantServiceImpl implements VariantService {

    @Override
    public void synchronizeVariants(Product product, ProductDTO productDTO) {
        List<VariantDTO> variantDTOs = productDTO.getVariants();
        if (variantDTOs == null || variantDTOs.isEmpty()) {
            variantDTOs = List.of(buildDefaultVariant(productDTO));
        }

        product.getVariants().clear();

        List<Variant> variants = variantDTOs.stream()
                .map(variantDTO -> toVariant(product, variantDTO))
                .toList();

        product.getVariants().addAll(variants);
        syncProductSummary(product, variants, productDTO);
    }

    private VariantDTO buildDefaultVariant(ProductDTO productDTO) {
        return VariantDTO.builder()
                .sku("SKU-" + System.currentTimeMillis())
                .price(productDTO.getPrice())
                .stock(productDTO.getQuantity() != null ? productDTO.getQuantity() : 0L)
                .imageUrl(productDTO.getThumbnail())
                .isActive(true)
                .weight(productDTO.getWeight())
                .specification(SpecificationDTO.builder()
                        .weight(productDTO.getWeight())
                        .length(productDTO.getLength())
                        .width(productDTO.getWidth())
                        .height(productDTO.getHeight())
                        .build())
                .build();
    }

    private Variant toVariant(Product product, VariantDTO variantDTO) {
        String sku = variantDTO.getSku();
        if (sku == null || sku.isBlank()) {
            sku = "SKU-" + System.nanoTime();
        }

        Variant variant = Variant.builder()
                .id(variantDTO.getId())
                .product(product)
                .sku(sku)
                .price(variantDTO.getPrice() != null ? variantDTO.getPrice() : 0F)
                .discountPrice(variantDTO.getDiscountPrice())
                .stock(variantDTO.getStock() != null ? variantDTO.getStock() : 0L)
                .color(variantDTO.getColor())
                .storage(variantDTO.getStorage())
                .ram(variantDTO.getRam())
                .imageUrl(variantDTO.getImageUrl())
                .isActive(variantDTO.getIsActive() != null ? variantDTO.getIsActive() : true)
                .barcode(variantDTO.getBarcode())
                .weight(variantDTO.getWeight())
                .build();

        Specification specification = toSpecification(variant, variantDTO.getSpecification(), variantDTO.getWeight());
        variant.setSpecification(specification);
        return variant;
    }

    private Specification toSpecification(Variant variant, SpecificationDTO specificationDTO, Integer fallbackWeight) {
        SpecificationDTO spec = specificationDTO != null ? specificationDTO : SpecificationDTO.builder().build();
        return Specification.builder()
                .variant(variant)
                .specName(spec.getSpecName())
                .specValue(spec.getSpecValue())
                .groupName(spec.getGroupName())
                .displayOrder(spec.getDisplayOrder())
                .weight(spec.getWeight() != null ? spec.getWeight() : fallbackWeight)
                .length(spec.getLength())
                .width(spec.getWidth())
                .height(spec.getHeight())
                .build();
    }

    private void syncProductSummary(Product product, List<Variant> variants, ProductDTO productDTO) {
        float minPrice = variants.stream()
                .map(Variant::getPrice)
                .filter(price -> price != null)
                .min(Float::compareTo)
                .orElse(productDTO.getPrice());
        long totalStock = variants.stream()
                .map(Variant::getStock)
                .filter(stock -> stock != null)
                .mapToLong(Long::longValue)
                .sum();
        String thumbnail = variants.stream()
                .map(Variant::getImageUrl)
                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                .findFirst()
                .orElse(productDTO.getThumbnail());

        product.setPrice(minPrice);
        product.setQuantity(totalStock);
        product.setThumbnail(thumbnail);
    }
}
