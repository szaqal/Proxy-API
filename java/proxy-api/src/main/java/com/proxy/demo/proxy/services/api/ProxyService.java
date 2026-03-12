package com.proxy.demo.proxy.services.api;

import java.util.Map;

public interface ProxyService {

  LookupResult loadWeatherData( Map<String, String> params);
}
