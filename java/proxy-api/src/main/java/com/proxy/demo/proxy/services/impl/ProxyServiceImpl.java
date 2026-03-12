package com.proxy.demo.proxy.services.impl;

import com.proxy.demo.proxy.services.api.ProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class ProxyServiceImpl implements ProxyService {

  private final RestClient restClient;

  public ProxyServiceImpl() {
    this.restClient = RestClient.builder()
        .baseUrl("https://api.open-meteo.com/")
        .build();
  }

  @Override
  public void loadWeatherData(Map<String, String> params) {
    String response = restClient.get()
        .uri(uriBuilder -> {
          uriBuilder.path("/v1/forecast");
          params.forEach(uriBuilder::queryParam);
          return uriBuilder.build();
        })
        .retrieve()
        .body(String.class);

    log.info("Loaded {} {}", params, response);
  }
}
