package com.bookstore.webui.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	/**
	 * {@code @LoadBalanced} lets clients built from this builder use Eureka service names
	 * (e.g. {@code http://api-gateway}) instead of fixed hosts.
	 */
	@Bean
	@LoadBalanced
	RestClient.Builder loadBalancedRestClientBuilder() {
		return RestClient.builder();
	}
}
