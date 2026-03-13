package com.proxy.demo.proxy.rest;

import com.proxy.demo.proxy.exception.FailedToLoadException;
import com.proxy.demo.proxy.services.impl.ProxyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.proxy.demo.proxy.exception.FailedToLoadException.Reason.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.ConnectException;
import java.util.List;
import java.util.stream.Stream;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProxyControllerTest {

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

  @Autowired
  private RestClient.Builder weatherRestClientBuilder;

  @Autowired
  private ProxyServiceImpl proxyService;

  private MockMvc mockMvc;
  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockServer = MockRestServiceServer.bindTo(weatherRestClientBuilder).build();
    RestClient mockClient = weatherRestClientBuilder.build();
    ReflectionTestUtils.setField(proxyService, "weatherRestClient", mockClient);
  }


  @ParameterizedTest(name="{0}")
  @MethodSource("testCases")
  void test(String name, TestCase testcase) throws Exception {


    mockServer.expect(testcase.upstreamCallCount(), testcase.upstreamRequest()).andRespond(testcase.upstreamResponse());
    ResultActions perform = mockMvc.perform(testcase.getApiRequest());
    for(var xxx: testcase.expectedApiResponseChecks()) {
      perform.andExpect(xxx);
    }
    mockServer.verify();
  }

  public static Stream<Arguments> testCases() {
    return Stream.of(
        Arguments.of("Should return 500 on upstream 500 error", new Upstream500ErrTestcase()),
        Arguments.of("Should return 404 on upstream returned empty response {}", new UpstreamEmptyResponse()),
        Arguments.of("Should return 400 on missing longitude", new MissingLongitude()),
        Arguments.of("Should return 400 on missing latitude", new MissingLatitude()),
        Arguments.of("Should return 200 with data", new ValidUpstreamResponse()),
        Arguments.of("Should return 504 on upstream timeout", new UpstreamTimeout())
    );
  }


  abstract static class TestCase {


    abstract MockHttpServletRequestBuilder getApiRequest();
    abstract List<ResultMatcher> expectedApiResponseChecks();


    ExpectedCount upstreamCallCount() {
      return ExpectedCount.once();
    }

    ResponseCreator upstreamResponse() {
      return withSuccess(); //defaults to success
    }

    RequestMatcher meteoUpstream() {
      return requestTo(containsString("https://api.open-meteo.com/v1/forecast"));
    }

    RequestMatcher upstreamRequest() {
      return meteoUpstream();
    }

    ResultMatcher errorType( FailedToLoadException.Reason reason ) {
      return jsonPath("$.error").value(reason.name());
    }

    ResultMatcher errorMessage(String message) {
      return jsonPath("$.message").value(message);
    }

    ResultMatcher serverError() {
      return status().isInternalServerError();
    }

    ResultMatcher notFound() {
      return status().isNotFound();
    }

    ResultMatcher badRequest() {
      return status().isBadRequest();
    }

    ResultMatcher gatewayTimeout() {
      return status().isGatewayTimeout();
    }

    ResultMatcher isJson() {
      return content().contentType(APPLICATION_JSON);
    }



    MockHttpServletRequestBuilder request(Double latitude, Double longitude) {
      MockHttpServletRequestBuilder request = get("/forecast");
      if(latitude != null) {
        request.param("latitude", String.valueOf(latitude));
      }
      if(longitude != null) {
        request.param("longitude", String.valueOf(longitude));
      }
      return request;
    }
  }

  //----

  private static class MissingLongitude extends TestCase {

    @Override
    MockHttpServletRequestBuilder getApiRequest() {
      return request(0.0, null);
    }

    @Override
    List<ResultMatcher> expectedApiResponseChecks() {
      return List.of(isJson(), badRequest(), errorType(INVALID_REQUEST), errorMessage("Unable to load weather data - invalid longitude"));
    }

    @Override
    ExpectedCount upstreamCallCount() {
      return ExpectedCount.never();
    }
  }

  private static class MissingLatitude extends TestCase {

    @Override
    MockHttpServletRequestBuilder getApiRequest() {
      return request(null, 0.0);
    }

    @Override
    List<ResultMatcher> expectedApiResponseChecks() {
      return List.of(isJson(), badRequest(), errorType(INVALID_REQUEST), errorMessage("Unable to load weather data - invalid latitude"));
    }

    @Override
    ExpectedCount upstreamCallCount() {
      return ExpectedCount.never();
    }
  }

  //----

  private static class UpstreamEmptyResponse extends TestCase {

    @Override
    MockHttpServletRequestBuilder getApiRequest() {
      return request(0.0, 0.0);
    }

    @Override
    List<ResultMatcher> expectedApiResponseChecks() {
      return List.of(isJson(), notFound(), errorType(UNAVAILABLE), errorMessage("Unable to load weather data - data not available"));
    }

    @Override
    DefaultResponseCreator upstreamResponse() {
      return withSuccess("{}", APPLICATION_JSON);
    }
  }

  //----

  private static class Upstream500ErrTestcase extends TestCase {

    @Override
    public MockHttpServletRequestBuilder getApiRequest() {
      return request(1.11, 2.22);
    }

    @Override
    public List<ResultMatcher> expectedApiResponseChecks() {
      return List.of(isJson(), serverError(), errorType(UPSTREAM_SERVER_ERROR), errorMessage("Unable to load weather data"));
    }

    @Override
    public DefaultResponseCreator upstreamResponse() {
      return withServerError();
    }
  }

  //----

  private static class ValidUpstreamResponse extends TestCase {

    @Override
    MockHttpServletRequestBuilder getApiRequest() {
      return request(52.52, 13.41);
    }

    @Override
    List<ResultMatcher> expectedApiResponseChecks() {
      return List.of();
    }

    @Override
    DefaultResponseCreator upstreamResponse() {
      return withSuccess("""
        {
          "current": {
            "temperature_2m": 22.5,
            "wind_speed_10m": 15.3
          }
        }
        """, APPLICATION_JSON);
    }
  }

  //----

  private static class UpstreamTimeout extends TestCase {

    @Override
    MockHttpServletRequestBuilder getApiRequest() {
      return request(52.53, 13.42);
    }

    @Override
    ResponseCreator upstreamResponse() {
      return withException(new ConnectException("Connection refused"));
    }

    @Override
    ExpectedCount upstreamCallCount() {
      return ExpectedCount.times(3);
    }

    @Override
    List<ResultMatcher> expectedApiResponseChecks() {
      return List.of(isJson(), gatewayTimeout(), jsonPath("$.error").value("Gateway Timeout"), errorMessage("Request to upstream service timed out"));
    }
  }
}
