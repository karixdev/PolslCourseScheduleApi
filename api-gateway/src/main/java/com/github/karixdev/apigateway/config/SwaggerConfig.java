package com.github.karixdev.apigateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String AUTH_SERVER_URL = "http://localhost:8000";
    private static final String REALM = "polsl-course-api";

    private static final String OPEN_ID_SCHEME_NAME = "OpenId";
    private static final String OPENID_CONFIG_FORMAT = "%s/realms/%s/.well-known/openid-configuration";

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("PolslCourseScheduleApi"))
                .components(new Components()
                        .addSecuritySchemes(OPEN_ID_SCHEME_NAME, createOpenIdScheme())
                )
                .addSecurityItem(new SecurityRequirement().addList(OPEN_ID_SCHEME_NAME));
    }

    private SecurityScheme createOpenIdScheme() {
        String connectUrl = String.format(OPENID_CONFIG_FORMAT, AUTH_SERVER_URL, REALM);

        return new SecurityScheme()
                .type(SecurityScheme.Type.OPENIDCONNECT)
                .openIdConnectUrl(connectUrl);
    }
}
