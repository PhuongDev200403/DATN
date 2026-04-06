package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.ProductDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductDTO productDTO) throws Exception;

    ProductResponse getProductById(long id) throws Exception;

    Page<ProductResponse> getAllProducts(PageRequest pageRequest);

    Page<ProductResponse> getProductsByCategory(Long categoryId, PageRequest pageRequest);

    Page<ProductResponse> getNewProducts(int months, PageRequest pageRequest);

    Page<ProductResponse> searchProducts(Long categoryId, String keyword, Float minPrice, Float maxPrice, PageRequest pageRequest);

    ProductResponse updateProduct(long id, ProductDTO productDTO) throws Exception;

    MessageResponse deleteProduct(long id);

    boolean existsProduct(String name);

    List<ProductResponse> findProductByIds(List<Long> productIds);

    List<ProductResponse> getSimilarProducts(Long productId) throws Exception;

    List<ProductResponse> getFrequentlyBoughtTogetherProducts(Long productId) throws Exception;

    List<ProductResponse> getTopSellingProducts();

    String getAiAssistantResponse(String query) throws Exception;

    String generateProductDescription(String productName, Long categoryId, String specs) throws Exception;

    void deleteFakeProducts();
}
