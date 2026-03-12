package com.proxy.demo.proxy.exception;

public class ProxyExceptions {

  public static NotFoundException notFound() {
    return new NotFoundException();
  }


  //TODO: differentiate message
  public static InvalidRequestException missingLongitude() {
    return new InvalidRequestException("Missing either longitude or latitude");
  }


  public static InvalidRequestException missingLatitude() {
    return new InvalidRequestException("Missing either longitude or latitude");
  }

  public static UpstreamException upstreamException() {
    return new UpstreamException();
  }


  public static InvalidRequestException invalidRequestException(String message) {
    return new InvalidRequestException(message);
  }




  public static class NotFoundException extends RuntimeException { }

  public static class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
      super(message);
    }
  }

  public static class UpstreamException extends RuntimeException {
    public UpstreamException() {
      super("Upstream returned server error");
    }
  }
}
