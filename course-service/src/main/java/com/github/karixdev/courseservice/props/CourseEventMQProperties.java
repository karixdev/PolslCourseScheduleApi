package com.github.karixdev.courseservice.props;

public class CourseEventMQProperties {
    public static final String COURSES_UPDATE_EXCHANGE = "schedule_events";
    public static final String COURSES_UPDATE_QUEUE = "schedule.delete";
    public static final String COURSES_UPDATE_ROUTING_KEY = "schedule.update";
}
