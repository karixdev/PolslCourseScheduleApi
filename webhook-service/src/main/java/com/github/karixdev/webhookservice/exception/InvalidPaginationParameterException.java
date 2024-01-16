package com.github.karixdev.webhookservice.exception;

import com.github.karixdev.commonservice.exception.AppBaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPaginationParameterException extends AppBaseException {

	public InvalidPaginationParameterException(String message) {
		super(message, HttpStatus.BAD_REQUEST);
	}

}
