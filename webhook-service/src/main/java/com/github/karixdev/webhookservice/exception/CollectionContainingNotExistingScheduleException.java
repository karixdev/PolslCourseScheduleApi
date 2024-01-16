package com.github.karixdev.webhookservice.exception;

import com.github.karixdev.commonservice.exception.AppBaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CollectionContainingNotExistingScheduleException extends AppBaseException {

	public CollectionContainingNotExistingScheduleException() {
		super("Provided collection contains not existing schedule's id or schedules' ids", HttpStatus.BAD_REQUEST);
	}

}
