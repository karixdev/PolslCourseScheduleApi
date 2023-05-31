package com.github.karixdev.courseservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class SwaggerConfig {
    static {
        Schema<LocalTime> schema = new Schema<>();
        schema.example(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        SpringDocUtils.getConfig().replaceWithSchema(LocalTime.class, schema);
    }

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("course-service"));
    }
}
