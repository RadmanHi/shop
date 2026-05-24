package com.radman.shop.cart.service;

import com.radman.shop.cart.model.Cart;
import com.radman.shop.cart.model.CheckoutState;
import com.radman.shop.cart.model.dao.CartDao;
import com.radman.shop.cart.service.mapper.CartServiceMapper;
import com.radman.shop.cart.service.model.PaymentStatus;
import com.radman.shop.config.ConfigProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckoutTimeoutJob {

    private final ConfigProvider configProvider;
    private final CartDao cartDao;
    private final CartService cartService;
    private final CartServiceMapper mapper;

    @Scheduled(fixedDelayString = "${shop.checkout.timeout-job.fixed-delay-ms:60000}")
    public void expireStaleCheckouts() {
        if (configProvider.isStaleCheckoutExpiryEnabled()) {
            List<Cart> expired = cartDao.findByCheckoutStateAndCheckoutExpiresAtBefore(
                    CheckoutState.CHECKOUT_IN_PROGRESS, Instant.now()
            );
            log.info("Expiring stale checkouts. count={}", expired.size());
            for (Cart cart : expired) {
                log.info("Expiring checkout. cartId={}, expiredAt={}", cart.getId(), cart.getCheckoutExpiresAt());
                try {
                    cartService.completeCheckout(mapper.toPaymentResultModel(cart.getUserId(), PaymentStatus.TIMEOUT));
                } catch (Exception e) {
                    log.error("Failed to expire checkout. cartId={}", cart.getId(), e);
                }
            }
        } else {
            log.warn("Stale checkout expiry is disabled");
        }
    }
}