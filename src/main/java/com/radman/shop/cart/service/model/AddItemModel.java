package com.radman.shop.cart.service.model;

public record AddItemModel(String userId, String productId, Integer quantity) {
}