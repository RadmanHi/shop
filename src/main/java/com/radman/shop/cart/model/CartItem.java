package com.radman.shop.cart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// CartItem.java
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "cart_items",
        indexes = {
                @Index(name = "idx_cart_item_basket_id", columnList = "basketId")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_item_basket_product", columnNames = {"basketId", "productId"})
        }
)
public class CartItem {

    @Id
    private String id;

    @Column(nullable = false)
    private String basketId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    private BigDecimal checkoutPriceSnapshot;
}