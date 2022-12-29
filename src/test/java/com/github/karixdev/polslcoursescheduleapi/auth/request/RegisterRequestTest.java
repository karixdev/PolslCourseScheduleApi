package com.github.karixdev.polslcoursescheduleapi.auth.request;

import com.github.karixdev.polslcoursescheduleapi.auth.payload.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class RegisterRequestTest {
    @Autowired
    JacksonTester<RegisterRequest> jTester;

    @Test
    void testDeserialization() throws IOException {
        String payload = """
                {
                    "email": "abc@abc.pl",
                    "password": "password"
                }
                """;

        RegisterRequest result = jTester.parseObject(payload);

        assertThat(result.getEmail()).isEqualTo("abc@abc.pl");
        assertThat(result.getPassword()).isEqualTo("password");
    }
}
