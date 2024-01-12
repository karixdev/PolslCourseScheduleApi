package com.github.karixdev.webhookservice.service;

import com.github.karixdev.commonservice.converter.RealmRoleConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class SecurityService {

	private final RealmRoleConverter converter;

	private static final String ADMIN_ROLE = "ROLE_admin";

	public boolean isAdmin(Jwt jwt) {
		Collection<GrantedAuthority> authorities = converter.convert(jwt);

		if (authorities == null) {
			return false;
		}

		return authorities.stream().anyMatch(
				authority -> authority.getAuthority().equals(ADMIN_ROLE)
		);
	}

	public String getUserId(Jwt jwt) {
		return jwt.getSubject();
	}

}
