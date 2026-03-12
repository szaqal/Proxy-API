package com.proxy.demo.proxy.services.impl;

import com.proxy.demo.proxy.services.api.LookupResult;
import com.proxy.demo.proxy.services.api.ProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyServiceImpl implements ProxyService {

  private final RestClient weatherRestClient;


  @Override
  public LookupResult loadWeatherData(Map<String, String> params) {
    LookupResult response = weatherRestClient.get()
        .uri(ofParams(params))
        .retrieve()
        .body(LookupResult.class);

    log.info("Loaded {} {}", params, response);
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
