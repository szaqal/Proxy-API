package com.proxy.demo.proxy.rest;

import com.proxy.demo.proxy.exception.ProxyExceptions;
import com.proxy.demo.proxy.services.api.LookupResult;
import com.proxy.demo.proxy.services.api.ProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Tag(name = "Weather Forecast", description = "Proxy API for weather data")
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
  @Operation(summary = "Get weather forecast", description = "Returns weather data for given coordinates")
  @GetMapping("/forecast")
  public Response forecast(
      @Parameter(description = "Latitude of the location") @RequestParam Map<String, String> params) {

    Double latitude = Optional.ofNullable(params.get("latitude"))
        .map(Double::parseDouble)
        .orElseThrow(ProxyExceptions::missingLatitude);

    Double longitude = Optional.ofNullable(params.get("longitude"))
        .map(Double::parseDouble)
        .orElseThrow(ProxyExceptions::missingLongitude);

    return Optional.ofNullable(proxyService.loadWeatherData(longitude, latitude, params))
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
