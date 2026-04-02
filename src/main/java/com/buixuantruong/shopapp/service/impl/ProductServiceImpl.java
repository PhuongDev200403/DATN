package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.ProductDTO;
import com.buixuantruong.shopapp.dto.VariantDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.ProductResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.DataNotFoundException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Category;
import com.buixuantruong.shopapp.model.Product;
import com.buixuantruong.shopapp.repository.CategoryRepository;
import com.buixuantruong.shopapp.repository.OrderDetailRepository;
import com.buixuantruong.shopapp.repository.ProductRepository;
import com.buixuantruong.shopapp.service.AiTextService;
import com.buixuantruong.shopapp.service.CloudinaryService;
import com.buixuantruong.shopapp.service.ProductService;
import com.buixuantruong.shopapp.service.VariantService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    OrderDetailRepository orderDetailRepository;
    AiTextService aiTextService;
    VariantService variantService;
    CloudinaryService cloudinaryService;

    private String generateWarrantyCode() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = now.format(dateFormatter);

        String alphanumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            sb.append(alphanumeric.charAt(random.nextInt(alphanumeric.length())));
        }

        return "WC-" + datePart + "-" + sb;
    }

    @Override
    @CacheEvict(value = "product_searches", allEntries = true)
    @Transactional
    public ApiResponse<ProductResponse> createProduct(ProductDTO productDTO) throws DataNotFoundException {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Category not found"));
        normalizeProductImages(productDTO);

        String warrantyCode = generateWarrantyCode();
        while (productRepository.existsByWarrantyCode(warrantyCode)) {
            warrantyCode = generateWarrantyCode();
        }

        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .thumbnail(productDTO.getThumbnail())
                .category(category)
                .warrantyCode(warrantyCode)
                .variants(new ArrayList<>())
                .build();

        variantService.synchronizeVariants(product, productDTO);

        Product savedProduct = productRepository.save(product);
        return ApiResponse.<ProductResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(ProductResponse.from(savedProduct))
                .build();
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(long id) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find product with id = " + id));
        return ProductResponse.from(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(PageRequest pageRequest) {
        return productRepository.findAll(pageRequest).map(ProductResponse::from);
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(Long categoryId, PageRequest pageRequest) {
        return productRepository.findByCategoryId(categoryId, pageRequest).map(ProductResponse::from);
    }

    @Override
    @Cacheable(value = "product_searches", key = "T(java.util.Objects).hash(#categoryId, #keyword, #minPrice, #maxPrice, #pageRequest.pageNumber, #pageRequest.pageSize)")
    public Page<ProductResponse> searchProducts(Long categoryId, String keyword, Float minPrice, Float maxPrice, PageRequest pageRequest) {
        return productRepository.searchProducts(categoryId, keyword, minPrice, maxPrice, pageRequest).map(ProductResponse::from);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "product_searches", allEntries = true)
    })
    public ProductResponse updateProduct(long id, ProductDTO productDTO) throws Exception {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.INVALID_DATA));
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new AppException(StatusCode.INVALID_DATA));
        normalizeProductImages(productDTO);

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setThumbnail(productDTO.getThumbnail());
        existingProduct.setCategory(category);

        variantService.synchronizeVariants(existingProduct, productDTO);

        Product savedProduct = productRepository.save(existingProduct);
        return ProductResponse.from(savedProduct);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "product_searches", allEntries = true)
    })
    public ApiResponse<Object> deleteProduct(long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        optionalProduct.ifPresent(productRepository::delete);
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result("Product deleted successfully")
                .build();
    }

    @Override
    public boolean existsProduct(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    public List<Product> findProductByIds(List<Long> productIds) {
        return productRepository.findProductByIds(productIds);
    }

    @Override
    public List<ProductResponse> getSimilarProducts(Long productId) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found with id = " + productId));

        Float basePrice = product.getPrice();
        Float minPrice = null;
        Float maxPrice = null;
        if (basePrice != null && basePrice > 0) {
            minPrice = basePrice * 0.8f;
            maxPrice = basePrice * 1.2f;
        }

        List<Product> candidates = productRepository.findSimilarCandidates(
                product.getId(),
                product.getCategory().getId(),
                minPrice,
                maxPrice
        );

        return candidates.stream()
                .map(candidate -> new java.util.AbstractMap.SimpleEntry<>(candidate, calculateSimilarityScore(product, candidate)))
                .sorted((left, right) -> Double.compare(right.getValue(), left.getValue()))
                .limit(5)
                .map(java.util.Map.Entry::getKey)
                .map(ProductResponse::from)
                .toList();
    }

    @Override
    public List<ProductResponse> getFrequentlyBoughtTogetherProducts(Long productId) throws Exception {
        productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found with id = " + productId));

        List<Long> orderedIds = orderDetailRepository.findFrequentlyBoughtTogetherProductIds(productId).stream()
                .map(row -> ((Number) row[0]).longValue())
                .limit(5)
                .toList();

        return mapProductsByOrderedIds(orderedIds);
    }

    @Override
    public List<ProductResponse> getTopSellingProducts() {
        List<Long> orderedIds = orderDetailRepository.findTopSellingProductIds().stream()
                .map(row -> ((Number) row[0]).longValue())
                .limit(10)
                .toList();

        return mapProductsByOrderedIds(orderedIds);
    }

    @Override
    public String getAiAssistantResponse(String query) throws Exception {
        List<Product> allProducts = productRepository.findAll();
        return aiTextService.getRecommendations(query, allProducts);
    }

    @Override
    public String generateProductDescription(String productName, Long categoryId, String specs) throws Exception {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new DataNotFoundException("Category not found"));
        return aiTextService.generateProductDescription(productName, category.getName(), specs);
    }

    @Override
    @Transactional
    public void deleteFakeProducts() {
        List<Product> fakeProducts = productRepository.findByThumbnailContaining("picsum.photos");
        productRepository.deleteAll(fakeProducts);
    }

    private void normalizeProductImages(ProductDTO productDTO) {
        try {
            String originalThumbnail = productDTO.getThumbnail();
            String uploadedThumbnail = uploadIfPresent(originalThumbnail, "shopapp/products/thumbnails");
            productDTO.setThumbnail(uploadedThumbnail);

            if (productDTO.getVariants() == null || productDTO.getVariants().isEmpty()) {
                return;
            }

            for (VariantDTO variant : productDTO.getVariants()) {
                String variantImage = variant.getImageUrl();
                if (variantImage == null || variantImage.isBlank()) {
                    variant.setImageUrl(uploadedThumbnail);
                    continue;
                }
                if (originalThumbnail != null && originalThumbnail.equals(variantImage) && uploadedThumbnail != null) {
                    variant.setImageUrl(uploadedThumbnail);
                    continue;
                }
                variant.setImageUrl(uploadIfPresent(variantImage, "shopapp/products/variants"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Upload product image failed: " + e.getMessage(), e);
        }
    }

    private String uploadIfPresent(String imageSource, String folder) throws Exception {
        if (imageSource == null || imageSource.isBlank()) {
            return imageSource;
        }
        return cloudinaryService.uploadImage(imageSource, folder);
    }

    private List<ProductResponse> mapProductsByOrderedIds(List<Long> orderedIds) {
        if (orderedIds.isEmpty()) {
            return List.of();
        }

        java.util.Map<Long, Product> productsById = productRepository.findActiveProductsByIds(orderedIds).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, product -> product));

        return orderedIds.stream()
                .map(productsById::get)
                .filter(java.util.Objects::nonNull)
                .map(ProductResponse::from)
                .toList();
    }

    private double calculateSimilarityScore(Product baseProduct, Product candidate) {
        double categoryScore = 5.0;

        Float basePrice = baseProduct.getPrice();
        Float candidatePrice = candidate.getPrice();
        if (basePrice == null || candidatePrice == null || basePrice <= 0) {
            return categoryScore;
        }

        double maxAllowedDiff = basePrice * 0.2;
        double actualDiff = Math.abs(basePrice - candidatePrice);
        double similarityRatio = Math.max(0.0, 1.0 - (actualDiff / maxAllowedDiff));
        double priceScore = similarityRatio * 3.0;

        return categoryScore + priceScore;
    }
}
