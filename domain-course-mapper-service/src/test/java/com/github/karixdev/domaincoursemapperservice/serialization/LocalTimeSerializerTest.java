package com.github.karixdev.domaincoursemapperservice.serialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class LocalTimeSerializerTest {
    LocalTimeSerializer underTest = new LocalTimeSerializer();

    @Test
    void GivenLocalTimeAndJsonGeneratorAndSerializerProvider_WhenSerialize_ThenPutsToStringLocalTimeIntoJsonGenerator() throws IOException {
        // Given
        LocalTime localTime = LocalTime.of(8, 30, 15);

        Writer writer = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(writer);

        SerializerProvider serializerProvider = new ObjectMapper().getSerializerProvider();

        // When
        underTest.serialize(localTime, jsonGenerator, serializerProvider);
        jsonGenerator.flush();

        // Then
        assertThat(writer.toString()).isEqualTo("\"08:30:15\"");
    }
}