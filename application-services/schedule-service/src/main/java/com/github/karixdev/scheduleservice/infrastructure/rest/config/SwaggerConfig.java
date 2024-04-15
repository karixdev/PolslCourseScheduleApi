package com.github.karixdev.scheduleservice.infrastructure.rest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String OPEN_ID_SCHEME_NAME = "OpenId";
    private static final String OPENID_CONFIG_FORMAT = "%s/realms/%s/.well-known/openid-configuration";

    static {
        Schema<LocalTime> schema = new Schema<>();
        schema.example(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        SpringDocUtils.getConfig().replaceWithSchema(LocalTime.class, schema);
    }

    @Bean
    OpenAPI openAPI(
            @Value("${keycloak.server-url}") String keycloakServerUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${swagger.server-url}") String serverUrl
    ) {
        Server server = new Server().url(serverUrl);

        return new OpenAPI()
                .info(new Info().title("course-service"))
                .servers(List.of(server))
                .components(new Components()
                        .addSecuritySchemes(
                                OPEN_ID_SCHEME_NAME,
                                createOpenIdScheme(keycloakServerUrl, realm)
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(OPEN_ID_SCHEME_NAME));
    }

    private SecurityScheme createOpenIdScheme(String serverUrl, String realm) {
        String connectUrl = String.format(OPENID_CONFIG_FORMAT, serverUrl, realm);

        return new SecurityScheme()
                .type(SecurityScheme.Type.OPENIDCONNECT)
                .openIdConnectUrl(connectUrl);
    }

}
