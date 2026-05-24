package com.radman.shop.cart.service.model;

public record UpdateItemQuantityModel(String userId, String productId, Integer quantity) {
}