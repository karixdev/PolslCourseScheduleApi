package com.github.karixdev.webscraperservice.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalTime;

public class LocalTimeSerializer extends JsonSerializer<LocalTime> {
    @Override
    public void serialize(
            LocalTime localTime,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeString(localTime.toString());
    }
}
