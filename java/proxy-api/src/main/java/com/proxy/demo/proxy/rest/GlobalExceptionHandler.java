package com.proxy.demo.proxy.rest;

import com.proxy.demo.proxy.exception.ProxyExceptions;
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

  @ExceptionHandler(ProxyExceptions.InvalidRequestException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidRequest(ProxyExceptions.InvalidRequestException ex) {
    Map<String, Object> body = Map.of(
        "error", "Bad Request",
        "message", ex.getMessage(),
        "timestamp", Instant.now().toString()
    );
    log.debug(ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(ProxyExceptions.NotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(ProxyExceptions.NotFoundException ex) {
    Map<String, Object> body = Map.of(
        "error", "Not Found",
        "message", "Resource not found",
        "timestamp", Instant.now().toString()
    );
    log.debug(ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

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
}
