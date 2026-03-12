package com.proxy.demo.proxy.rest;

import com.proxy.demo.proxy.exception.ProxyExceptions;
import com.proxy.demo.proxy.services.api.LookupResult;
import com.proxy.demo.proxy.services.api.ProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ProxyController {

  private final ProxyService proxyService;

  /**
   * Task doesn't cleary states what this API should accept as input and how to validate hence
   * given it's Proxy it is assumed to forward anything as is to origin service,
   * so instead accepting particular parameters we're getting map here, otherwise we could use @Valid and
   * java validation API.
   *
   * We can be sure that either  latitude/longitude are required.
   */
  @GetMapping("/forecast")
  public Response forecast(@RequestParam Map<String, String> params) {
    if(!params.containsKey("latitude") || !params.containsKey("longitude")) {
      throw new ProxyExceptions.InvalidRequestException("Missing either longitude or latitude");
    }

    LookupResult value = proxyService.loadWeatherData(params);
    return Optional.ofNullable(value)
        .map(this::asResponse)
        .orElseThrow(ProxyExceptions::notFound);
  }

  private Response asResponse(LookupResult lookupResult) {
    if(lookupResult.getCurrent() == null) {
      throw ProxyExceptions.notFound();
    }

    return new Response("open-meteo", Instant.now(),
            new Current(lookupResult.getCurrent().getTemperature(), lookupResult.getCurrent().getWindSpeed()));
  }
}
