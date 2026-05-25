package com.radman.shop.cart.service.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment processing status")
public enum PaymentStatus {
    @Schema(description = "Payment completed successfully")
    PURCHASED,
    @Schema(description = "Payment cancelled")
    CANCELLED,
    @Schema(description = "Payment timed out")
    TIMEOUT
}