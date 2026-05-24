package com.radman.shop.product.service;

import com.radman.shop.common.exception.BusinessException;
import com.radman.shop.common.exception.ProductNotFoundException;
import com.radman.shop.product.service.model.ProductResult;
import com.radman.shop.product.service.model.ProductsResult;
import com.radman.shop.product.service.model.UpdateProductStockModel;

public interface ProductService {

    ProductsResult getAllProducts(Integer page, Integer size);

    ProductResult getProduct(String productId) throws ProductNotFoundException;

    void reserveProducts(UpdateProductStockModel model) throws BusinessException;

    void releaseProducts(UpdateProductStockModel model) throws BusinessException;

    void fulfillProducts(UpdateProductStockModel model) throws BusinessException;
}
