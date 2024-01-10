package com.github.karixdev.webhookservice.exception;

import com.github.karixdev.commonservice.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDiscordWebhookUrlFormatException extends ValidationException {

	public InvalidDiscordWebhookUrlFormatException(String fieldName) {
		super(fieldName, "Provided Discord webhook url has invalid format");
	}

}
