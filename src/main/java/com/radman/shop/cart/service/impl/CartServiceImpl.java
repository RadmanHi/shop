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

        productService.getProduct(model.productId());
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
        findItem(cart, model.productId()).setQuantity(model.quantity());
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

        try {
            productService.reserveProducts(mapper.toStockModel(cart.getItems()));
        } catch (Exception e) {
            log.warn("Reserve failed, product must self-heal. userId={}", userId, e);
        }
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

        if (cart.getCheckoutState() != CheckoutState.CHECKOUT_IN_PROGRESS) {
            log.warn("Cart not in checkout, ignoring. userId={}", model.userId());
            return;
        }

        switch (model.status()) {
            case PURCHASED -> {
                try {
                    productService.fulfillProducts(mapper.toStockModel(cart.getItems()));
                } catch (Exception e) {
                    log.warn("Fulfill failed, product will self-heal. userId={}", model.userId(), e);
                }
                cart.getItems().clear();
                clearCheckout(cart);
            }
            case CANCELLED, TIMEOUT -> {
                try {
                    productService.releaseProducts(mapper.toStockModel(cart.getItems()));
                } catch (Exception e) {
                    log.warn("Release failed, product will self-heal. userId={}", model.userId(), e);
                }
                clearCheckout(cart);
            }
        }
        cartDao.save(cart);

        log.info("Payment result handled. userId={}, status={}", model.userId(), model.status());
    }

    private void createPriceSnapshots(Cart cart) throws BusinessException {
        Map<String, BigDecimal> prices = productService
                .getPricesByProductIds(cart.getItems().stream().map(CartItem::getProductId).toList()).prices().stream()
                .collect(Collectors.toMap(ProductPriceDto::productId, ProductPriceDto::price));

        cart.setCheckoutState(CheckoutState.CHECKOUT_IN_PROGRESS);
        cart.setCheckoutExpiresAt(Instant.now().plus(CHECKOUT_TIMEOUT));
        cart.getItems().forEach(item -> item.setCheckoutPriceSnapshot(prices.get(item.getProductId())));
    }

    private void clearCheckout(Cart cart) {
        cart.setCheckoutState(CheckoutState.IDLE);
        cart.setCheckoutExpiresAt(null);
        cart.getItems().forEach(item -> item.setCheckoutPriceSnapshot(null));
    }


    private void ensureNotInCheckout(Cart cart, String userId) throws CartAlreadyInCheckoutException {
        if (cart.getCheckoutState() == CheckoutState.CHECKOUT_IN_PROGRESS)
            throw new CartAlreadyInCheckoutException(userId);
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