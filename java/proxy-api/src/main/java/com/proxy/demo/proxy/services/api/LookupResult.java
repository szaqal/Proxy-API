package com.proxy.demo.proxy.services.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LookupResult {

  @JsonProperty("current")
  private Current current;

  @Data
  public static class Current {

    @JsonProperty("temperature_2m")
    private double temperature;

    @JsonProperty("wind_speed_10m")
    private double windSpeed;
  }
}
