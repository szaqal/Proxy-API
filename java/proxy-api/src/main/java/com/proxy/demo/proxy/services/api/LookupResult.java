package com.proxy.demo.proxy.services.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class LookupResult implements Serializable {

  @JsonProperty("current")
  private Current current;

  @Data
  public static class Current implements Serializable {

    @JsonProperty("temperature_2m")
    private double temperature;

    @JsonProperty("wind_speed_10m")
    private double windSpeed;
  }
}
