package com.github.karixdev.domainmodelmapperservice.application.mapper;

import com.github.karixdev.domainmodelmapperservice.domain.processed.ProcessedRawTimeInterval;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawTimeInterval;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class ProcessedRawTimeIntervalMapper {

    public ProcessedRawTimeInterval map(RawTimeInterval raw) {
        return ProcessedRawTimeInterval.builder()
                .start(LocalTime.parse(raw.start()))
                .end(LocalTime.parse(raw.end()))
                .build();
    }

}
