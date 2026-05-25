package com.radman.shop.product.api;

import com.radman.shop.common.annotation.InRange;
import com.radman.shop.common.exception.BusinessException;
import com.radman.shop.product.api.mapper.ProductResourceMapper;
import com.radman.shop.product.api.model.response.GetAllProductsResponse;
import com.radman.shop.product.api.model.response.ProductResponse;
import com.radman.shop.product.service.ProductService;
import com.radman.shop.product.service.model.ProductResult;
import com.radman.shop.product.service.model.ProductsResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Products")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductResource {

    private final ProductService productService;

    private final ProductResourceMapper mapper;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAllProductsResponse> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10")
            @InRange(min = "shop.search.size.min", max = "shop.search.size.max")
            Integer size
    ) {
        log.info("Getting products. page={}, size={}", page, size);
        ProductsResult result = productService.getAllProducts(page, size);
        log.info("Retrieved {} products", result.products().size());
        return ResponseEntity.ok(mapper.toGetAllProductsResponse(result));
    }

    @GetMapping(path = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String productId) throws BusinessException {
        log.info("Getting product. productId={}", productId);
        ProductResult result = productService.getProduct(productId);
        log.info("Retrieved product. productId={}", productId);
        return ResponseEntity.ok(mapper.toProductResponse(result));
    }
}