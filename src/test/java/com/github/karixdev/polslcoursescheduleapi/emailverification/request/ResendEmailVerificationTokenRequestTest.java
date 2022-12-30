package com.github.karixdev.polslcoursescheduleapi.emailverification.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ResendEmailVerificationTokenRequestTest {
    @Autowired
    JacksonTester<ResendEmailVerificationTokenRequest> jTester;

    @Test
    void testDeserialize() throws IOException {
        String content = """
                {
                    "email": "abc@abc.pl"
                }
                """;

        ResendEmailVerificationTokenRequest result =
                jTester.parseObject(content);

        assertThat(result.getEmail()).isEqualTo("abc@abc.pl");

    }
}
