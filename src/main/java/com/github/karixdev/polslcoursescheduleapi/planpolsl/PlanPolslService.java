package com.github.karixdev.polslcoursescheduleapi.planpolsl;

import com.github.karixdev.polslcoursescheduleapi.planpolsl.exception.PlanPolslEmptyResponseException;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.exception.PlanPolslWebClientException;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.PlanPolslResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanPolslService {
    private final WebClient webClient;
    private final PlanPolslProperties properties;
    private final PlanPolslResponseMapper responseMapper;

    public PlanPolslResponse getPlanPolslResponse(Schedule schedule) {
        String uri = createUri(schedule);

        Optional<ByteArrayResource> resp = webClient.get().uri(uri)
                .retrieve()
                .bodyToMono(ByteArrayResource.class)
                .blockOptional();

        if (resp.isEmpty()) {
            throw new PlanPolslEmptyResponseException();
        }

        String responseString =
                new String(resp.get().getByteArray(), Charset.forName("ISO-8859-2"));

        Document document = Jsoup.parse(responseString);

        return new PlanPolslResponse(
                responseMapper.getTimeCells(document),
                responseMapper.getCourseCells(document)
        );
    }

    private String createUri(Schedule schedule) {
        return UriComponentsBuilder
                .fromUriString(properties.getBaseUrl())
                .queryParam("winW", properties.getWinW())
                .queryParam("winH", properties.getWinH())
                .queryParam("type", schedule.getType())
                .queryParam("id", schedule.getPlanPolslId())
                .build()
                .toUriString();
    }
}
