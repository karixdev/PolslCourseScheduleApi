package com.github.karixdev.courseservice.application.client;

import java.util.UUID;

public interface ScheduleServiceClient {
    Boolean doesScheduleWithIdExist(UUID id);
}
