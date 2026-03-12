package com.proxy.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static java.time.Duration.ofSeconds;

@SpringBootApplication
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

  /**
   * TODO: externally configurable env variable / config map
   * <ol>
   * <li>URL should be configurable</li>
   * <li>Timeouts configurable</li>
   * </ol>
   */
  @Bean
  RestClient.Builder weatherRestClientBuilder() {
    var requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(ofSeconds(1));
    requestFactory.setReadTimeout(ofSeconds(1));

    // If performance over debugging is valued mode we could use Reactive WebClient too.
    return RestClient.builder()
      .requestFactory(requestFactory)
      .baseUrl("https://api.open-meteo.com/");
  }


}
