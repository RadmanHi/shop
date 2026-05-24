package com.radman.shop.cart.model.dao;

import com.radman.shop.cart.model.Cart;
import com.radman.shop.cart.model.CheckoutState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartDao extends JpaRepository<Cart, String> {

    Optional<Cart> findByUserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.userId = :userId")
    Optional<Cart> findByUserIdForUpdate(@Param("userId") String userId);

    List<Cart> findByCheckoutStateAndCheckoutExpiresAtBefore(CheckoutState state, Instant cutoff);
}