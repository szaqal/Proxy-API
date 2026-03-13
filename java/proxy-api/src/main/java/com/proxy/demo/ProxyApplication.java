package com.proxy.demo;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

import static java.time.Duration.ofSeconds;

@SpringBootApplication
@EnableCaching
@EnableRetry
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Proxy API")
            .version("1.0")
            .description("Weather forecast proxy API"));
  }

  @Bean
  RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer() //TODO: deprecation fix
            )
        );

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(config)
        .withInitialCacheConfigurations(Map.of(
            "weatherCache",
            RedisCacheConfiguration.defaultCacheConfig().entryTtl(ofSeconds(60)) //Could be configurable
        ))
        .build();
  }

  /**
   * TODO: externally configurable env variable / config map
   * <ol>
   * <li>URL should be configurable</li>
   * <li>Timeouts configurable</li>
   * <li>Retry configuration</li>
   * </ol>
   */
  @Bean
  RestClient.Builder weatherRestClientBuilder(
      @Value("${WEATHER_API_BASE_URL:https://api.open-meteo.com/}") String baseUrl,
      @Value("${WEATHER_API_CONN_TIMEOUT:1s}") Duration connectTimeout,
      @Value("${WEATHER_API_READ_TIMEOUT:1s}") Duration readTimeout) {
    var requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(connectTimeout);
    requestFactory.setReadTimeout(readTimeout);

    return RestClient.builder()
      .requestFactory(requestFactory)
      .baseUrl(baseUrl);
  }


}
