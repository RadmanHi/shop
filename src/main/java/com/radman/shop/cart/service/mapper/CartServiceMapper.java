package com.radman.shop.cart.service.mapper;

import com.radman.shop.cart.model.Cart;
import com.radman.shop.cart.model.CartItem;
import com.radman.shop.cart.model.CheckoutState;
import com.radman.shop.cart.service.model.CartItemResult;
import com.radman.shop.cart.service.model.CartResult;
import com.radman.shop.cart.service.model.PaymentResultModel;
import com.radman.shop.cart.service.model.PaymentStatus;
import com.radman.shop.product.service.model.ProductQuantityDto;
import com.radman.shop.product.service.model.UpdateProductStockModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, CheckoutState.class})
public interface CartServiceMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "checkoutState", expression = "java(CheckoutState.IDLE)")
    @Mapping(target = "items", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "checkoutExpiresAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Cart toCart(String userId);

    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "basketId", source = "cartId")
    @Mapping(target = "checkoutPriceSnapshot", ignore = true)
    CartItem toCartItem(String cartId, String productId, Integer quantity);

    ProductQuantityDto toProductQuantityDto(CartItem item);

    default UpdateProductStockModel toStockModel(List<CartItem> items) {
        return new UpdateProductStockModel(
                items.stream()
                        .map(this::toProductQuantityDto)
                        .toList()
        );
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "checkoutState", source = "checkoutState")
    @Mapping(
            target = "checkoutExpiresAt",
            expression = "java(cart.getCheckoutExpiresAt() != null ? cart.getCheckoutExpiresAt().toEpochMilli() : null)"
    )
    @Mapping(
            target = "createdAt",
            expression = "java(cart.getCreatedAt() != null ? cart.getCreatedAt().toEpochMilli() : null)"
    )
    @Mapping(target = "items", source = "items")
    CartResult toCartResult(Cart cart);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "checkoutPriceSnapshot", source = "checkoutPriceSnapshot")
    CartItemResult toCartItemResult(CartItem item);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "status", source = "status")
    PaymentResultModel toPaymentResultModel(String userId, PaymentStatus status);

}