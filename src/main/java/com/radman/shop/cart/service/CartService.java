package com.radman.shop.cart.service;

import com.radman.shop.cart.service.model.*;
import com.radman.shop.common.exception.BusinessException;

public interface CartService {

    CartResult getCart(String userId);

    void addItem(AddItemModel model) throws BusinessException;

    void updateItemQuantity(UpdateItemQuantityModel model) throws BusinessException;

    void removeItem(RemoveItemModel model) throws BusinessException;

    CheckoutResult initiateCheckout(String userId) throws BusinessException;

    void completeCheckout(PaymentResultModel model) throws BusinessException;
}