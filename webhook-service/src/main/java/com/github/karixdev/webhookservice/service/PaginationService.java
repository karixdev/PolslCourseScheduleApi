package com.github.karixdev.webhookservice.service;

import com.github.karixdev.webhookservice.exception.InvalidPaginationParameterException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PaginationService {

	private final int defaultPageSize;

	public PaginationService(@Value("${pagination.defaults.page-size}") int defaultPageSize) {
		this.defaultPageSize = defaultPageSize;
	}

	public PageRequest getPageRequest(Integer page, Integer pageSize) {
		if (page == null) {
			page = 0;
		}
		if (page < 0) {
			throw new InvalidPaginationParameterException("page", "page must be null or >= 0");
		}

		if (pageSize == null) {
			pageSize = defaultPageSize;
		}
		if (pageSize <= 0) {
			throw new InvalidPaginationParameterException("pageSize", "page must be null or >= 0");
		}

		return PageRequest.of(page, pageSize);
	}

}
