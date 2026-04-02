package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT COUNT(r) FROM Review r")
    Long countAllReviews();

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r")
    Double getAverageRating();
}
