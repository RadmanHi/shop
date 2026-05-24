package com.radman.shop.cart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "cart_items",
        indexes = {
                @Index(name = "idx_cart_item_cart_id", columnList = "cart_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_item_cart_product", columnNames = {"cart_id", "productId"})
        }
)
public class CartItem {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    private BigDecimal checkoutPriceSnapshot;
}