package com.github.karixdev.webhookservice.exception;

import com.github.karixdev.commonservice.exception.AppBaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotExistingDiscordWebhookException extends AppBaseException {

	public NotExistingDiscordWebhookException() {
		super("Provided Discord webhook does not exist", HttpStatus.BAD_REQUEST);
	}

}
