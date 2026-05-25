package com.radman.shop.api.cart;

import com.radman.shop.AbstractContainerBaseTest;
import com.radman.shop.cart.api.model.*;
import com.radman.shop.cart.model.Cart;
import com.radman.shop.cart.model.CheckoutState;
import com.radman.shop.cart.model.dao.CartDao;
import com.radman.shop.cart.service.model.PaymentStatus;
import com.radman.shop.product.model.Product;
import com.radman.shop.product.model.dao.ProductDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


class CartControllerIT extends AbstractContainerBaseTest {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private CartDao cartDao;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        cartDao.deleteAll();
        productDao.deleteAll();
    }

    @Test
    @DisplayName("cart full flow - success path - checkout to payment completion")
    void full_flow_success() {
        createProducts();

        addItem("user-1", "product-1", 2);

        CartResponse cart1 = getCart("user-1");
        assertEquals(1, cart1.getCart().items().size());
        assertEquals(2, cart1.getCart().items().getFirst().quantity());

        checkout("user-1");

        CartResponse cart2 = getCart("user-1");
        assertEquals(CheckoutState.CHECKOUT_IN_PROGRESS, cart2.getCart().checkoutState());

        payment("user-1", PaymentStatus.PURCHASED);

        CartResponse cart3 = getCart("user-1");
        assertEquals(CheckoutState.IDLE, cart3.getCart().checkoutState());
        assertTrue(cart3.getCart().items().isEmpty());
    }

    @Test
    @DisplayName("cart checkout - timeout flow - releases stock and resets cart")
    void checkout_timeout_flow() {
        createProducts();

        addItem("user-1", "product-1", 2);
        checkout("user-1");

        payment("user-1", PaymentStatus.TIMEOUT);

        CartResponse cart = getCart("user-1");
        assertEquals(CheckoutState.IDLE, cart.getCart().checkoutState());
        assertFalse(cart.getCart().items().isEmpty());
    }

    @Test
    @DisplayName("cart payment - idempotency - repeated PURCHASED event is safe")
    void payment_idempotency() {
        createProducts();

        addItem("user-1", "product-1", 2);
        checkout("user-1");

        payment("user-1", PaymentStatus.PURCHASED);
        payment("user-1", PaymentStatus.PURCHASED);

        CartResponse cart = getCart("user-1");
        assertEquals(CheckoutState.IDLE, cart.getCart().checkoutState());
        assertTrue(cart.getCart().items().isEmpty());
    }

    @Test
    @DisplayName("cart checkout - blocked when already in checkout state")
    void checkout_conflict() {
        createProducts();

        addItem("user-1", "product-1", 2);
        checkout("user-1");

        client.post()
                .uri("/api/v1/cart/checkout")
                .header("X-User-Id", "user-1")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("cart checkout - stock reservation failure leaves cart in checkout state")
    void checkout_stock_failure_handling() {
        Product p = new Product();
        p.setId("product-1");
        p.setSku("SKU-1");
        p.setName("iPhone");
        p.setPrice(BigDecimal.valueOf(1000));
        p.setTotalQuantity(1);
        p.setReservedQuantity(0);
        productDao.save(p);

        addItem("user-1", "product-1", 1);

        // Simulate stock depletion between add and checkout
        p.setTotalQuantity(0);
        productDao.save(p);

        checkoutFailure("user-1");

        CartResponse response = getCart("user-1");
        assertEquals(CheckoutState.CHECKOUT_IN_PROGRESS, response.getCart().checkoutState());
    }

    @Test
    @DisplayName("add item - insufficient stock - request rejected")
    void addItem_insufficientStock_rejected() {
        Product p = new Product();
        p.setId("product-1");
        p.setSku("SKU-1");
        p.setName("iPhone");
        p.setPrice(BigDecimal.valueOf(1000));
        p.setTotalQuantity(2);
        p.setReservedQuantity(0);
        productDao.save(p);

        AddItemRequest req = new AddItemRequest();
        req.setProductId("product-1");
        req.setQuantity(10);

        client.post()
                .uri("/api/v1/cart/items")
                .header("X-User-Id", "user-1")
                .body(req)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("add item - same product twice - quantities accumulate")
    void addItem_sameProductTwice_quantityAccumulates() {
        createProducts();

        addItem("user-1", "product-1", 2);
        addItem("user-1", "product-1", 3);

        CartResponse cart = getCart("user-1");
        assertEquals(1, cart.getCart().items().size());
        assertEquals(5, cart.getCart().items().getFirst().quantity());
    }

    @Test
    @DisplayName("add item - non-existent product - request rejected")
    void addItem_nonExistentProduct_rejected() {
        AddItemRequest req = new AddItemRequest();
        req.setProductId("non-existent");
        req.setQuantity(1);

        client.post()
                .uri("/api/v1/cart/items")
                .header("X-User-Id", "user-1")
                .body(req)
                .exchange()
                .expectStatus().is4xxClientError();
    }


    @Test
    @DisplayName("update item quantity - replaces quantity not adds")
    void updateItemQuantity_replacesQuantity() {
        createProducts();

        addItem("user-1", "product-1", 2);
        updateItemQuantity("user-1", "product-1", 5);

        CartResponse cart = getCart("user-1");
        assertEquals(5, cart.getCart().items().getFirst().quantity());
    }

    @Test
    @DisplayName("update item quantity - insufficient stock - request rejected")
    void updateItemQuantity_insufficientStock_rejected() {
        Product p = new Product();
        p.setId("product-1");
        p.setSku("SKU-1");
        p.setName("iPhone");
        p.setPrice(BigDecimal.valueOf(1000));
        p.setTotalQuantity(2);
        p.setReservedQuantity(0);
        productDao.save(p);

        addItem("user-1", "product-1", 1);

        UpdateItemQuantityRequest req = new UpdateItemQuantityRequest();;
        req.setQuantity(10);

        client.patch()
                .uri("/api/v1/cart/items/{productId}", "product-1")
                .header("X-User-Id", "user-1")
                .body(req)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("remove item - not in cart - returns 404")
    void removeItem_notInCart_returns404() {
        createProducts();
        addItem("user-1", "product-1", 2);

        client.delete()
                .uri("/api/v1/cart/items/{productId}", "product-2")
                .header("X-User-Id", "user-1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("get cart - cart does not exist - returns 404")
    void getCart_notFound_returns404() {
        client.get()
                .uri("/api/v1/cart")
                .header("X-User-Id", "user-1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("expired checkout - cleared on next write - user can add items again")
    void expiredCheckout_clearedOnNextWrite() {
        createProducts();

        addItem("user-1", "product-1", 2);
        checkout("user-1");

        // Manually expire the checkout
        Cart cart = cartDao.findByUserId("user-1").orElseThrow();
        cart.setCheckoutExpiresAt(Instant.now().minus(Duration.ofMinutes(1)));
        cartDao.save(cart);

        // Should succeed — expired checkout is cleared on write path
        addItem("user-1", "product-1", 1);

        CartResponse response = getCart("user-1");
        assertEquals(CheckoutState.IDLE, response.getCart().checkoutState());
    }

    @Test
    @DisplayName("checkout - empty cart - request rejected")
    void checkout_emptyCart_rejected() {
        // Create cart implicitly via a product add then remove
        createProducts();
        addItem("user-1", "product-1", 1);
        removeItem("user-1", "product-1");

        client.post()
                .uri("/api/v1/cart/checkout")
                .header("X-User-Id", "user-1")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("cart concurrency - only one cart created for same user")
    void concurrent_cart_creation() throws Exception {

        createProducts();

        int threads = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    addItem("user-1", "product-1", 1);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        List<Cart> carts = cartDao.findAll();

        assertEquals(1, carts.size());
    }

    @Test
    @DisplayName("cart concurrency - only one checkout transitions cart state")
    void concurrent_checkout() throws Exception {

        createProducts();
        addItem("user-1", "product-1", 2);

        int threads = 5;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    checkout("user-1");
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        CartResponse cart = getCart("user-1");

        assertEquals(
                CheckoutState.CHECKOUT_IN_PROGRESS,
                cart.getCart().checkoutState()
        );
    }

    @Test
    @DisplayName("cart concurrency - duplicate payment events are idempotent")
    void concurrent_payment_events() throws Exception {

        createProducts();

        addItem("user-1", "product-1", 2);
        checkout("user-1");

        int threads = 5;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    payment("user-1", PaymentStatus.PURCHASED);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        CartResponse cart = getCart("user-1");

        assertEquals(
                CheckoutState.IDLE,
                cart.getCart().checkoutState()
        );

        assertTrue(cart.getCart().items().isEmpty());
    }

    @Test
    @DisplayName("cart concurrency - add and remove item concurrently keeps consistent state")
    void concurrent_add_and_remove() throws Exception {

        createProducts();

        addItem("user-1", "product-1", 5);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        executor.submit(() -> {
            try {
                addItem("user-1", "product-1", 5);
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                removeItem("user-1", "product-1");
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        CartResponse cart = getCart("user-1");

        int quantity = cart.getCart().items().stream()
                .filter(i -> i.productId().equals("product-1"))
                .findFirst()
                .map(CartItemDto::quantity)
                .orElse(0);

        assertTrue(quantity == 0 || quantity == 10);
    }

    @Test
    @DisplayName("cart concurrency - duplicate remove requests are safe")
    void concurrent_remove_item() throws Exception {

        createProducts();

        addItem("user-1", "product-1", 1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                try {
                    removeItem("user-1", "product-1");
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        CartResponse cart = getCart("user-1");

        assertTrue(cart.getCart().items().isEmpty());
    }

    @Test
    @DisplayName("cart concurrency - checkout blocks item modification")
    void checkout_vs_update_race() throws Exception {

        createProducts();

        addItem("user-1", "product-1", 2);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        executor.submit(() -> {
            try {
                checkout("user-1");
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {

                UpdateItemQuantityRequest request = new UpdateItemQuantityRequest();
                request.setQuantity(99);

                client.patch()
                        .uri("/api/v1/cart/items/product-1")
                        .header("X-User-Id", "user-1")
                        .body(request)
                        .exchange();

            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        CartResponse cart = getCart("user-1");

        assertEquals(
                CheckoutState.CHECKOUT_IN_PROGRESS,
                cart.getCart().checkoutState()
        );

        assertNotEquals(
                99,
                cart.getCart().items().getFirst().quantity()
        );
    }

    @Test
    @DisplayName("cart concurrency - checkout and payment completion maintain consistent state")
    void concurrent_checkout_and_payment() throws Exception {

        createProducts();

        addItem("user-1", "product-1", 2);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        executor.submit(() -> {
            try {
                checkout("user-1");
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                payment("user-1", PaymentStatus.PURCHASED);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        CartResponse cart = getCart("user-1");

        assertTrue(
                cart.getCart().checkoutState() == CheckoutState.IDLE
                        || cart.getCart().checkoutState() == CheckoutState.CHECKOUT_IN_PROGRESS
        );
    }

    private void addItem(String userId, String productId, int qty) {
        AddItemRequest req = new AddItemRequest();
        req.setProductId(productId);
        req.setQuantity(qty);

        client.post()
                .uri("/api/v1/cart/items")
                .header("X-User-Id", userId)
                .body(req)
                .exchange()
                .expectStatus().isOk();
    }

    private void updateItemQuantity(String userId, String productId, int qty) {
        UpdateItemQuantityRequest req = new UpdateItemQuantityRequest();
        req.setQuantity(qty);

        client.patch()
                .uri("/api/v1/cart/items/{productId}", productId)
                .header("X-User-Id", userId)
                .body(req)
                .exchange()
                .expectStatus().isOk();
    }

    private void removeItem(String userId, String productId) {
        client.delete()
                .uri("/api/v1/cart/items/{productId}", productId)
                .header("X-User-Id", userId)
                .exchange()
                .expectStatus().isOk();
    }

    private void checkout(String userId) {
        client.post()
                .uri("/api/v1/cart/checkout")
                .header("X-User-Id", userId)
                .exchange()
                .expectStatus().isOk();
    }

    private void checkoutFailure(String userId) {
        client.post()
                .uri("/api/v1/cart/checkout")
                .header("X-User-Id", userId)
                .exchange();
    }

    private void payment(String userId, PaymentStatus status) {
        PaymentResultRequest req = new PaymentResultRequest();
        req.setStatus(status);

        client.post()
                .uri("/api/v1/cart/payment-result")
                .header("X-User-Id", userId)
                .body(req)
                .exchange()
                .expectStatus().isOk();
    }

    private CartResponse getCart(String userId) {
        return client.get()
                .uri("/api/v1/cart")
                .header("X-User-Id", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private void createProducts() {
        Product p = new Product();
        p.setId("product-1");
        p.setSku("SKU-IPHONE-001");
        p.setName("iPhone");
        p.setPrice(BigDecimal.valueOf(1000));
        p.setTotalQuantity(10);
        p.setReservedQuantity(0);
        productDao.save(p);
    }
}