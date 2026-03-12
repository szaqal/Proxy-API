package com.proxy.demo.proxy.rest;

import com.proxy.demo.proxy.services.api.ProxyService;
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

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProxyControllerTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private ProxyService proxyService;

  @Autowired
  private RestClient.Builder restClientBuilder;

  private MockMvc mockMvc;
  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
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

    mockServer.expect(requestTo("https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41"))
        .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

    // When & Then
    mockMvc.perform(get("/forecast")
            .param("latitude", "52.52")
            .param("longitude", "13.41"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.provider").value("open-meteo"))
        .andExpect(jsonPath("$.current.temperature").value(22.5))
        .andExpect(jsonPath("$.current.windSpeed").value(15.3));

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
    mockServer.expect(requestTo("https://api.open-meteo.com/v1/forecast?latitude=0&longitude=0"))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    // When & Then
    mockMvc.perform(get("/forecast")
            .param("latitude", "0")
            .param("longitude", "0"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Resource not found"));

    mockServer.verify();
  }
}
