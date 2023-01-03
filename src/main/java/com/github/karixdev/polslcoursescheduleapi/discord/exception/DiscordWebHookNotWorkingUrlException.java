package com.github.karixdev.polslcoursescheduleapi.discord.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DiscordWebHookNotWorkingUrlException extends RuntimeException {
    public DiscordWebHookNotWorkingUrlException() {
        super("Provided discord web hook url is not working properly");
    }
}
