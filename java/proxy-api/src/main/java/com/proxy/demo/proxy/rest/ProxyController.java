package com.proxy.demo.proxy.rest;

import com.proxy.demo.proxy.exception.FailedToLoadException;
import com.proxy.demo.proxy.services.api.LookupResult;
import com.proxy.demo.proxy.services.api.ProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Validated
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
      @Parameter(description = "Latitude of the location", required = true)
      @RequestParam @Min(-90) @Max(90) Double latitude,
      @Parameter(description = "Longitude of the location", required = true)
      @RequestParam @Min(-180) @Max(180) Double longitude) {

    return Optional.ofNullable(proxyService.loadWeatherData(longitude, latitude))
        .map(this::asResponse)
        .orElseThrow(FailedToLoadException::unavailable);
  }

  private Response asResponse(LookupResult lookupResult) {
    return Optional.ofNullable(lookupResult.getCurrent())
        .map(ProxyController::getResponse)
        .orElseThrow(FailedToLoadException::unavailable);
  }

  private static Response getResponse( LookupResult.Current lookupCurrent ) {
    return new Response("open-meteo", Instant.now(), new Current(lookupCurrent.getTemperature(), lookupCurrent.getWindSpeed()));
  }
}
