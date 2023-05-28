package com.github.karixdev.domaincoursemapperservice.props;

public class CoursesMQProperties {
    public static final String COURSES_EXCHANGE = "course_mapping_flow";

    public static final String DOMAIN_COURSES_QUEUE = "course.mapped";
    public static final String DOMAIN_COURSES_ROUTING_KEY = "course.mapped";

    public static final String RAW_COURSES_QUEUE = "course.raw";
    public static final String RAW_COURSES_ROUTING_KEY = "course.raw";
}
