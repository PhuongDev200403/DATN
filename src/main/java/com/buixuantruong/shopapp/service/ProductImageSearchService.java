package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.response.ProductResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageSearchService {
    private final ChatClient chatClient;
    private final ProductService productService;

    public ProductImageSearchService(ChatClient.Builder builder, ProductService productService) {
        this.chatClient = builder.build();
        this.productService = productService;
    }

    public Page<ProductResponse> searchByImage(
            MultipartFile file,
            Long categoryId,
            Float minPrice,
            Float maxPrice,
            PageRequest pageRequest
    ) {
        if (file == null || file.isEmpty()) {
            throw new AppException(StatusCode.FILE_EMPTY);
        }

        String keyword = extractSearchKeyword(file);
        if (keyword.isBlank()) {
            throw new AppException(StatusCode.INVALID_DATA, "Could not extract a product keyword from the image");
        }
        return productService.searchProducts(categoryId, keyword, minPrice, maxPrice, pageRequest);
    }

    private String extractSearchKeyword(MultipartFile file) {
        Media media = Media.builder()
                .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                .data(file.getResource())
                .build();

        String response = chatClient.prompt()
                .system("""
                        You extract product search keywords from an image.
                        Return only a short search query.
                        Include product type, model, color, capacity, or other visible identifying details.
                        Do not explain anything.
                        """)
                .user(prompt -> prompt.media(media).text("Extract the best product search keywords from this image."))
                .call()
                .content();

        return normalizeKeyword(response);
    }

    private String normalizeKeyword(String response) {
        if (response == null) {
            return "";
        }

        String cleaned = response.trim();
        int newlineIndex = cleaned.indexOf('\n');
        if (newlineIndex >= 0) {
            cleaned = cleaned.substring(0, newlineIndex);
        }

        cleaned = cleaned.replaceAll("^[\\-\\*\\s]+", "");
        cleaned = cleaned.replaceAll("(?i)^(keyword|search query|query|product|san pham)\\s*[:\\-]\\s*", "");
        cleaned = cleaned.replaceAll("[\\p{Punct}]+$", "");
        return cleaned.trim();
    }
}
