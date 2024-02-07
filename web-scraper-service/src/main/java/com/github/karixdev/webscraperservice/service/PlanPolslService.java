package com.github.karixdev.webscraperservice.service;

import com.github.karixdev.webscraperservice.client.PlanPolslClient;
import com.github.karixdev.webscraperservice.mapper.PlanPolslResponseMapper;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.props.PlanPolslClientProperties;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

@Service
@RequiredArgsConstructor
public class PlanPolslService {

    private final PlanPolslClient client;
    private final PlanPolslResponseMapper mapper;

    public PlanPolslResponse getSchedule(int planPolslId, int type, int wd) {
        ByteArrayResource response = client.getSchedule(
                planPolslId,
                type,
                wd,
                PlanPolslClientProperties.WIN_W,
                PlanPolslClientProperties.WIN_H
        );

        String responseStr = new String(
                response.getByteArray(),
                Charset.forName("ISO-8859-2")
        );

        Document document = Jsoup.parse(responseStr);

        return mapper.map(document);
    }

}
