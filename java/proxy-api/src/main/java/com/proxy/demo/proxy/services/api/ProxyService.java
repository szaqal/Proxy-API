package com.proxy.demo.proxy.services.api;

import java.util.Map;

public interface ProxyService {

  LookupResult loadWeatherData(double longitude, double latitude, Map<String, String> sourceParams);
}
