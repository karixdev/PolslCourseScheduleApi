package com.github.karixdev.webscraperservice.service;

import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HtmlElementService {
    public Map<String, String> getStylesAttr(Element element) {
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

        return map;
    }

    public int getSizeAttr(Element element, String attrName) {
        try {
            return Integer.parseInt(element.attr(attrName));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getCssSizeProperty(Map<String, String> styles, String propertyName) {
        if (styles.get(propertyName) == null) {
            return 0;
        }

        return Integer.parseInt(
                styles.get(propertyName)
                        .split("px")[0]
        );
    }
}
