package com.northernchile.api.cart;

import com.northernchile.api.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserId(UUID userId);

    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.items items " +
           "LEFT JOIN FETCH items.schedule s " +
           "LEFT JOIN FETCH s.tour " +
           "WHERE c.id = :cartId")
    Optional<Cart> findByIdWithDetails(@Param("cartId") UUID cartId);

    @Query("SELECT COALESCE(SUM(ci.numParticipants), 0) FROM CartItem ci " +
           "WHERE ci.schedule.id = :scheduleId " +
           "AND ci.cart.expiresAt > CURRENT_TIMESTAMP")
    Integer countParticipantsByScheduleId(@Param("scheduleId") UUID scheduleId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.expiresAt < :now")
    int deleteByExpiresAtBefore(@Param("now") Instant now);

    /**
     * Check if there are any expired carts (lightweight check before running DELETE)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cart c WHERE c.expiresAt < :now")
    boolean existsExpiredCarts(@Param("now") Instant now);

    @Query("SELECT COALESCE(SUM(ci.numParticipants), 0) FROM CartItem ci " +
           "WHERE ci.schedule.id = :scheduleId " +
           "AND ci.cart.id != :excludeCartId " +
           "AND ci.cart.expiresAt > CURRENT_TIMESTAMP")
    Integer countParticipantsByScheduleIdExcludingCart(
            @Param("scheduleId") UUID scheduleId,
            @Param("excludeCartId") UUID excludeCartId);

    @Query("SELECT COALESCE(SUM(ci.numParticipants), 0) FROM CartItem ci " +
           "WHERE ci.schedule.id = :scheduleId " +
           "AND ci.cart.user.id != :excludeUserId " +
           "AND ci.cart.expiresAt > CURRENT_TIMESTAMP")
    Integer countParticipantsByScheduleIdExcludingUser(
            @Param("scheduleId") UUID scheduleId,
            @Param("excludeUserId") UUID excludeUserId);
}
