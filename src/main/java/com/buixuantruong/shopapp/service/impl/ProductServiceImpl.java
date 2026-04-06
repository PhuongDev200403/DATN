package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.ProductDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.ProductResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.mapper.ProductMapper;
import com.buixuantruong.shopapp.model.Category;
import com.buixuantruong.shopapp.model.Product;
import com.buixuantruong.shopapp.model.Variant;
import com.buixuantruong.shopapp.repository.CategoryRepository;
import com.buixuantruong.shopapp.repository.OrderDetailRepository;
import com.buixuantruong.shopapp.repository.ProductRepository;
import com.buixuantruong.shopapp.service.AiTextService;
import com.buixuantruong.shopapp.service.ProductService;
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

import java.time.LocalDateTime;
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
    ProductMapper productMapper;

    @Override
    @CacheEvict(value = "product_searches", allEntries = true)
    @Transactional
    public ProductResponse createProduct(ProductDTO productDTO) {
        if (productRepository.existsByName(productDTO.getName())) {
            throw new AppException(StatusCode.PRODUCT_EXISTED);
        }

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new AppException(StatusCode.CATEGORY_NOT_FOUND));

        Product product = productMapper.toProduct(productDTO);
        product.setCategory(category);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.PRODUCT_NOT_FOUND));
        return productMapper.toResponse(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(PageRequest pageRequest) {
        return productRepository.findAll(pageRequest)
                .map(productMapper::toResponse);
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(Long categoryId, PageRequest pageRequest) {
        return productRepository.findByCategoryId(categoryId, pageRequest)
                .map(productMapper::toResponse);
    }

    @Override
    public Page<ProductResponse> getNewProducts(int months, PageRequest pageRequest) {
        int effectiveMonths = months <= 0 ? 6 : months;
        LocalDateTime fromDate = LocalDateTime.now().minusMonths(effectiveMonths);
        return productRepository.findNewProducts(fromDate, pageRequest)
                .map(productMapper::toResponse);
    }

    @Override
    @Cacheable(value = "product_searches", key = "T(java.util.Objects).hash(#categoryId, #keyword, #minPrice, #maxPrice, #pageRequest.pageNumber, #pageRequest.pageSize)")
    public Page<ProductResponse> searchProducts(Long categoryId, String keyword, Float minPrice, Float maxPrice, PageRequest pageRequest) {
        return productRepository.searchProducts(categoryId, keyword, minPrice, maxPrice, pageRequest)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "product_searches", allEntries = true)
    })
    public ProductResponse updateProduct(long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.PRODUCT_NOT_FOUND));
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new AppException(StatusCode.CATEGORY_NOT_FOUND));

        boolean duplicatedName = productDTO.getName() != null
                && !existingProduct.getName().equalsIgnoreCase(productDTO.getName())
                && productRepository.existsByName(productDTO.getName());
        if (duplicatedName) {
            throw new AppException(StatusCode.PRODUCT_EXISTED);
        }

        productMapper.updateProductFromDto(productDTO, existingProduct);
        existingProduct.setCategory(category);
        return productMapper.toResponse(productRepository.save(existingProduct));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "product_searches", allEntries = true)
    })
    public MessageResponse deleteProduct(long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        optionalProduct.ifPresent(productRepository::delete);
        return MessageResponse.builder().message("Product deleted successfully").build();
    }

    @Override
    public boolean existsProduct(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    public List<ProductResponse> findProductByIds(List<Long> productIds) {
        return productRepository.findProductByIds(productIds).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> getSimilarProducts(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(StatusCode.PRODUCT_NOT_FOUND));

        Float basePrice = extractBasePrice(product);
        Float minPrice = null;
        Float maxPrice = null;
        if (basePrice != null && basePrice > 0) {
            minPrice = basePrice * 0.8f;
            maxPrice = basePrice * 1.2f;
        }

        return productRepository.findSimilarCandidates(product.getId(), product.getCategory().getId(), minPrice, maxPrice)
                .stream()
                .map(candidate -> new java.util.AbstractMap.SimpleEntry<>(candidate, calculateSimilarityScore(product, candidate)))
                .sorted((left, right) -> Double.compare(right.getValue(), left.getValue()))
                .limit(5)
                .map(java.util.Map.Entry::getKey)
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> getFrequentlyBoughtTogetherProducts(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new AppException(StatusCode.PRODUCT_NOT_FOUND));

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
        return aiTextService.getRecommendations(query, productRepository.findAll());
    }

    @Override
    public String generateProductDescription(String productName, Long categoryId, String specs) throws Exception {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(StatusCode.CATEGORY_NOT_FOUND));
        return aiTextService.generateProductDescription(productName, category.getName(), specs);
    }

    @Override
    @Transactional
    public void deleteFakeProducts() {
        List<Product> fakeProducts = productRepository.findByVariantImageContaining("picsum.photos");
        productRepository.deleteAll(fakeProducts);
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
                .map(productMapper::toResponse)
                .toList();
    }

    private Float extractBasePrice(Product product) {
        return product.getVariants() == null ? null : product.getVariants().stream()
                .map(Variant::getPrice)
                .filter(price -> price != null)
                .min(Float::compareTo)
                .orElse(null);
    }

    private double calculateSimilarityScore(Product baseProduct, Product candidate) {
        double categoryScore = 5.0;
        Float basePrice = extractBasePrice(baseProduct);
        Float candidatePrice = extractBasePrice(candidate);
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
