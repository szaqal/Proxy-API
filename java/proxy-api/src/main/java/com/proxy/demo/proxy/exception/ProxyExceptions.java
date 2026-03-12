package com.proxy.demo.proxy.exception;

public class ProxyExceptions {

  public static NotFound notFound() {
    return new NotFound();
  }

  public static InvalidRequestException invalidRequestException(String message) {
    return new InvalidRequestException(message);
  }


  public static class NotFound extends RuntimeException { }

  public static class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
      super(message);
    }
  }
}
