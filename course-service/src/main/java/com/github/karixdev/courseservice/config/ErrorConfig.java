package com.github.karixdev.courseservice.config;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Configuration
public class ErrorConfig {

    @Bean
    DefaultErrorAttributes defaultErrorAttributes() {
        return new CustomErrorAttributes();
    }

    private static class CustomErrorAttributes extends DefaultErrorAttributes {

        @Override
        public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
            Map<String, Object> attrs = super.getErrorAttributes(webRequest, options);

            attrs.remove("timestamp");
            attrs.remove("path");

            return attrs;
        }

    }

}
