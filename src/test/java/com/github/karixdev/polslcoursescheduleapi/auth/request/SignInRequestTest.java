package com.github.karixdev.polslcoursescheduleapi.auth.request;

import com.github.karixdev.polslcoursescheduleapi.auth.payload.request.SignInRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class SignInRequestTest {
    @Autowired
    JacksonTester<SignInRequest> jTester;

    @Test
    void testDeserialization() throws IOException {
        String payload = """
                {
                    "email": "email@email.com",
                    "password": "password"
                }
                """;

        SignInRequest result = jTester.parseObject(payload);

        assertThat(result.getEmail()).isEqualTo("email@email.com");
        assertThat(result.getPassword()).isEqualTo("password");
    }
}