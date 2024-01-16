package com.github.karixdev.webhookservice.exception;

import com.github.karixdev.commonservice.exception.AppBaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnavailableDiscordWebhookUrlException extends AppBaseException {

	public UnavailableDiscordWebhookUrlException() {
		super("Webhook with provided url already exists", HttpStatus.BAD_REQUEST);
	}

}
