package com.radman.shop.api.cart;

import com.radman.shop.AbstractContainerBaseTest;
import com.radman.shop.cart.api.model.AddItemRequest;
import com.radman.shop.cart.api.model.CartResponse;
import com.radman.shop.cart.api.model.PaymentResultRequest;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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
    @DisplayName("cart checkout - stock failure does not break cart state")
    void checkout_stock_failure_handling() {

        Product p = new Product();
        p.setId("product-1");
        p.setSku("SKU-1");
        p.setName("iPhone");
        p.setPrice(BigDecimal.valueOf(1000));
        p.setTotalQuantity(1);
        p.setReservedQuantity(0);
        productDao.save(p);

        addItem("user-1", "product-1", 5);

        checkout("user-1");

        CartResponse cart = getCart("user-1");

        assertEquals(CheckoutState.CHECKOUT_IN_PROGRESS, cart.getCart().checkoutState());
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

    private void checkout(String userId) {
        client.post()
                .uri("/api/v1/cart/checkout")
                .header("X-User-Id", userId)
                .exchange()
                .expectStatus().isOk();
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