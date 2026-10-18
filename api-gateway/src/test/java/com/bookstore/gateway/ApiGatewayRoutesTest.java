package com.bookstore.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

@SpringBootTest(properties = "eureka.client.enabled=false")
class ApiGatewayRoutesTest {

	@Autowired
	private RouteLocator routeLocator;

	@Test
	void routesApiPathsToServicesThroughTheLoadBalancer() {
		List<Route> routes = routeLocator.getRoutes().collectList().block();

		assertThat(routes)
				.extracting(route -> route.getId() + " -> " + route.getUri())
				.containsExactlyInAnyOrder(
						"book-service -> lb://book-service",
						"review-service -> lb://review-service");
	}
}
