package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id IN (SELECT c.id FROM Cart c WHERE c.updatedAt < :olderThan)")
    void deleteInactiveCartItems(@Param("olderThan") java.time.LocalDateTime olderThan);
}
