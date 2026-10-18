package com.bookstore.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bookstore.review.client.BookClient;
import com.bookstore.review.client.BookClient.BookSummary;
import com.bookstore.review.dto.RatingSummary;
import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;

import feign.FeignException;

/**
 * Runs against a real PostgreSQL (Testcontainers); only the call to book-service is stubbed.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "eureka.client.enabled=false")
class ReviewApiIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	private TestRestTemplate rest;

	@MockBean
	private BookClient bookClient;

	@Test
	void reviewsAreStoredAndSummarised() {
		when(bookClient.getBook(1L)).thenReturn(new BookSummary(1L, "Effective Java"));

		ResponseEntity<ReviewResponse> first = rest.postForEntity("/api/reviews",
				new ReviewRequest(1L, "Ada", 5, "Essential reading"), ReviewResponse.class);
		rest.postForEntity("/api/reviews", new ReviewRequest(1L, "Linus", 4, null), ReviewResponse.class);

		assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		ResponseEntity<ReviewResponse[]> reviews = rest.getForEntity("/api/reviews?bookId=1", ReviewResponse[].class);
		assertThat(reviews.getBody()).hasSize(2);

		RatingSummary summary = rest.getForObject("/api/reviews/summary?bookId=1", RatingSummary.class);
		assertThat(summary).isEqualTo(new RatingSummary(1L, 2, 4.5));
	}

	@Test
	void reviewForUnknownBookIsRejected() {
		when(bookClient.getBook(404L)).thenThrow(mock(FeignException.NotFound.class));

		ResponseEntity<String> response = rest.postForEntity("/api/reviews",
				new ReviewRequest(404L, "Ada", 3, null), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
