package com.github.karixdev.polslcoursescheduleapi.auth.response;

import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.repsonse.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class SignInResponseTest {
    @Autowired
    JacksonTester<SignInResponse> jTester;

    @Test
    void testSerializationWithUserEntityArgument() throws IOException {
        SignInResponse payload =
                new SignInResponse(
                        "token",
                        new UserResponse(
                                "email@email.com",
                                UserRole.ROLE_USER,
                                TRUE));

        var result = jTester.write(payload);

        assertThat(result).hasJsonPathValue("$.access_token");
        assertThat(result).extractingJsonPathValue("$.access_token")
                .isEqualTo("token");

        assertThat(result).hasJsonPathValue("$.user.email");
        assertThat(result).extractingJsonPathValue("$.user.email")
                .isEqualTo("email@email.com");

        assertThat(result).hasJsonPathValue("$.user.user_role");
        assertThat(result).extractingJsonPathValue("$.user.user_role")
                .isEqualTo("ROLE_USER");

        assertThat(result).hasJsonPathValue("$.user.is_enabled");
        assertThat(result).extractingJsonPathValue("$.user.is_enabled")
                .isEqualTo(true);
    }
}
