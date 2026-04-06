package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.ReviewDTO;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.ReviewPageResponse;
import com.buixuantruong.shopapp.dto.response.ReviewRatingSummaryResponse;
import com.buixuantruong.shopapp.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ReviewService {
    ReviewResponse createReview(ReviewDTO reviewDTO);

    ReviewPageResponse getReviewsByProduct(Long productId, Pageable pageable);

    List<ReviewResponse> getReviewsByUser(Long userId);

    ReviewResponse updateReview(Long reviewId, ReviewDTO reviewDTO);

    MessageResponse deleteReview(Long reviewId);

    ReviewResponse respondToReview(Long reviewId, String response);

    ReviewRatingSummaryResponse getProductRatingSummary(Long productId);
}
