package com.github.karixdev.scheduleservice.shared.util;

public class NameCaseConverter {
    public static String camelToSnake(String str) {
        StringBuilder builder = new StringBuilder();

        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                builder.append("_");
            }

            builder.append(Character.toLowerCase(c));
        }

        return builder.toString();
    }
}
