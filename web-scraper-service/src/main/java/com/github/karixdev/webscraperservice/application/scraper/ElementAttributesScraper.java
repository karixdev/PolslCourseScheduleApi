package com.github.karixdev.webscraperservice.application.scraper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(access = AccessLevel.MODULE)
public class ElementAttributesScraper {

    public int getCW(Element element) {
        return getSizeAttr(element, "cw");
    }

    public int getCH(Element element) {
        return getSizeAttr(element, "ch");
    }

    private int getSizeAttr(Element element, String attrName) {
        try {
            return Integer.parseInt(element.attr(attrName));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getHref(Element element) {
        return element.attr("href");
    }

}
