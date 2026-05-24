package com.radman.shop.product.service.impl;

import com.radman.shop.common.exception.*;
import com.radman.shop.product.model.Product;
import com.radman.shop.product.model.dao.ProductDao;
import com.radman.shop.product.service.ProductService;
import com.radman.shop.product.service.mapper.ProductServiceMapper;
import com.radman.shop.product.service.model.ProductResult;
import com.radman.shop.product.service.model.ProductQuantityDto;
import com.radman.shop.product.service.model.ProductsResult;
import com.radman.shop.product.service.model.UpdateProductStockModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final ProductServiceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public ProductsResult getAllProducts(Integer page, Integer size) {
        log.info("Fetching products. page={}, size={}", page, size);

        PageRequest pageRequest = PageRequest.of(page, size);
        return mapper.toProductsResult(productDao.findAll(pageRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResult getProduct(String productId) throws ProductNotFoundException {
        return mapper.toDto(findProduct(productId));
    }

    @Override
    @Transactional
    public void reserveProducts(UpdateProductStockModel model) throws BusinessException {
        log.info("Reserve products request. size={}", model.getProducts().size());

        updateStock(model, (product, qty) -> {
            int available = product.getTotalQuantity() - product.getReservedQuantity();

            if (available < qty)
                throw new InsufficientStockException(product.getId(), qty, available);

            product.setReservedQuantity(product.getReservedQuantity() + qty);
        });

        log.info("Reserve products completed.");
    }

    @Override
    @Transactional
    public void releaseProducts(UpdateProductStockModel model) throws BusinessException {
        log.info("Release products request. size={}", model.getProducts().size());

        updateStock(model, (product, qty) -> {
            if (product.getReservedQuantity() < qty)
                throw new InvalidStockStateException(product.getId());

            product.setReservedQuantity(product.getReservedQuantity() - qty);
        });

        log.info("Release products completed.");
    }

    @Override
    @Transactional
    public void fulfillProducts(UpdateProductStockModel model) throws BusinessException {
        log.info("Fulfill products request. size={}", model.getProducts().size());

        updateStock(model, (product, qty) -> {
            if (product.getReservedQuantity() < qty)
                throw new InvalidStockStateException(product.getId());

            product.setReservedQuantity(product.getReservedQuantity() - qty);
            product.setTotalQuantity(product.getTotalQuantity() - qty);
        });

        log.info("Fulfill products completed.");
    }

    private void updateStock(UpdateProductStockModel model, StockOperation operation) throws BusinessException {
        Map<String, Integer> quantityByProductId = model.getProducts().stream()
                .collect(Collectors.toMap(
                        ProductQuantityDto::getProductId,
                        ProductQuantityDto::getQuantity,
                        Integer::sum
                ));

        List<Product> productsForUpdate = productDao.findAllForUpdate(quantityByProductId.keySet());

        ensureAllProductsExist(quantityByProductId.keySet(), productsForUpdate);

        for (Product product : productsForUpdate) {
            Integer requestedQuantity = quantityByProductId.get(product.getId());
            ensureValidQuantity(requestedQuantity);

            operation.apply(product, requestedQuantity);
        }

        productDao.saveAll(productsForUpdate); // JPA dirty checking handles persistence; kept for write boundary clarity.
    }

    private Product findProduct(String productId) throws ProductNotFoundException {
        return productDao.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void ensureAllProductsExist(Set<String> requestedIds, List<Product> products)
            throws BusinessException {
        Set<String> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());

        for (String id : requestedIds) {
            if (!foundIds.contains(id)) {
                throw new ProductNotFoundException(id);
            }
        }
    }

    private void ensureValidQuantity(Integer quantity) throws BusinessException {
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        if (quantity <= 0) {
            throw new InvalidQuantityException(quantity);
        }
    }

    @FunctionalInterface
    private interface StockOperation {
        void apply(Product product, int quantity) throws BusinessException;
    }
}