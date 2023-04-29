package com.github.karixdev.webscraperservice.planpolsl;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface PlanPolslClient {
    @GetExchange("/plan.php")
    ByteArrayResource getSchedule(
            @RequestParam(name = "id") int id,
            @RequestParam(name = "type") int type,
            @RequestParam(name = "wd") int wd,
            @RequestParam(name = "winW") int winW,
            @RequestParam(name = "winH") int winH
    );
}
