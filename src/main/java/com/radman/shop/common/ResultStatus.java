package com.radman.shop.common;

import lombok.Getter;

import java.io.IOException;
import java.util.Properties;

@Getter
public enum ResultStatus {

    SUCCESS(0, "success"),
    UNKNOWN(1, "unknown.error"),
    FAILURE(2, "failure"),
    INVALID_PARAMETER(3, "core.invalid.parameter.exception"),
    FORBIDDEN_REQUEST(4, "forbidden.request"),
    PRODUCT_NOT_FOUND(5, "product.not.found"),
    INSUFFICIENT_STOCK(6, "product.insufficient.stock"),
    INVALID_STOCK_STATE(7, "product.invalid.stock.state"),
    INVALID_QUANTITY(8, "product.invalid.quantity");

    private final String description;

    private final Integer status;

    ResultStatus(int status, String description) {
        this.status = status;
        String errorMsg = MessageHolder.ERROR_MESSAGE_PROPERTIES.getProperty(description);
        this.description = errorMsg != null ? errorMsg : description;
    }

    private static final class MessageHolder {

        private static final Properties ERROR_MESSAGE_PROPERTIES = new Properties();

        static {
            try {
                ERROR_MESSAGE_PROPERTIES.load((ResultStatus.class.getResourceAsStream("/error-messages.properties")));
            } catch (IOException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

    }

}