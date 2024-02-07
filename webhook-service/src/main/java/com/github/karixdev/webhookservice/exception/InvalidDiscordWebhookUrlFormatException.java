package com.github.karixdev.webhookservice.exception;

import com.github.karixdev.commonservice.exception.AppBaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDiscordWebhookUrlFormatException extends AppBaseException {

	public InvalidDiscordWebhookUrlFormatException() {
		super("Provided Discord webhook url has invalid format", HttpStatus.BAD_REQUEST);
	}

}
