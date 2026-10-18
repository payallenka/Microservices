package com.bookstore.book.dto;

import java.time.Instant;

import com.bookstore.book.entity.Book;

public record BookResponse(
		Long id,
		String title,
		String author,
		String publisher,
		String isbn,
		Instant createdAt,
		Instant updatedAt) {

	public static BookResponse from(Book book) {
		return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getPublisher(),
				book.getIsbn(), book.getCreatedAt(), book.getUpdatedAt());
	}
}
