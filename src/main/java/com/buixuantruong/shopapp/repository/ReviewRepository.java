package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Lấy tất cả review của một sản phẩm theo trang
    Page<Review> findByProduct_Id(Long productId, Pageable pageable);

    // Lấy tất cả review của một user
    List<Review> findByUser_Id(Long userId);

    // Kiểm tra user đã review sản phẩm chưa (mỗi user chỉ được review 1 lần)
    Optional<Review> findByUser_IdAndProduct_Id(Long userId, Long productId);

    // Tính điểm trung bình của sản phẩm
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    // Đếm số lượng review theo từng mức sao
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> countReviewsByRatingForProduct(@Param("productId") Long productId);
}
