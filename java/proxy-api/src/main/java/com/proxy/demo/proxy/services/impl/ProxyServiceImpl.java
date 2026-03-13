package com.proxy.demo.proxy.services.impl;

import com.proxy.demo.proxy.exception.FailedToLoadException;
import com.proxy.demo.proxy.services.api.LookupResult;
import com.proxy.demo.proxy.services.api.ProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
public class ProxyServiceImpl implements ProxyService {

  private final RestClient weatherRestClient;

  public ProxyServiceImpl(RestClient weatherRestClient) {
    this.weatherRestClient = weatherRestClient;
  }

  @Override
  @Cacheable(value = "weatherCache", key = "#longitude + ':' + #latitude")
  @Retryable(
      retryFor = { ResourceAccessException.class, ConnectException.class, SocketTimeoutException.class },
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public LookupResult loadWeatherData(double longitude, double latitude) {
    try {
      LookupResult response = Optional.ofNullable(weatherRestClient.get()
          .uri(ofParams(longitude, latitude))
          .retrieve()
          .body(LookupResult.class))
          .orElseThrow(FailedToLoadException::unavailable);

      log.info("Loaded {}", response);
      return response;
    } catch (RestClientResponseException ex) {
      throw FailedToLoadException.ofRestClientException(ex);
    } catch (ResourceAccessException ex) {
      log.warn("Network error reaching upstream for params {}:", ex.getMessage());
      throw ex;
    }
  }

  private static Function<UriBuilder, URI> ofParams(double longitude, double latitude) {
    return uriBuilder -> {
      uriBuilder.path("/v1/forecast")
          .queryParam("latitude", latitude)
          .queryParam("longitude", longitude)
          .queryParam("current", "temperature_2m,wind_speed_10m");
      return uriBuilder.build();
    };
  }
}
