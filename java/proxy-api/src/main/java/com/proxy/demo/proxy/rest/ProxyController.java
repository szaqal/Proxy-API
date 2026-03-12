package com.proxy.demo.proxy.rest;

import com.proxy.demo.proxy.services.api.ProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProxyController {

  private final ProxyService proxyService;

  @GetMapping("/forecast")
  public String forecast(@RequestParam Map<String, String> params) {
    proxyService.loadWeatherData(params);
    return "OK";
  }
}
