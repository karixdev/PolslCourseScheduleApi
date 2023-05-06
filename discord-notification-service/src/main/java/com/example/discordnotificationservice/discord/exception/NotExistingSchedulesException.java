package com.example.discordnotificationservice.discord.exception;

import com.example.discordnotificationservice.shared.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotExistingSchedulesException extends ValidationException {
    public NotExistingSchedulesException() {
        super("schedules", "Provided set of schedules includes non-existing schedules");
    }
}
