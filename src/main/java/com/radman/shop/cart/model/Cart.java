package com.radman.shop.cart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "carts",
        indexes = {
                @Index(name = "idx_cart_user_id", columnList = "userId"),
                @Index(name = "idx_cart_checkout_state", columnList = "checkoutState")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_user_id", columnNames = "userId")
        }
)
public class Cart {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckoutState checkoutState = CheckoutState.IDLE;

    private Instant checkoutExpiresAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();
}