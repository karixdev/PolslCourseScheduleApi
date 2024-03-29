package com.github.karixdev.webhookservice.config;

import com.github.karixdev.commonservice.converter.RealmRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String[] WHITE_LIST = {
			"/swagger-ui/**",
			"/v3/api-docs/**"
	};

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			RealmRoleConverter realmRoleConverter
	) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(request -> request
						.requestMatchers(WHITE_LIST).permitAll()
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(config -> config
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter(realmRoleConverter)))
				)
				.build();
	}

	@Bean
	RealmRoleConverter realmRoleConverter() {
		return new RealmRoleConverter();
	}

	Converter<Jwt, ? extends AbstractAuthenticationToken> jwtConverter(
			RealmRoleConverter realmRoleConverter
	) {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(realmRoleConverter);

		return converter;
	}

}
