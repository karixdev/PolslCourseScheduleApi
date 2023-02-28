package com.github.karixdev.webscraperservice.service;

import com.github.karixdev.webscraperservice.dto.PlanPolslResponse;
import com.github.karixdev.webscraperservice.properties.PlanPolslClientProperties;
import com.github.karixdev.webscraperservice.exception.PlanPolslUnavailableException;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanPolslClient {
    private final WebClient webClient;
    private final PlanPolslAdapter adapter;

    public PlanPolslResponse getSchedule(int planPolslId, int type, int wd) {
        String uri = buildUri(planPolslId, type, wd);

        Optional<ByteArrayResource> response = webClient.get().uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    throw new PlanPolslUnavailableException(
                            "plan.polsl.pl responded with status: " + clientResponse.statusCode(),
                            planPolslId, type, wd);
                })
                .bodyToMono(ByteArrayResource.class)
                .blockOptional();

        if (response.isEmpty()) {
            throw new PlanPolslUnavailableException(
                    "plan.polsl.pl responded with empty body",
                    planPolslId, type, wd);
        }

        String responseStr = new String(
                response.get().getByteArray(),
                Charset.forName("ISO-8859-2")
        );

        Document document = Jsoup.parse(responseStr);

        return new PlanPolslResponse(
                adapter.getTimeCells(document),
                adapter.getCourseCells(document)
        );
    }

    private String buildUri(int planPolslId, int type, int wd) {
        return UriComponentsBuilder
                .fromPath(PlanPolslClientProperties.PATH)
                .queryParam("type", type)
                .queryParam("id", planPolslId)
                .queryParam("wd", wd)
                .queryParam("winW", PlanPolslClientProperties.WIN_W)
                .queryParam("winH", PlanPolslClientProperties.WIN_H)
                .toUriString();
    }
}
