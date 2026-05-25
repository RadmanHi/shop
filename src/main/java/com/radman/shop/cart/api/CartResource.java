package com.radman.shop.cart.api;

import com.radman.shop.cart.api.mapper.CartResourceMapper;
import com.radman.shop.cart.api.model.AddItemRequest;
import com.radman.shop.cart.api.model.CartResponse;
import com.radman.shop.cart.api.model.PaymentResultRequest;
import com.radman.shop.cart.api.model.UpdateItemQuantityRequest;
import com.radman.shop.cart.service.CartService;
import com.radman.shop.common.GeneralResponse;
import com.radman.shop.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "Cart operations (requires X-User-Id=user-1 in local/dev)")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartResource {

    private final CartService cartService;
    private final CartResourceMapper mapper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartResponse> getCart(@RequestHeader("X-User-Id") String userId) throws BusinessException {
        log.info("Getting cart. userId={}", userId);
        return ResponseEntity.ok(mapper.toCartResponse(cartService.getCart(userId)));
    }

    @PostMapping(path = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralResponse> addItem(@RequestHeader("X-User-Id") String userId,
            @RequestBody @Valid AddItemRequest request) throws BusinessException {
        log.info("Adding item. userId={}, productId={}", userId, request.getProductId());
        cartService.addItem(mapper.toAddItemModel(userId, request));
        return ResponseEntity.ok(GeneralResponse.success());
    }

    @PatchMapping(path = "/items/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralResponse> updateItemQuantity(@RequestHeader("X-User-Id") String userId,
             @PathVariable String productId, @RequestBody @Valid UpdateItemQuantityRequest request) throws BusinessException {
        log.info("Updating item quantity. userId={}, productId={}", userId, productId);
        cartService.updateItemQuantity(mapper.toUpdateItemQuantityModel(userId, productId, request));
        return ResponseEntity.ok(GeneralResponse.success());
    }

    @DeleteMapping(path = "/items/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralResponse> removeItem(@RequestHeader("X-User-Id") String userId,
            @PathVariable String productId) throws BusinessException {
        log.info("Removing item. userId={}, productId={}", userId, productId);
        cartService.removeItem(mapper.toRemoveItemModel(userId, productId));
        return ResponseEntity.ok(GeneralResponse.success());
    }

    @Operation(summary = "Checkout cart", description = "Starts checkout flow for current user cart")
    @PostMapping(path = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralResponse> checkout(@RequestHeader("X-User-Id") String userId) throws BusinessException {
        log.info("Checkout requested. userId={}", userId);
        cartService.initiateCheckout(userId);
        return ResponseEntity.ok(GeneralResponse.success());
    }

    @Operation(summary = "Internal payment callback",
            description = """
                        Internal use only.
                        This endpoint is called by payment service after transaction completion.
                    
                        Do NOT call directly from frontend or gateway.
                        Must be triggered immediately after checkout flow.
                    
                        Requires valid X-User-Id header (seeded test user in local/dev environments).
                    """
    )
    @PostMapping(path = "/payment-result", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralResponse> paymentResult(@RequestHeader("X-User-Id") String userId,
            @RequestBody @Valid PaymentResultRequest request) throws BusinessException {
        log.info("Payment result received. userId={}, status={}", userId, request.getStatus());
        cartService.completeCheckout(mapper.toPaymentResultModel(userId, request));
        return ResponseEntity.ok(GeneralResponse.success());
    }
}