package com.radman.shop.common;

import lombok.Getter;

import java.io.IOException;
import java.util.Properties;

@Getter
public enum ResultStatus {

    SUCCESS(0, "success"), UNKNOWN(1, "unknown.error"), FAILURE(2, "failure"),
    USER_DUPLICATE_USERNAME(3, "user.duplicate.username"), INVALID_PARAMETER(4, "core.invalid.parameter.exception"),
    FORBIDDEN_REQUEST(5, "forbidden.request"),
    GROUP_NOT_FOUND(5, "group.not.found"),
    EXPENSE_NOT_FOUND(5, "expense.not.found"),
    USER_NOT_FOUND(6, "user.not.found");

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