package com.proxy.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

    @Bean
    RestClient weatherRestClient() {
      return RestClient.builder()
        .baseUrl("https://api.open-meteo.com/")
        .build();
    }
}
