package com.github.karixdev.webhookservice.service;

import com.github.karixdev.webhookservice.converter.RealmRoleConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final RealmRoleConverter realmRoleConverter;

    public boolean isAdmin(Jwt jwt) {
        Collection<GrantedAuthority> roles =
                realmRoleConverter.convert(jwt);

        if (roles == null) {
            return false;
        }

        return roles.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_admin"));
    }

    public String getUserId(Jwt jwt) {
        return jwt.getSubject();
    }
}
