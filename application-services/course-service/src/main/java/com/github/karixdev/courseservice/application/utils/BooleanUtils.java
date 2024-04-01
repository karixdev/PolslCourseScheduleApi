package com.github.karixdev.courseservice.application.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BooleanUtils {

    public static boolean isFalse(Boolean bool) {
        return Boolean.FALSE.equals(bool);
    }

    public static boolean isTrue(Boolean bool) {
        return Boolean.TRUE.equals(bool);
    }

}
