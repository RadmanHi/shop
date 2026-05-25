package com.radman.shop.api.product;

import com.radman.shop.AbstractContainerBaseTest;
import com.radman.shop.product.api.model.response.GetAllProductsResponse;
import com.radman.shop.product.api.model.response.ProductResponse;
import com.radman.shop.product.model.Product;
import com.radman.shop.product.model.dao.ProductDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class ProductControllerIT extends AbstractContainerBaseTest {

    @Autowired
    private ProductDao productDao;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        productDao.deleteAll();
    }

    @Test
    void getAllProducts_shouldReturnSeededProducts() {
        createProducts();

        GetAllProductsResponse response = client.get()
                .uri("/api/v1/products?page=0&size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(GetAllProductsResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(2, response.getProducts().size());
        assertEquals("product-1", response.getProducts().getFirst().id());
        assertEquals("iPhone", response.getProducts().getFirst().name());
    }

    @Test
    void getProduct_shouldReturnSingleProduct() {
        createProducts();
        ProductResponse response = client.get()
                .uri("/api/v1/products/{id}", "product-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals("product-1", response.getProduct().id());
        assertEquals("iPhone", response.getProduct().name());
    }

    private void createProducts() {

        Product p1 = new Product();
        p1.setId("product-1");
        p1.setSku("SKU-IPHONE-001");
        p1.setName("iPhone");
        p1.setPrice(BigDecimal.valueOf(1000));
        p1.setTotalQuantity(10);
        p1.setReservedQuantity(0);

        Product p2 = new Product();
        p2.setId("product-2");
        p2.setSku("SKU-MAC-001");
        p2.setName("MacBook");
        p2.setPrice(BigDecimal.valueOf(2000));
        p2.setTotalQuantity(5);
        p2.setReservedQuantity(0);

        productDao.saveAll(List.of(p1, p2));
    }
}