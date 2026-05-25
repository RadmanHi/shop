package com.radman.shop.cart.service.impl;

import com.radman.shop.cart.model.Cart;
import com.radman.shop.cart.model.CartItem;
import com.radman.shop.cart.model.CheckoutState;
import com.radman.shop.cart.model.dao.CartDao;
import com.radman.shop.cart.service.CartService;
import com.radman.shop.cart.service.mapper.CartServiceMapper;
import com.radman.shop.cart.service.model.*;
import com.radman.shop.common.exception.*;
import com.radman.shop.product.service.ProductService;
import com.radman.shop.product.service.model.ProductPriceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final Duration CHECKOUT_TIMEOUT = Duration.ofMinutes(30);

    private final CartDao cartDao;
    private final ProductService productService;
    private final CartServiceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public CartResult getCart(String userId) throws BusinessException {
        return mapper.toCartResult(findCart(userId));
    }

    @Override
    @Transactional
    public void addItem(AddItemModel model) throws BusinessException {
        log.info("Adding item. userId={}, productId={}, quantity={}", model.userId(), model.productId(), model.quantity());

        Cart cart = cartDao.findByUserId(model.userId()).orElseGet(() -> mapper.toCart(model.userId()));
        ensureNotInCheckout(cart, model.userId());
        productService.ensureSufficientStock(model.productId(), model.quantity());

        cart.getItems().stream()
                .filter(i -> i.getProductId().equals(model.productId()))
                .findFirst()
                .ifPresentOrElse(i -> i.setQuantity(i.getQuantity() + model.quantity()), () ->
                        cart.getItems().add(mapper.toCartItem(cart, model.productId(), model.quantity())));
        cartDao.save(cart);

        log.info("Item added. userId={}, productId={}", model.userId(), model.productId());
    }


    @Override
    @Transactional
    public void updateItemQuantity(UpdateItemQuantityModel model) throws BusinessException {
        log.info("Updating item quantity. userId={}, productId={}, quantity={}", model.userId(), model.productId(), model.quantity());

        Cart cart = findCartForUpdate(model.userId());
        ensureNotInCheckout(cart, model.userId());
        productService.ensureSufficientStock(model.productId(), model.quantity());

        findItem(cart, model.productId())
                .setQuantity(model.quantity());
        cartDao.save(cart);

        log.info("Item quantity updated. userId={}, productId={}", model.userId(), model.productId());
    }

    @Override
    @Transactional
    public void removeItem(RemoveItemModel model) throws BusinessException {
        log.info("Removing item. userId={}, productId={}", model.userId(), model.productId());

        Cart cart = findCartForUpdate(model.userId());
        ensureNotInCheckout(cart, model.userId());

        if (!cart.getItems().removeIf(i -> i.getProductId().equals(model.productId())))
            throw new CartItemNotFoundException(model.productId());

        cartDao.save(cart);

        log.info("Item removed. userId={}, productId={}", model.userId(), model.productId());
    }


    @Override
    @Transactional
    public void initiateCheckout(String userId) throws BusinessException {
        log.info("Initiating checkout. userId={}", userId);

        Cart cart = findCartForUpdate(userId);
        ensureNotInCheckout(cart, userId);

        if (cart.getItems().isEmpty())
            throw new EmptyCartException(userId);

        createPriceSnapshots(cart);
        cartDao.save(cart);
        productService.reserveProducts(mapper.toStockModel(cart.getItems()));
        /*
         * Planned payment flow:
         *     paymentService.charge(calculateTotal(cart), userId);
         *
         * Payment handling is intentionally out of scope for this assignment.
         * The payable amount is derived from immutable checkout price snapshots
         * multiplied by item quantities.
         */
        log.info("Checkout initiated. userId={}", userId);
    }

    @Override
    @Transactional
    public void completeCheckout(PaymentResultModel model) throws BusinessException {
        log.info("Handling payment result. userId={}, status={}", model.userId(), model.status());

        Cart cart = findCartForUpdate(model.userId());
        boolean wasInCheckout = cart.getCheckoutState() == CheckoutState.CHECKOUT_IN_PROGRESS;
        if (!wasInCheckout) {
            log.warn("""
                            [ANOMALY] Payment result received for cart not in checkout.
                            Processing anyway to avoid stock leak.
                            userId={}, status={}, cartState={}
                            """,
                    model.userId(),
                    model.status(),
                    cart.getCheckoutState()
            );
        }
        switch (model.status()) {
            case PURCHASED -> {
                if (wasInCheckout) fulfillSilently(cart, model.userId());
                cart.getItems().clear();
                clearCheckout(cart);
            }
            case CANCELLED, TIMEOUT -> {
                if (wasInCheckout) releaseSilently(cart, model.userId());
                clearCheckout(cart);
            }
        }
        cartDao.save(cart);
        log.info("Payment result handled. userId={}, status={}", model.userId(), model.status());
    }

    private void fulfillSilently(Cart cart, String userId) {
        try {
            productService.fulfillProducts(mapper.toStockModel(cart.getItems()));
        } catch (Exception e) {
            log.warn("Fulfill failed, product will self-heal. userId={}", userId, e);
        }
    }

    private void releaseSilently(Cart cart, String userId) {
        try {
            productService.releaseProducts(mapper.toStockModel(cart.getItems()));
        } catch (Exception e) {
            log.warn("Release failed, product will self-heal. userId={}", userId, e);
        }
    }

    private void createPriceSnapshots(Cart cart) throws BusinessException {
        Map<String, BigDecimal> prices = productService
                .getPricesByProductIds(cart.getItems().stream().map(CartItem::getProductId).toList()).prices().stream()
                .collect(Collectors.toMap(ProductPriceDto::productId, ProductPriceDto::price));
        for (CartItem item : cart.getItems()) {
            item.setCheckoutPriceSnapshot(Optional.ofNullable(prices.get(item.getProductId()))
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId())));
        }
        cart.setCheckoutState(CheckoutState.CHECKOUT_IN_PROGRESS);
        cart.setCheckoutExpiresAt(Instant.now().plus(CHECKOUT_TIMEOUT));
    }

    private void clearCheckout(Cart cart) {
        cart.setCheckoutState(CheckoutState.IDLE);
        cart.setCheckoutExpiresAt(null);
        cart.getItems().forEach(item -> item.setCheckoutPriceSnapshot(null));
    }


    private void ensureNotInCheckout(Cart cart, String userId) throws CartAlreadyInCheckoutException {
        boolean isInCheckout = cart.getCheckoutState() == CheckoutState.CHECKOUT_IN_PROGRESS;
        boolean isExpired = cart.getCheckoutExpiresAt() != null && Instant.now().isAfter(cart.getCheckoutExpiresAt());
        if (isInCheckout && isExpired) {
            log.info("Checkout expired at read time, clearing. userId={}", userId);
            clearCheckout(cart);
            cartDao.save(cart);
            return;
        }
        if (isInCheckout) {
            throw new CartAlreadyInCheckoutException(userId);
        }
    }

    private CartItem findItem(Cart cart, String productId) throws CartItemNotFoundException {
        return cart.getItems().stream().filter(i -> i.getProductId().equals(productId)).findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(productId));
    }

    private Cart findCart(String userId) throws CartNotFoundException {
        return cartDao.findByUserId(userId).orElseThrow(() -> new CartNotFoundException(userId));
    }

    private Cart findCartForUpdate(String userId) throws CartNotFoundException {
        return cartDao.findByUserIdForUpdate(userId).orElseThrow(() -> new CartNotFoundException(userId));
    }
}