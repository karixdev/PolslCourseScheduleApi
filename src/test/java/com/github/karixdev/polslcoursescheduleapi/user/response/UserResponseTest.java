package com.github.karixdev.polslcoursescheduleapi.user.response;

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
public class UserResponseTest {
    @Autowired
    JacksonTester<UserResponse> jTester;

    @Test
    void testSerialization() throws IOException {
        UserResponse payload = new UserResponse(
                "email@email.com",
                UserRole.ROLE_USER,
                TRUE
        );

        var result = jTester.write(payload);

        assertThat(result).hasJsonPathValue("$.email");
        assertThat(result).extractingJsonPathValue("$.email")
                .isEqualTo("email@email.com");

        assertThat(result).hasJsonPathValue("$.user_role");
        assertThat(result).extractingJsonPathValue("$.user_role")
                .isEqualTo("ROLE_USER");

        assertThat(result).hasJsonPathValue("$.is_enabled");
        assertThat(result).extractingJsonPathValue("$.is_enabled")
                .isEqualTo(true);
    }
}
