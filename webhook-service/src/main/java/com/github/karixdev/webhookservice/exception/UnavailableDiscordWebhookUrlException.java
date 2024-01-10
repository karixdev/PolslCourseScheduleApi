package com.github.karixdev.webhookservice.exception;

import com.github.karixdev.commonservice.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnavailableDiscordWebhookUrlException extends ValidationException {

	public UnavailableDiscordWebhookUrlException(String fieldName) {
		super(fieldName, "Webhook with provided url already exists");
	}

}
