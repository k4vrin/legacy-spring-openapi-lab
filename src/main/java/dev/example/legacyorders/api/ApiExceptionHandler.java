package dev.example.legacyorders.api;

import dev.example.legacyorders.order.OrderNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackageClasses = OrderController.class)
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidRequest(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted().collect(Collectors.toList());
        return ResponseEntity.badRequest().body(new ApiError("INVALID_ORDER", "Request validation failed", details));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> malformedRequest() {
        return ResponseEntity.badRequest().body(
                new ApiError("INVALID_REQUEST", "Request could not be read", Collections.emptyList()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> orderNotFound() {
        return ResponseEntity.status(404).body(
                new ApiError("ORDER_NOT_FOUND", "Order not found", Collections.emptyList()));
    }
}
