package com.bookstore.review.exception;

public class BookServiceUnavailableException extends RuntimeException {

	public BookServiceUnavailableException(Throwable cause) {
		super("book-service is unavailable, please try again later", cause);
	}
}
