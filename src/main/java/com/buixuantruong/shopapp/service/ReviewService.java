package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.ReviewDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.exception.DataNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface ReviewService {

    // Tạo review mới
    ApiResponse<Object> createReview(ReviewDTO reviewDTO) throws DataNotFoundException;

    // Lấy danh sách review của một sản phẩm (có phân trang)
    ApiResponse<Object> getReviewsByProduct(Long productId, Pageable pageable);

    // Lấy danh sách review của một user
    ApiResponse<Object> getReviewsByUser(Long userId);

    // Cập nhật review (chỉ user đã tạo mới được sửa)
    ApiResponse<Object> updateReview(Long reviewId, ReviewDTO reviewDTO) throws DataNotFoundException;

    // Xóa review
    ApiResponse<Object> deleteReview(Long reviewId) throws DataNotFoundException;

    // Admin phản hồi review
    ApiResponse<Object> respondToReview(Long reviewId, String response) throws DataNotFoundException;

    // Lấy thông tin tổng hợp điểm đánh giá của sản phẩm
    ApiResponse<Object> getProductRatingSummary(Long productId);
}
