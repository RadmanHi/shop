package com.radman.shop.product.api.mapper;

import com.radman.shop.common.ResultStatus;
import com.radman.shop.product.api.model.response.GetAllProductsResponse;
import com.radman.shop.product.api.model.response.ProductDto;
import com.radman.shop.product.api.model.response.ProductResponse;
import com.radman.shop.product.service.model.ProductResult;
import com.radman.shop.product.service.model.ProductsResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {ResultStatus.class})
public interface ProductResourceMapper {

    @Mapping(target = "products", source = "products")
    @Mapping(target = "page", source = "page")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "totalElements", source = "totalElements")
    @Mapping(target = "totalPages", source = "totalPages")
    @Mapping(target = "result", expression = "java(ResultStatus.SUCCESS)")
    GetAllProductsResponse toGetAllProductsResponse(ProductsResult result);

    @Mapping(target = "product", source = "product")
    @Mapping(target = "result", expression = "java(ResultStatus.SUCCESS)")
    ProductResponse toProductResponse(ProductResult product);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "availableQuantity", expression = "java(product.getTotalQuantity() - product.getReservedQuantity())")
    ProductDto toProductDto(ProductResult product);
}
