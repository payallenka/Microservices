package com.bookstore.webui.client;

import java.time.Instant;
import java.util.List;

/**
 * JSON shapes exchanged with the backend APIs.
 */
public final class ApiModels {

	private ApiModels() {
	}

	public record Book(Long id, String title, String author, String publisher, String isbn) {
	}

	public record BookRequest(String title, String author, String publisher, String isbn) {
	}

	public record BookPage(List<Book> content, int page, int size, long totalElements, int totalPages) {
	}

	public record Review(Long id, Long bookId, String reviewer, int rating, String comment, Instant createdAt) {
	}

	public record ReviewRequest(Long bookId, String reviewer, int rating, String comment) {
	}

	public record RatingSummary(Long bookId, long reviewCount, Double averageRating) {
	}
}
