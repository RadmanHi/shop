package com.radman.shop.cart.api.model.request;

import com.radman.shop.cart.service.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentResultRequest {

    @NotNull
    private PaymentStatus status;
}