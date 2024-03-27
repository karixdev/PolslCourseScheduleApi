package com.github.karixdev.commonservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AppBaseException extends RuntimeException {

	private final HttpStatus httpStatus;

	protected AppBaseException(String message, HttpStatus httpStatus) {
		super(message);
		this.httpStatus = httpStatus;
	}

}
