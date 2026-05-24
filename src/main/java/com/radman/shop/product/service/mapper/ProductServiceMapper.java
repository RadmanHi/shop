package com.radman.shop.product.service.mapper;

import com.radman.shop.product.model.Product;
import com.radman.shop.product.service.model.ProductPriceDto;
import com.radman.shop.product.service.model.ProductPricesResult;
import com.radman.shop.product.service.model.ProductResult;
import com.radman.shop.product.service.model.ProductsResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;


@Mapper(componentModel = "spring")
public interface ProductServiceMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "totalQuantity", source = "totalQuantity")
    @Mapping(target = "reservedQuantity", source = "reservedQuantity")
    @Mapping(target = "creationDate",
            expression = "java(product.getCreatedAt() != null ? product.getCreatedAt().toEpochMilli() : null)"
    )
    ProductResult toDto(Product product);

    @Mapping(target = "products", source = "content")
    @Mapping(target = "page", expression = "java(products.getNumber())")
    @Mapping(target = "size", expression = "java(products.getSize())")
    @Mapping(target = "totalElements", expression = "java(products.getTotalElements())")
    @Mapping(target = "totalPages", expression = "java(products.getTotalPages())")
    ProductsResult toProductsResult(Page<Product> products);


    @Mapping(target = "productId", source = "id")
    ProductPriceDto toPriceDto(Product product);

    List<ProductPriceDto> toPriceDtos(List<Product> products);

    default ProductPricesResult toPricesResult(List<Product> products) {
        return new ProductPricesResult(
                toPriceDtos(products)
        );
    }
}
