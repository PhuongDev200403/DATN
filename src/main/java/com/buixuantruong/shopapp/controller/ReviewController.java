package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.ReviewDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.exception.DataNotFoundException;
import com.buixuantruong.shopapp.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

    ReviewService reviewService;

    /**
     * Tạo review mới cho sản phẩm.
     * Mỗi user chỉ được review mỗi sản phẩm 1 lần.
     * POST /api/v1/reviews
     */
    @PostMapping("")
    public ApiResponse<Object> createReview(@Valid @RequestBody ReviewDTO reviewDTO) throws DataNotFoundException {
        return reviewService.createReview(reviewDTO);
    }

    /**
     * Lấy danh sách review của một sản phẩm (có phân trang).
     * GET /api/v1/reviews/product/{productId}?page=0&limit=10
     */
    @GetMapping("/product/{productId}")
    public ApiResponse<Object> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("createAt").descending());
        return reviewService.getReviewsByProduct(productId, pageRequest);
    }

    /**
     * Lấy tổng hợp điểm đánh giá của sản phẩm (điểm TB, phân phối sao).
     * GET /api/v1/reviews/product/{productId}/summary
     */
    @GetMapping("/product/{productId}/summary")
    public ApiResponse<Object> getProductRatingSummary(@PathVariable Long productId) {
        return reviewService.getProductRatingSummary(productId);
    }

    /**
     * Lấy danh sách review của một user.
     * GET /api/v1/reviews/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<Object> getReviewsByUser(@PathVariable Long userId) {
        return reviewService.getReviewsByUser(userId);
    }

    /**
     * Cập nhật review (user chỉ sửa được review của mình).
     * PUT /api/v1/reviews/{reviewId}
     */
    @PutMapping("/{reviewId}")
    public ApiResponse<Object> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewDTO reviewDTO) throws DataNotFoundException {
        return reviewService.updateReview(reviewId, reviewDTO);
    }

    /**
     * Xóa review.
     * DELETE /api/v1/reviews/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Object> deleteReview(@PathVariable Long reviewId) throws DataNotFoundException {
        return reviewService.deleteReview(reviewId);
    }

    /**
     * Admin phản hồi lại review của khách hàng.
     * Chỉ ADMIN mới có quyền gọi API này.
     * POST /api/v1/reviews/{reviewId}/respond
     * Body: { "response": "Cảm ơn bạn đã phản hồi..." }
     */
    @PostMapping("/{reviewId}/respond")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> respondToReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> body) throws DataNotFoundException {
        String response = body.get("response");
        return reviewService.respondToReview(reviewId, response);
    }
}
