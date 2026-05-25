package com.radman.shop.common.handler;

import com.radman.shop.common.GeneralResponse;
import com.radman.shop.common.ResponseService;
import com.radman.shop.common.ResultStatus;
import com.radman.shop.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.security.InvalidParameterException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Environment environment;

    @ExceptionHandler(BusinessException.class)
    public final ResponseEntity<ResponseService> handleBusinessException(BusinessException ex, WebRequest request) {
        logger.error("business exception occurred", ex);

        return ResponseEntity
                .status(resolveStatus(ex.getResultStatus()))
                .body(new GeneralResponse(ex.getResultStatus()));
    }

    @ExceptionHandler(InvalidParameterException.class)
    public final ResponseEntity<ResponseService> handleInvalidParameterException(InvalidParameterException ex) {
        logger.error("invalid param error", ex);

        return ResponseEntity
                .unprocessableEntity()
                .body(new GeneralResponse(ResultStatus.INVALID_PARAMETER));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public final ResponseEntity<ResponseService> handleUnsupportedOperationException(
            UnsupportedOperationException ex
    ) {

        logger.error(ResultStatus.FORBIDDEN_REQUEST.getDescription(), ex);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new GeneralResponse(ResultStatus.FORBIDDEN_REQUEST));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<ResponseService> handleConstraintViolationException(
            ConstraintViolationException ex
    ) {

        logger.warn("constraint violation exception", ex);

        var response = new GeneralResponse(ResultStatus.INVALID_PARAMETER);

        ex.getConstraintViolations()
                .stream()
                .findFirst()
                .ifPresent(violation ->
                        response.setResult(
                                ResultStatus.INVALID_PARAMETER,
                                environment.getProperty(violation.getMessage())
                        )
                );

        return ResponseEntity
                .unprocessableEntity()
                .body(response);
    }

    @ExceptionHandler(Throwable.class)
    public final ResponseEntity<ResponseService> handleGeneralException(
            Throwable throwable
    ) {

        logger.error(ResultStatus.UNKNOWN.getDescription(), throwable);

        return ResponseEntity.unprocessableEntity().body(new GeneralResponse(ResultStatus.UNKNOWN));
    }

    private HttpStatus resolveStatus(ResultStatus resultStatus) {

        return switch (resultStatus) {

            case CART_NOT_FOUND, PRODUCT_NOT_FOUND, CART_ITEM_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case DUPLICATE_PRODUCT -> HttpStatus.CONFLICT;

            case FORBIDDEN_REQUEST -> HttpStatus.FORBIDDEN;

            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }
}