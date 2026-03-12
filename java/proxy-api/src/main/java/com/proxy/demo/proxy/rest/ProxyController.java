package com.proxy.demo.proxy.rest;

import com.proxy.demo.proxy.services.api.ProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class ProxyController {

  private final ProxyService proxyService;

  @GetMapping("/aaa")
  public String test() {
    proxyService.loadWeatherData();
    return "OK";
  }
}
