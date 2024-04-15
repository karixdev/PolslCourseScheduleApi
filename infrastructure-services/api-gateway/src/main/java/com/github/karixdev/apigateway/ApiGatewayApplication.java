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

	private static final String SCHEDULE_SERVICE = "lb://schedule-service";
	private static final String COURSE_SERVICE = "lb://course-service";

	@Bean
	RouteLocator routeLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r -> r.path("/api/admin/schedules/**").uri(SCHEDULE_SERVICE))
				.route(r -> r.path("/api/schedules/**").uri(SCHEDULE_SERVICE))
				.route(r -> r.path("/schedule-service/v3/api-docs").uri(SCHEDULE_SERVICE))

				.route(r -> r.path("/api/courses/**").uri(COURSE_SERVICE))
				.route(r -> r.path("/api/admin/courses/**").uri(COURSE_SERVICE))
				.route(r -> r.path("/course-service/v3/api-docs").uri(COURSE_SERVICE))

				.build();
	}

}
