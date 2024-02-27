package com.github.karixdev.webscraperservice.infrastructure.client;

import com.github.karixdev.webscraperservice.application.client.PlanPolslClient;
import com.github.karixdev.webscraperservice.application.payload.PlanPolslResponse;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.Charset;

@RequiredArgsConstructor
public class PlanPolslClientAdapter implements PlanPolslClient {

    private final HttpInterfacesPlanPolslClient client;

    @Override
    public PlanPolslResponse getSchedule(int id, int type, int wd, int winW, int winH) {
        ByteArrayResource response = client.getSchedule(
                id,
                type,
                wd,
                winW,
                winH
        );

        String responseInCorrectEncoding = new String(
                response.getByteArray(),
                Charset.forName("ISO-8859-2")
        );

        return PlanPolslResponse.builder()
                .content(Jsoup.parse(responseInCorrectEncoding))
                .build();
    }

}
