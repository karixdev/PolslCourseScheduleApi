package com.github.karixdev.webscraperservice.application.props;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class PlanPolslScrapperProperties {

    public static final String TIME_CELL_CLASS = "CD";
    public static final Pattern TIME_CELL_TEXT_PATTERN = Pattern.compile("^\\d{2}:\\d{2}-\\d{2}:\\d{2}$");

    public static final String COURSE_CELL_CLASS = "coursediv";

}
