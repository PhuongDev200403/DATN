package com.buixuantruong.shopapp.dto.response;

import com.buixuantruong.shopapp.model.Review;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    Long id;
    Long userId;
    String userName;
    String userAvatar;
    Long productId;
    String comment;
    Integer rating;
    String adminResponse;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

//    public static ReviewResponse fromReview(Review review) {
//        return ReviewResponse.builder()
//                .id(review.getId())
//                .userId(review.getUser().getId())
//                .userName(review.getUser().getFullName())
//                .userAvatar(review.getUser().getAvatarUrl())
//                .productId(review.getProduct().getId())
//                .comment(review.getComment())
//                .rating(review.getRating())
//                .adminResponse(review.getAdminResponse())
//                .createdAt(review.getCreateAt())
//                .updatedAt(review.getUpdatedAt())
//                .build();
//    }
}
