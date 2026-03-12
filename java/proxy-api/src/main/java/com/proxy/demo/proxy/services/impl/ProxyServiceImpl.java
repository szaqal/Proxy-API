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

  @Value("${OPEN_METEO_API_FORECAST_URL:https://api.open-meteo.com/v1/forecast}")
  private String apiUrl;

  private final RestClient restClient;

  public ProxyServiceImpl(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.build();
  }

  @Override
  public void loadWeatherData(Map<String, String> params) {
    String response = restClient.get()
        .uri(uriBuilder -> {
          uriBuilder.path(apiUrl);
          params.forEach(uriBuilder::queryParam);
          return uriBuilder.build();
        })
        .retrieve()
        .body(String.class);

    log.info("Loaded {} {} {}", apiUrl, params, response);
  }
}
