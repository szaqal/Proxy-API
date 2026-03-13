package com.proxy.demo.proxy.exception;

import org.springframework.http.HttpStatusCode;

public class ProxyExceptions {

  public static NotAvailableException notAvailable() {
    return notAvailable(null);
  }

  public static NotAvailableException notAvailable(String message) {
    return new NotAvailableException(message);
  }

  //TODO: differentiate message
  public static InvalidRequestException missingLongitude() {
    return new InvalidRequestException("Missing either longitude or latitude");
  }

  public static InvalidRequestException missingLatitude() {
    return new InvalidRequestException("Missing either longitude or latitude");
  }

  public static UpstreamServerErrorException upstreamServerErrorException() {
    return new UpstreamServerErrorException();
  }

  public static InvalidRequestException invalidRequestException( String message ) {
    return new InvalidRequestException(message);
  }

  public static RuntimeException ofUpstreamStatusCode( HttpStatusCode statusCode ) {
    if (statusCode.is4xxClientError()) {
      throw invalidRequestException("Upstream rejected the request: %s".formatted(statusCode));
    } else if (statusCode.is5xxServerError()) {
      throw upstreamServerErrorException();
    }
    return new RuntimeException("Unrecognized upstream exception");
  }

  public static class NotAvailableException extends RuntimeException {
    public NotAvailableException(String message) {
      super(message);
    }
  }

  public static class InvalidRequestException extends RuntimeException {
    public InvalidRequestException( String message ) {
      super(message);
    }
  }

  public static class UpstreamServerErrorException extends RuntimeException {
    public UpstreamServerErrorException() {
      super("Upstream returned server error");
    }
  }
}
