package com.github.karixdev.scheduleservice.shared.mapping;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalTime;

public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    @Override
    public LocalTime deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext
    ) throws IOException {
        return LocalTime.parse(jsonParser.getText());
    }
}
