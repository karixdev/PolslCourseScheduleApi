package com.github.karixdev.domaincoursemapperservice.mapper;

import com.github.karixdev.domaincoursemapperservice.model.raw.TimeCell;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class TimeCellMapper {
    public LocalTime mapToLocalTime(TimeCell timeCell) {
        return LocalTime.parse(timeCell.text().split("-")[0]);
    }
}
