package com.github.karixdev.webhookservice.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

public class KeycloakUtils {

    private record KeycloakToken(@JsonProperty("access_token") String accessToken) {}

    private record KeycloakProfile(@JsonProperty("sub") String id) {}

    private static String getToken(String authServerUrl, String username, String password) {
        WebClient webClient = WebClient.builder().build();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.put("grant_type", List.of("password"));
        map.put("client_id", List.of("test-client"));
        map.put("client_secret", List.of("yRKGZsSpZ7I2msjSLxadwJF3qePvQI1V"));
        map.put("username", List.of(username));
        map.put("password", List.of(password));
        map.put("scope", List.of("openid"));

        KeycloakToken token = webClient.post().uri(authServerUrl + "/realms/polsl-course-api/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(map)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KeycloakToken.class)
                .block();

        assert token != null;
        return token.accessToken();
    }

    public static String getAdminToken(String authServerUrl) {
        return getToken(authServerUrl, "admin", "admin");
    }

    public static String getUserToken(String authServerUrl) {
        return getToken(authServerUrl, "user", "user");
    }

    public static String getUserId(String authServerUrl, String jwt) {
        WebClient webClient = WebClient.builder().build();

        KeycloakProfile profile = webClient.get().uri(authServerUrl + "/realms/polsl-course-api/protocol/openid-connect/userinfo")
                .header("Authorization", "Bearer %s".formatted(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KeycloakProfile.class)
                .block();

        assert profile != null;
        return profile.id();
    }

}
