package com.radman.shop.cart.api.mapper;

import com.radman.shop.cart.api.model.*;
import com.radman.shop.cart.api.model.response.CheckoutDto;
import com.radman.shop.cart.api.model.response.CheckoutItemDto;
import com.radman.shop.cart.api.model.response.CheckoutResponse;
import com.radman.shop.cart.service.model.*;
import com.radman.shop.common.ResultStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = ResultStatus.class)
public interface CartResourceMapper {

    /*
     * Requests -> Models
     */

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "productId", source = "request.productId")
    @Mapping(target = "quantity", source = "request.quantity")
    AddItemModel toAddItemModel(String userId, AddItemRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "request.quantity")
    UpdateItemQuantityModel toUpdateItemQuantityModel(
            String userId,
            String productId,
            UpdateItemQuantityRequest request
    );

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "productId", source = "productId")
    RemoveItemModel toRemoveItemModel(
            String userId,
            String productId
    );

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "status", source = "request.status")
    PaymentResultModel toPaymentResultModel(
            String userId,
            PaymentResultRequest request
    );

    /*
     * Results -> Responses
     */

    @Mapping(target = "cart", source = ".")
    @Mapping(target = "result", expression = "java(ResultStatus.SUCCESS)")
    CartResponse toCartResponse(CartResult result);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "checkoutState", source = "checkoutState")
    @Mapping(target = "checkoutExpiresAt", source = "checkoutExpiresAt")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "items", source = "items")
    CartDto toCartDto(CartResult result);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "checkoutPriceSnapshot", source = "checkoutPriceSnapshot")
    CartItemDto toCartItemDto(CartItemResult item);

    @Mapping(target = "checkout", source = ".")
    CheckoutResponse toCheckoutResponse(CheckoutResult result);

    CheckoutDto toCheckoutDto(CheckoutResult result);

    CheckoutItemDto toCheckoutItemDto(CheckoutItemResult item);
}