package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.ReviewDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.ReviewResponse;
import com.buixuantruong.shopapp.exception.DataNotFoundException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Product;
import com.buixuantruong.shopapp.model.Review;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.repository.ProductRepository;
import com.buixuantruong.shopapp.repository.ReviewRepository;
import com.buixuantruong.shopapp.repository.UserRepository;
import com.buixuantruong.shopapp.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {

    ReviewRepository reviewRepository;
    UserRepository userRepository;
    ProductRepository productRepository;

    @Override
    @Transactional
    public ApiResponse<Object> createReview(ReviewDTO reviewDTO) throws DataNotFoundException {
        User user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + reviewDTO.getUserId()));

        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + reviewDTO.getProductId()));

        // Kiểm tra xem user đã review sản phẩm này chưa
        Optional<Review> existingReview = reviewRepository.findByUser_IdAndProduct_Id(
                reviewDTO.getUserId(), reviewDTO.getProductId());
        if (existingReview.isPresent()) {
            return ApiResponse.builder()
                    .code(StatusCode.INVALID_DATA.getCode())
                    .message("You have already reviewed this product")
                    .build();
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .comment(reviewDTO.getComment())
                .rating(reviewDTO.getRating())
                .build();

        Review savedReview = reviewRepository.save(review);

        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(ReviewResponse.fromReview(savedReview))
                .build();
    }

    @Override
    public ApiResponse<Object> getReviewsByProduct(Long productId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByProduct_Id(productId, pageable);
        List<ReviewResponse> reviewResponses = reviewPage.getContent().stream()
                .map(ReviewResponse::fromReview)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("reviews", reviewResponses);
        result.put("totalPages", reviewPage.getTotalPages());
        result.put("totalElements", reviewPage.getTotalElements());

        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(result)
                .build();
    }

    @Override
    public ApiResponse<Object> getReviewsByUser(Long userId) {
        List<Review> reviews = reviewRepository.findByUser_Id(userId);
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(ReviewResponse::fromReview)
                .toList();

        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(reviewResponses)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Object> updateReview(Long reviewId, ReviewDTO reviewDTO) throws DataNotFoundException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DataNotFoundException("Review not found with id: " + reviewId));

        // Chỉ cho phép sửa comment và rating
        if (reviewDTO.getComment() != null) {
            review.setComment(reviewDTO.getComment());
        }
        if (reviewDTO.getRating() != null) {
            review.setRating(reviewDTO.getRating());
        }

        Review updatedReview = reviewRepository.save(review);

        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(ReviewResponse.fromReview(updatedReview))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Object> deleteReview(Long reviewId) throws DataNotFoundException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DataNotFoundException("Review not found with id: " + reviewId));

        reviewRepository.delete(review);

        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result("Review deleted successfully")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Object> respondToReview(Long reviewId, String response) throws DataNotFoundException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DataNotFoundException("Review not found with id: " + reviewId));

        review.setAdminResponse(response);
        Review updatedReview = reviewRepository.save(review);

        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(ReviewResponse.fromReview(updatedReview))
                .build();
    }

    @Override
    public ApiResponse<Object> getProductRatingSummary(Long productId) {
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
        List<Object[]> ratingCounts = reviewRepository.countReviewsByRatingForProduct(productId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("averageRating", averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);

        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) ratingDistribution.put(i, 0L);
        for (Object[] row : ratingCounts) {
            Integer star = (Integer) row[0];
            Long count = (Long) row[1];
            ratingDistribution.put(star, count);
        }
        summary.put("ratingDistribution", ratingDistribution);

        long totalReviews = ratingDistribution.values().stream().mapToLong(Long::longValue).sum();
        summary.put("totalReviews", totalReviews);

        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(summary)
                .build();
    }
}
