package com.github.karixdev.webscraperservice.application.client;

import com.github.karixdev.webscraperservice.application.payload.PlanPolslResponse;

public interface PlanPolslClient {
    PlanPolslResponse getSchedule(int id, int type, int wd, int winW, int winH);

}
