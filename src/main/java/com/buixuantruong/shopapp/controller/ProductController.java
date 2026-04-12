package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.ProductDTO;
import com.buixuantruong.shopapp.dto.response.*;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.ProductService;
import com.buixuantruong.shopapp.service.impl.ChatServiceImpl;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;
    ChatServiceImpl chatService;



    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductDTO productDTO) throws Exception {
        return ApiResponse.<ProductResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.createProduct(productDTO))
                .build();
    }

    @GetMapping("/ai-assistant")
    public ResponseEntity<ApiResponse<String>> aiAssistant(@RequestParam String query) throws Exception {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getAiAssistantResponse(query))
                .build());
    }

    @PostMapping("/generate-description")
    public ResponseEntity<ApiResponse<String>> generateDescription(@RequestParam String productName, @RequestParam Long categoryId, @RequestParam(required = false) String specs) throws Exception {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.generateProductDescription(productName, categoryId, specs))
                .build());
    }

    @GetMapping("")
    public ApiResponse<ProductListResponse> getProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "min_price", required = false) Float minPrice,
            @RequestParam(value = "max_price", required = false) Float maxPrice,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        Sort sortObj = direction.equalsIgnoreCase("desc") ? Sort.by(sort).descending() : Sort.by(sort).ascending();
        PageRequest pageRequest = PageRequest.of(page, limit, sortObj);
        Page<ProductResponse> products = productService.searchProducts(categoryId, keyword, minPrice, maxPrice, pageRequest);
        return ApiResponse.<ProductListResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(new ProductListResponse(products.getContent(), products.getTotalPages()))
                .build();
    }

    @GetMapping("/new")
    public ApiResponse<ProductListResponse> getNewProducts(
            @RequestParam(value = "months", defaultValue = "6") int months,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "sort", defaultValue = "createAt") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {
        Sort sortObj = direction.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();
        PageRequest pageRequest = PageRequest.of(page, limit, sortObj);
        Page<ProductResponse> products = productService.getNewProducts(months, pageRequest);
        return ApiResponse.<ProductListResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(new ProductListResponse(products.getContent(), products.getTotalPages()))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable("id") Long id) throws Exception {
        return ApiResponse.<ProductResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getProductById(id))
                .build();
    }

    @GetMapping("/{id}/similar")
    public ApiResponse<List<ProductResponse>> getSimilarProducts(@PathVariable("id") Long id) throws Exception {
        return ApiResponse.<List<ProductResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getSimilarProducts(id))
                .build();
    }

    @GetMapping("/{id}/frequently-bought-together")
    public ApiResponse<List<ProductResponse>> getFrequentlyBoughtTogetherProducts(@PathVariable("id") Long id) throws Exception {
        return ApiResponse.<List<ProductResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getFrequentlyBoughtTogetherProducts(id))
                .build();
    }

    @GetMapping("/top-selling")
    public ApiResponse<List<ProductResponse>> getTopSellingProducts() {
        return ApiResponse.<List<ProductResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getTopSellingProducts())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<MessageResponse> deleteProduct(@PathVariable("id") Long id) {
        return ApiResponse.<MessageResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.deleteProduct(id))
                .build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) throws Exception {
        return ApiResponse.<ProductResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.updateProduct(id, productDTO))
                .build();
    }

    @GetMapping("/by-ids")
    public ApiResponse<List<ProductResponse>> getProductsByIds(@RequestParam("ids") String ids) {
        List<Long> productIds = Arrays.stream(ids.split(",")).map(Long::parseLong).toList();
        return ApiResponse.<List<ProductResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.findProductByIds(productIds))
                .build();
    }

    @DeleteMapping("/deleteFakeProducts")
    public ApiResponse<MessageResponse> deleteFakeProducts() {
        productService.deleteFakeProducts();
        return ApiResponse.<MessageResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(MessageResponse.builder().message("Deleted fake products successfully").build())
                .build();
    }

//    @PostMapping("/search")
//    public List<ListAndSearchVariantResponse> search(@RequestParam("keyword") String keyword){
//        return productService.searchProducts(null, keyword, null, null, null);
//    }
}
