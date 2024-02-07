package com.github.karixdev.webhookservice.service;

import com.github.karixdev.commonservice.converter.RealmRoleConverter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

	@InjectMocks
	SecurityService underTest;

	@Mock
	RealmRoleConverter realmRoleConverter;

	@Test
	void GivenJwtWithNullAuthorities_WhenIsAdmin_ThenReturnsFalse() {
		// Given
		Jwt jwt = mock(Jwt.class);

		Mockito.when(realmRoleConverter.convert(jwt))
				.thenReturn(null);

		// When
		boolean result = underTest.isAdmin(jwt);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	void GivenJwtWithoutAdminRole_WhenIsAdmin_ThenReturnsFalse() {
		// Given
		Jwt jwt = mock(Jwt.class);

		Mockito.when(realmRoleConverter.convert(jwt))
				.thenReturn(List.of(
						new SimpleGrantedAuthority("ROLE_user")
				));

		// When
		boolean result = underTest.isAdmin(jwt);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	void GivenJwtWithAdminRole_WhenIsAdmin_ThenReturnsTrue() {
		// Given
		Jwt jwt = mock(Jwt.class);

		Mockito.when(realmRoleConverter.convert(jwt))
				.thenReturn(List.of(
						new SimpleGrantedAuthority("ROLE_admin")
				));

		// When
		boolean result = underTest.isAdmin(jwt);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	void GivenJwt_WhenGetUserId_ThenReturnsUserId() {
		// Given
		Jwt jwt = mock(Jwt.class);
		String userId = "userId";

		Mockito.when(jwt.getSubject()).thenReturn(userId);

		// When
		String result = underTest.getUserId(jwt);

		// Then
		assertThat(result).isEqualTo(userId);
	}


}