package com.proxy.demo.proxy.services.impl;

import com.proxy.demo.proxy.services.api.LookupResult;
import com.proxy.demo.proxy.services.api.ProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class ProxyServiceImpl implements ProxyService {

  private final RestClient.Builder weatherRestClientBuilder;

  public ProxyServiceImpl(RestClient.Builder weatherRestClientBuilder) {
    this.weatherRestClientBuilder = weatherRestClientBuilder;
  }

  @Override
  @Cacheable(value = "weatherCache", key = "#longitude + ':' + #latitude")
  @Retryable(
      retryFor = { ConnectException.class, SocketException.class },
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public LookupResult loadWeatherData(double longitude, double latitude, Map<String, String> sourceParams) {
    LookupResult response = weatherRestClientBuilder.build().get()
        .uri(ofParams(sourceParams))
        .retrieve()
        .body(LookupResult.class);

    log.info("Loaded {} {}", sourceParams, response);
    return response;
  }

  private static Function<UriBuilder, URI> ofParams( Map<String, String> params ) {
    return uriBuilder -> {
      uriBuilder.path("/v1/forecast");
      params.forEach(uriBuilder::queryParam);
      return uriBuilder.build();
    };
  }
}
