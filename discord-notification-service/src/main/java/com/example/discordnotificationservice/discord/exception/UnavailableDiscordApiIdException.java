package com.example.discordnotificationservice.discord.exception;

import com.example.discordnotificationservice.shared.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnavailableDiscordApiIdException extends ValidationException {
    public UnavailableDiscordApiIdException() {
        super("url", "Id in provided url is unavailable");
    }
}
