package com.proxy.demo.proxy.services.impl;

import com.proxy.demo.proxy.services.api.ProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProxyServiceImpl implements ProxyService {

  @Value("${OPEN_METEO_API_FORECAST_URL:https://api.open-meteo.com/v1/forecast}")
  private String apiUrl;

  @Override
  public void loadWeatherData() {
    log.info("Loaded {}", apiUrl);
  }
}
