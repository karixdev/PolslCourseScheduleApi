package com.github.karixdev.polslcoursescheduleapi.frontend;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CssService {
    public int getSizePropertyValue(
            Map<String, String> stylesMap,
            String propertyName
    ) {
        if (stylesMap.get(propertyName) == null) {
            return 0;
        }

        return Integer.parseInt(stylesMap.get(propertyName).split("px")[0]);
    }
}
