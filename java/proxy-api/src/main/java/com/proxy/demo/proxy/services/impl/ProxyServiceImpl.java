package com.proxy.demo.proxy.services.impl;

import com.proxy.demo.proxy.exception.ProxyExceptions;
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

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.proxy.demo.proxy.exception.ProxyExceptions.ofUpstreamStatusCode;

@Slf4j
@Service
public class ProxyServiceImpl implements ProxyService {

  //TODO: siongel  RestClient + get messages and forward to client
  private final RestClient.Builder weatherRestClientBuilder;

  public ProxyServiceImpl(RestClient.Builder weatherRestClientBuilder) {
    this.weatherRestClientBuilder = weatherRestClientBuilder;
  }

  @Override
  @Cacheable(value = "weatherCache", key = "#longitude + ':' + #latitude")
  @Retryable(
      retryFor = { ResourceAccessException.class },
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public LookupResult loadWeatherData(double longitude, double latitude, Map<String, String> sourceParams) {
    try {
      LookupResult response = Optional.ofNullable( weatherRestClientBuilder.build().get()
          .uri(ofParams(sourceParams))
          .retrieve()
          .body(LookupResult.class))
          .orElseThrow(ProxyExceptions::notFound);

      log.info("Loaded {} {}", sourceParams, response);
      return response;
    } catch (RestClientResponseException ex) {
      throw ofUpstreamStatusCode(ex.getStatusCode());
    } catch (ResourceAccessException ex) {
      log.warn("Network error reaching upstream for params {}: {}", sourceParams, ex.getMessage());
      throw ex;
    }
  }

  private static Function<UriBuilder, URI> ofParams( Map<String, String> params ) {
    return uriBuilder -> {
      uriBuilder.path("/v1/forecast");
      params.forEach(uriBuilder::queryParam);
      return uriBuilder.build();
    };
  }
}
