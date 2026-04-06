package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.ReviewDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.ReviewPageResponse;
import com.buixuantruong.shopapp.dto.response.ReviewRatingSummaryResponse;
import com.buixuantruong.shopapp.dto.response.ReviewResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.mapper.ReviewMapper;
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
    ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewDTO reviewDTO) {
        User user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));
        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new AppException(StatusCode.PRODUCT_NOT_FOUND));

        Optional<Review> existingReview = reviewRepository.findByUser_IdAndProduct_Id(
                reviewDTO.getUserId(), reviewDTO.getProductId());
        if (existingReview.isPresent()) {
            throw new AppException(StatusCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .comment(reviewDTO.getComment())
                .rating(reviewDTO.getRating())
                .build();

        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    public ReviewPageResponse getReviewsByProduct(Long productId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByProduct_Id(productId, pageable);
        return ReviewPageResponse.builder()
                .reviews(reviewPage.getContent().stream().map(reviewMapper::toResponse).toList())
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .build();
    }

    @Override
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        return reviewRepository.findByUser_Id(userId).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewDTO reviewDTO) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(StatusCode.REVIEW_NOT_FOUND));

        if (reviewDTO.getComment() != null) {
            review.setComment(reviewDTO.getComment());
        }
        if (reviewDTO.getRating() != null) {
            review.setRating(reviewDTO.getRating());
        }

        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public MessageResponse deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(StatusCode.REVIEW_NOT_FOUND));
        reviewRepository.delete(review);
        return MessageResponse.builder().message("Review deleted successfully").build();
    }

    @Override
    @Transactional
    public ReviewResponse respondToReview(Long reviewId, String response) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(StatusCode.REVIEW_NOT_FOUND));
        review.setAdminResponse(response);
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    public ReviewRatingSummaryResponse getProductRatingSummary(Long productId) {
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
        List<Object[]> ratingCounts = reviewRepository.countReviewsByRatingForProduct(productId);

        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        for (Object[] row : ratingCounts) {
            ratingDistribution.put((Integer) row[0], (Long) row[1]);
        }

        long totalReviews = ratingDistribution.values().stream().mapToLong(Long::longValue).sum();
        return ReviewRatingSummaryResponse.builder()
                .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0)
                .ratingDistribution(ratingDistribution)
                .totalReviews(totalReviews)
                .build();
    }
}
