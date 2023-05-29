package com.github.karixdev.domaincoursemapperservice.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class LocalTimeDeserializerTest {
    LocalTimeDeserializer underTest = new LocalTimeDeserializer();

    @Test
    void GivenJsonParserAndDeserializationContext_WhenDeserialize_ThenReturnsCorrectLocalTime() throws IOException {
        // Given
        String json = """
                {
                    "value": "01:01:01"
                }
                """;

        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jsonParser = mapper.getFactory().createParser(inputStream);
        DeserializationContext ctx = mapper.getDeserializationContext();

        jsonParser.nextToken();
        jsonParser.nextToken();
        jsonParser.nextToken();

        // When
        LocalTime result = underTest.deserialize(jsonParser, ctx);

        // Then
        assertThat(result)
                .isEqualTo(LocalTime.of(
                        1,
                        1,
                        1
                ));
    }
}