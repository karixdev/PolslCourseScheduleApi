package com.github.karixdev.webhookservice.exception;

import com.github.karixdev.commonservice.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CollectionContainingNotExistingScheduleException extends ValidationException {

	public CollectionContainingNotExistingScheduleException(String fieldName) {
		super(fieldName, "Provided collection contains not existing schedule");
	}

}
