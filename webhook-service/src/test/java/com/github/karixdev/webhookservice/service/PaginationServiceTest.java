package com.github.karixdev.webhookservice.service;

import com.github.karixdev.webhookservice.exception.InvalidPaginationParameterException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageRequest;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginationServiceTest {

	PaginationService underTest;

	int defaultPageSize;

	@BeforeEach
	void setUp() {
		defaultPageSize = 10;
		underTest = new PaginationService(defaultPageSize);
	}

	@Test
	void GivenPageSmallerThanZero_WhenGetPageRequest_ThenThrowsInvalidPaginationParameterException() {
		// Given
		int page = -1;
		int pageSize = 10;

		// When & Then
		assertThatThrownBy(() -> underTest.getPageRequest(page, pageSize))
				.isInstanceOf(InvalidPaginationParameterException.class);
	}

	@ParameterizedTest
	@MethodSource("invalidPageSizes")
	void GivenPageSizeSmallerOrEqualToZero_WhenGetPageRequest_ThenThrowsInvalidPaginationParameterException(int pageSize) {
		// Given
		int page = 1;

		// When & Then
		assertThatThrownBy(() -> underTest.getPageRequest(page, pageSize))
				.isInstanceOf(InvalidPaginationParameterException.class);
	}

	@Test
	void GivenNullPageAndNullPageSize_WhenGetPageRequest_ThenReturnsPageRequestWithDefaultValues() {
		// Given
		Integer page = null;
		Integer pageSize = null;

		// When
		PageRequest result = underTest.getPageRequest(page, pageSize);

		// Then
		assertThat(result.getPageNumber()).isZero();
		assertThat(result.getPageSize()).isEqualTo(defaultPageSize);
	}

	@Test
	void GivenValidPageAndPageSize_WhenGetPageSize_ThenReturnsPageSizeWithProvidedValues() {
		// Given
		int page = 1;
		int pageSize = 100;

		// When
		PageRequest result = underTest.getPageRequest(page, pageSize);

		// Then
		assertThat(result.getPageNumber()).isEqualTo(page);
		assertThat(result.getPageSize()).isEqualTo(pageSize);
	}

	private static Stream<Arguments> invalidPageSizes() {
		return Stream.of(
				Arguments.of(0),
				Arguments.of(-1)
		);
	}

}