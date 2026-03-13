package com.proxy.demo.proxy.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClientResponseException;

import static com.proxy.demo.proxy.exception.FailedToLoadException.Reason.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
public class FailedToLoadException extends RuntimeException {


  @AllArgsConstructor
  public enum Reason {
    // We've recieved invalid request
    INVALID_REQUEST(BAD_REQUEST),
    UPSTREAM_SERVER_ERROR(INTERNAL_SERVER_ERROR),
    // Uncategorized error.
    UPSTREAM_GENERAL_ERROR(NOT_FOUND),
    // Our backend sent invalid request
    UPSTREAM_INVALID_REQUEST(BAD_REQUEST),
    // Whatever else happened
    UNAVAILABLE(NOT_FOUND);

    @Getter
    final HttpStatus status;

  };

  @Getter
  private final Reason reason;

  private FailedToLoadException(Reason reason) {
    super("Unable to load weather data");
    this.reason = reason;
  }

  private FailedToLoadException(Reason reason, String message) {
    super(message);
    this.reason = reason;
  }

  public static FailedToLoadException upstreamGeneralError() {
    return new FailedToLoadException(UPSTREAM_GENERAL_ERROR);
  }

  public static FailedToLoadException upstreamServerError() {
    return new FailedToLoadException(UPSTREAM_SERVER_ERROR);
  }

  public static FailedToLoadException upstreamInvalidRequest() {
    return new FailedToLoadException(UPSTREAM_INVALID_REQUEST);
  }

  public static FailedToLoadException unavailable() {
    return new FailedToLoadException(UNAVAILABLE, "Unable to load weather data - data not available");
  }

  public static FailedToLoadException invalidLongitude() {
    return new FailedToLoadException(INVALID_REQUEST, "Unable to load weather data - invalid longitude");
  }

  public static FailedToLoadException invalidLatitude() {
    return new FailedToLoadException(INVALID_REQUEST, "Unable to load weather data - invalid latitude");
  }

  public static RuntimeException ofRestClientException( RestClientResponseException e ) {

    /* TODO: We could be more specific there (not only 4xx and 5xx) to apply different strategies based on retirability etc...
     */
    HttpStatusCode statusCode = e.getStatusCode();
    log.warn("Upstream retuned error {}", e.getMessage());
    if (statusCode.is4xxClientError()) {
      throw upstreamInvalidRequest();
    } else if (statusCode.is5xxServerError()) {
      throw upstreamServerError();
    }
    return upstreamGeneralError();
  }
}
