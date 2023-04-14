package com.github.karixdev.scheduleservice.shared.config;

import com.github.karixdev.scheduleservice.shared.converter.RealmRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST).hasRole("admin")
                        .requestMatchers(HttpMethod.PUT).hasRole("admin")
                        .requestMatchers(HttpMethod.DELETE).hasRole("admin")
                        .requestMatchers(HttpMethod.GET).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(config ->
                        config.jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtConverter())
                        )
                )
                .build();
    }

    Converter<Jwt, ? extends AbstractAuthenticationToken> jwtConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new RealmRoleConverter());

        return converter;
    }
}
