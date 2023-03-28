package com.github.karixdev.scheduleservice.shared.mapping;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.Test;

import java.io.*;
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