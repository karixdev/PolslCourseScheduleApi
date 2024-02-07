package com.github.karixdev.webhookservice.exception;

import com.github.karixdev.commonservice.exception.AppBaseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DiscordWebhookApiClientException extends AppBaseException {

	private static final String MSG_TEMPLATE = "Discord Webhook API returned client side error status: %s";

	public DiscordWebhookApiClientException(HttpStatusCode apiResponseStatus) {
		super(MSG_TEMPLATE.formatted(apiResponseStatus), HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
