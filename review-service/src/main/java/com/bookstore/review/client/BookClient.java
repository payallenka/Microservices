package com.bookstore.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Declarative HTTP client for book-service. The name is resolved through Eureka and load-balanced,
 * so no host or port is hard-coded here.
 */
@FeignClient(name = "book-service")
public interface BookClient {

	@GetMapping("/api/books/{id}")
	BookSummary getBook(@PathVariable("id") Long id);

	record BookSummary(Long id, String title) {
	}
}
