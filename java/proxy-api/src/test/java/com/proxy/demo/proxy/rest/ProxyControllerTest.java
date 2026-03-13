package com.proxy.demo.proxy.rest;

import com.proxy.demo.proxy.services.impl.ProxyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.ConnectException;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProxyControllerTest {

  /*
   * Root cause of the original test failure:
   *
   * ProxyServiceImpl injects the already-built RestClient singleton.
   * MockRestServiceServer.bindTo(RestClient.Builder) only installs its intercepting
   * factory *on the builder*, not on the finished client.  The production context
   * builds weatherRestClient at startup from a prototype RestClient.Builder, so any
   * builder instance the test auto-wires is a different copy — the mock never sees
   * outgoing requests.
   *
   * Fix: after calling MockRestServiceServer.bindTo(builder).build() (which mutates
   * the builder's request-factory), we call builder.build() again to produce a *new*
   * RestClient that uses the mock factory, then inject it directly into the service
   * via ReflectionTestUtils.  This avoids touching production code and sidesteps
   * Spring's bean-overriding restrictions introduced in Spring Boot 4 / Spring 7.
   */

  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
      .withExposedPorts(6379);

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
  }

  @Autowired
  private WebApplicationContext webApplicationContext;

  /**
   * The prototype builder registered by ProxyApplication.  MockRestServiceServer
   * mutates this builder's request-factory; we then re-build from it so the service
   * receives a client that routes through the mock.
   */
  @Autowired
  private RestClient.Builder weatherRestClientBuilder;

  /** The service whose internal RestClient we replace before each test. */
  @Autowired
  private ProxyServiceImpl proxyService;

  private MockMvc mockMvc;
  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    // 1. Bind the mock to the shared builder — this replaces the builder's
    //    request-factory with MockRestServiceServer's intercepting factory.
    mockServer = MockRestServiceServer.bindTo(weatherRestClientBuilder).build();

    // 2. Build a fresh RestClient from the now-mutated builder and inject it
    //    into the service so that every HTTP call goes through the mock.
    RestClient mockClient = weatherRestClientBuilder.build();
    ReflectionTestUtils.setField(proxyService, "weatherRestClient", mockClient);
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

    mockServer.expect(meteoRequest())
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
    mockServer.expect(meteoRequest())
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    // When & Then
    mockMvc.perform(get("/forecast")
            .param("latitude", "10")
            .param("longitude", "10")
            .param("current","temperature_2m,wind_speed_10m")
        )
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Resource not found"));

    mockServer.verify();
  }

  @Test
  void forecast_shouldReturn404WhenNoCurrentProvided() throws Exception {
    // Given
    mockServer.expect(meteoRequest())
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    // When & Then
    mockMvc.perform(get("/forecast")
            .param("latitude", "0")
            .param("longitude", "0")
        )
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Resource not found"));

    mockServer.verify();
  }


  @Test
  void forecast_shouldReturn504WhenConnectionTimeout() throws Exception {
    //with retries
    mockServer.expect(ExpectedCount.times(3), meteoRequest())
        .andRespond(withException(new ConnectException("Connection refused")));

    mockMvc.perform(get("/forecast")
            .param("latitude", "52.53")
            .param("longitude", "13.42")
            .param("current", "temperature_2m,wind_speed_10m")
        )
        .andExpect(status().isGatewayTimeout())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Gateway Timeout"))
        .andExpect(jsonPath("$.message").value("Request to upstream service timed out"));

    mockServer.verify();
  }



  @Test
  void forecast_shouldReturn500WhenUpstreamReturns5xx() throws Exception {
    // Given — upstream responds with a 500; no retries expected (only network errors are retried)
    mockServer.expect(ExpectedCount.once(), meteoRequest())
        .andRespond(withServerError());

    // When & Then
    mockMvc.perform(get("/forecast")
            .param("latitude", "1.11")
            .param("longitude", "2.22")
            .param("current", "temperature_2m,wind_speed_10m")
        )
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Upstream server error"))
        .andExpect(jsonPath("$.message").value("Upstream server error"));

    mockServer.verify();
  }

  private static RequestMatcher meteoRequest() {
    return requestTo(containsString("https://api.open-meteo.com/v1/forecast"));
  }
}
