package com.proxy.demo.proxy.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//TODO: test of timeouts
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProxyControllerTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private RestClient.Builder weatherRestClientBuilder;

  private MockMvc mockMvc;
  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockServer = MockRestServiceServer.bindTo(weatherRestClientBuilder).build();
  }

  @Test
  void forecast_shouldReturn200WithWeatherData() throws Exception {
    // Given
    String jsonResponse = """
        {
          "current": {
            "temperature_2m": 22.5,
            "wind_speed_10m": 15.3
          }
        }
        """;

    mockServer.expect(requestTo(containsString("https://api.open-meteo.com/v1/forecast")))
        .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

    // When & Then
    mockMvc.perform(get("/forecast")
            .param("latitude", "52.52")
            .param("longitude", "13.41")
            .param("current", "temperature_2m,wind_speed_10m")
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.source").value("open-meteo"))
        .andExpect(jsonPath("$.current.temperatureC").value(22.5))
        .andExpect(jsonPath("$.current.windSpeedKmh").value(15.3));

    mockServer.verify();
  }

  @Test
  void forecast_shouldReturn400WhenMissingLatitude() throws Exception {
    // When & Then
    mockMvc.perform(get("/forecast")
            .param("longitude", "13.41"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Missing either longitude or latitude"));
  }

  @Test
  void forecast_shouldReturn400WhenMissingLongitude() throws Exception {
    // When & Then
    mockMvc.perform(get("/forecast")
            .param("latitude", "52.52"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Missing either longitude or latitude"));
  }

  @Test
  void forecast_shouldReturn404WhenNoDataFound() throws Exception {
    // Given
    mockServer.expect(requestTo(containsString("https://api.open-meteo.com/v1/forecast")))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    // When & Then
    mockMvc.perform(get("/forecast")
            .param("latitude", "0")
            .param("longitude", "0")
            .param("current","temperature_2m,wind_speed_10m")
        )
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Resource not found"));

    mockServer.verify();
  }

}
