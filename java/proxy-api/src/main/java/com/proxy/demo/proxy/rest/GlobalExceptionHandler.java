package com.proxy.demo.proxy.rest;

import com.proxy.demo.proxy.exception.FailedToLoadException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {



  @ExceptionHandler(ResourceAccessException.class)
  public ResponseEntity<Map<String, Object>> handleResourceAccessException(ResourceAccessException ex) {
    Map<String, Object> body = Map.of(
        "error", "Gateway Timeout",
        "message", "Request to upstream service timed out",
        "timestamp", Instant.now().toString()
    );
    log.debug("Upstream service timeout: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(body);
  }

  @ExceptionHandler(FailedToLoadException.class)
  public ResponseEntity<Map<String, Object>> handleFailedToLoadException(FailedToLoadException ex) {
    Map<String, Object> body = Map.of(
        "error", ex.getReason(), // if that itself would be considered as some internal exposed then some codes  could be returned
        "message", ex.getMessage(),
        "timestamp", Instant.now().toString()
    );
    return ResponseEntity.status(ex.getReason().getStatus()).body(body);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
    String message = ex.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .findFirst()
        .orElse("Validation error");
    
    Map<String, Object> body = Map.of(
        "error", FailedToLoadException.Reason.INVALID_REQUEST.name(),
        "message", message,
        "timestamp", Instant.now().toString()
    );
    log.debug("Validation error: {}", message);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }


}
