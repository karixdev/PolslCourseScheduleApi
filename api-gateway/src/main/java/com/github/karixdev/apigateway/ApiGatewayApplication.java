package com.github.karixdev.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	RouteLocator routeLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r -> r.path("/api/schedules/**").uri("lb://schedule-service"))
				.route(r -> r.path("/api/webhooks/**").uri("lb://webhook-service"))
				.route(r -> r.path("/api/courses/**").uri("lb://course-service"))
				.route(r -> r.path("/api/notifications/**").uri("lb://notification-service"))

				.route(r -> r.path("/course-service/v3/api-docs").uri("lb://course-service"))
				.route(r -> r.path("/schedule-service/v3/api-docs").uri("lb://schedule-service"))
				.route(r -> r.path("/webhook-service/v3/api-docs").uri("lb://webhook-service"))
				.build();
	}

}
