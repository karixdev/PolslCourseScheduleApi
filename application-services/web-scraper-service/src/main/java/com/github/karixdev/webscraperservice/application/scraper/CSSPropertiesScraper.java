package com.github.karixdev.webscraperservice.application.scraper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor(access = AccessLevel.MODULE)
public class CSSPropertiesScraper {

    private Map<String, String> getProperties(Element element) {
        Map<String, String> map = new HashMap<>();

        if (!element.hasAttr("style")) {
            return map;
        }

        String styleStr = element.attr("style");
        String[] keys = styleStr.split(":");

        if (keys.length < 2) {
            return map;
        }

        for (int i = 0; i < keys.length - 1; i++) {
            String[] split = keys[i].split(";");

            String propertyName;
            String propertyValue = keys[i + 1].split(";")[0].trim();

            if (i % 2 != 0) {
                if (split.length == 1) {
                    break;
                }

                propertyName = split[1].trim();
            } else {
                propertyName = split[split.length - 1].trim();
            }

            map.put(propertyName, propertyValue);
        }

        return Collections.unmodifiableMap(map);
    }

    public int getTop(Element element) {

        return getSizeProperty(getProperties(element), "top");
    }

    public int getLeft(Element element) {
        return getSizeProperty(getProperties(element), "left");
    }



    private int getSizeProperty(Map<String, String> properties, String propertyName) {
        if (properties.get(propertyName) == null) {
            return 0;
        }

        return Integer.parseInt(
                properties.get(propertyName)
                        .split("px")[0]
        );
    }

}
