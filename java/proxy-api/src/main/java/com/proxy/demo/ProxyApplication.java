package com.proxy.demo;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
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
            .description("Weather forecast proxy API")
            .contact(new Contact().name("Demo").email("demo@example.com")));
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
  RestClient.Builder weatherRestClientBuilder() {
    var requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(ofSeconds(1));
    requestFactory.setReadTimeout(ofSeconds(1));

    return RestClient.builder()
      .requestFactory(requestFactory)
      .baseUrl("https://api.open-meteo.com/");
  }


}
