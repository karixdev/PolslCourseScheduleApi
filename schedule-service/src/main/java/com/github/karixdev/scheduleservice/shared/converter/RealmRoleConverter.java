package com.github.karixdev.scheduleservice.shared.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class RealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        if (jwt.getClaims().get("realm_access") == null) {
            return Collections.emptySet();
        }

        Map<String, List<String>> realmAccess =
                (Map<String, List<String>>) jwt.getClaims().get("realm_access");

        if (!realmAccess.containsKey("roles")) {
            return Collections.emptySet();
        }

        return realmAccess.get("roles").stream()
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toSet());
    }
}
